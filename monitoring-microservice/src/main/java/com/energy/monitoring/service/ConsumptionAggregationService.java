package com.energy.monitoring.service;

import com.energy.monitoring.dto.OverconsumptionEvent;
import com.energy.monitoring.entity.Device;
import com.energy.monitoring.entity.HourlyConsumption;
import com.energy.monitoring.repository.DeviceRepository;
import com.energy.monitoring.repository.HourlyConsumptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsumptionAggregationService {
    
    private final HourlyConsumptionRepository consumptionRepository;
    private final DeviceRepository deviceRepository;
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${app.rabbitmq.overconsumption-queue}")
    private String overconsumptionQueue;
    
    // Buffer: deviceId -> (hourTimestamp -> list of measurements)
    private final Map<String, Map<LocalDateTime, List<Double>>> consumptionBuffer = new ConcurrentHashMap<>();
    
    private static final int MEASUREMENTS_PER_HOUR = 6; // 6 measurements × 10 minutes = 1 hour
    
    public void addMeasurement(String deviceId, Double value, LocalDateTime timestamp) {
        // Round timestamp to hour
        LocalDateTime hourTimestamp = timestamp.truncatedTo(ChronoUnit.HOURS);
        
        // Initialize buffer for device if not exists
        consumptionBuffer.putIfAbsent(deviceId, new ConcurrentHashMap<>());
        Map<LocalDateTime, List<Double>> deviceBuffer = consumptionBuffer.get(deviceId);
        
        // Initialize list for hour if not exists
        deviceBuffer.putIfAbsent(hourTimestamp, Collections.synchronizedList(new ArrayList<>()));
        List<Double> measurements = deviceBuffer.get(hourTimestamp);
        
        // Add measurement
        measurements.add(value);
        
        log.debug("Added measurement for device {}: {} kWh at {} (buffer size: {}/{})",
                deviceId, value, hourTimestamp, measurements.size(), MEASUREMENTS_PER_HOUR);
        
        // Check if we have enough measurements to aggregate
        if (measurements.size() >= MEASUREMENTS_PER_HOUR) {
            aggregateAndSave(deviceId, hourTimestamp, measurements);
            // Clear buffer for this hour
            deviceBuffer.remove(hourTimestamp);
        }
    }
    
    private void aggregateAndSave(String deviceId, LocalDateTime hourTimestamp, List<Double> measurements) {
        // Calculate total consumption
        double totalConsumption = measurements.stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        
        // Round to 2 decimal places
        totalConsumption = Math.round(totalConsumption * 100.0) / 100.0;
        
        // Create and save entity
        HourlyConsumption consumption = new HourlyConsumption();
        consumption.setDeviceId(deviceId);
        consumption.setTimestamp(hourTimestamp);
        consumption.setConsumptionKwh(totalConsumption);
        consumption.setMeasurementCount(measurements.size());
        
        consumptionRepository.save(consumption);
        
        log.info(" Saved hourly consumption: device={}, timestamp={}, consumption={} kWh, measurements={}",
                deviceId, hourTimestamp, totalConsumption, measurements.size());
        
        // ========== OVERCONSUMPTION DETECTION ==========
        checkOverconsumption(deviceId, totalConsumption, hourTimestamp);
    }
    
    /**
     * Verifică dacă device-ul a depășit consumul maxim orar
     * Dacă DA, publică un event în RabbitMQ pentru notificare
     */
    private void checkOverconsumption(String deviceId, double consumption, LocalDateTime timestamp) {
        // Găsește device-ul în DB pentru a obține maxHourlyConsumption
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        
        if (deviceOpt.isEmpty()) {
            log.warn(" Device not found for overconsumption check: {}", deviceId);
            return;
        }
        
        Device device = deviceOpt.get();
        Double maxConsumption = device.getMaxHourlyConsumption();
        
        if (maxConsumption == null) {
            log.debug(" Device {} has no max consumption limit set", deviceId);
            return;
        }
        
        // Verifică dacă a depășit limita
        if (consumption > maxConsumption) {
            log.warn(" OVERCONSUMPTION DETECTED! Device: {}, Consumption: {} kWh > Max: {} kWh",
                     deviceId, consumption, maxConsumption);
            
            // Creează event pentru RabbitMQ
            OverconsumptionEvent event = new OverconsumptionEvent(
                deviceId,
                device.getUserId(),
                consumption,
                maxConsumption,
                timestamp
            );
            
            // Publică în RabbitMQ
            try {
                rabbitTemplate.convertAndSend(overconsumptionQueue, event);
                log.info("-- Published overconsumption event to RabbitMQ: {}", event);
            } catch (Exception e) {
                log.error("-- Failed to publish overconsumption event", e);
            }
        } else {
            log.debug("-- Consumption within limits: {} kWh <= {} kWh", consumption, maxConsumption);
        }
    }
}
