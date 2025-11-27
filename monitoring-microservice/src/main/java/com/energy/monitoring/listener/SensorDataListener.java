package com.energy.monitoring.listener;

import com.energy.monitoring.dto.SensorDataMessage;
import com.energy.monitoring.service.ConsumptionAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@RequiredArgsConstructor
public class SensorDataListener {
    
    private final ConsumptionAggregationService aggregationService;
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    
    @RabbitListener(queues = "${app.rabbitmq.sensor-data-queue}")
    public void onSensorData(SensorDataMessage message) {
        try {
            log.debug("Received sensor data: device={}, value={} kWh",
                    message.getDeviceId(), message.getMeasurementValue());
            
            // Parse timestamp
            LocalDateTime timestamp = LocalDateTime.parse(
                    message.getTimestamp().replace("Z", ""),
                    ISO_FORMATTER
            );
            
            // Add to aggregation service
            aggregationService.addMeasurement(
                    message.getDeviceId(),
                    message.getMeasurementValue(),
                    timestamp
            );
            
        } catch (Exception e) {
            log.error("Error processing sensor data: {}", e.getMessage(), e);
        }
    }
}
