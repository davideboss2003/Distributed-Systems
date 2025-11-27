package com.energy.monitoring.controller;

import com.energy.monitoring.entity.Device;
import com.energy.monitoring.entity.HourlyConsumption;
import com.energy.monitoring.entity.User;
import com.energy.monitoring.repository.DeviceRepository;
import com.energy.monitoring.repository.HourlyConsumptionRepository;
import com.energy.monitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/monitoring")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MonitoringController {
    
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final HourlyConsumptionRepository consumptionRepository;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "monitoring-microservice");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        List<Map<String, Object>> response = users.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("username", user.getUsername());
                    map.put("email", user.getEmail());
                    map.put("createdAt", user.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/devices")
    public ResponseEntity<List<Map<String, Object>>> getAllDevices() {
        List<Device> devices = deviceRepository.findAll();
        
        List<Map<String, Object>> response = devices.stream()
                .map(device -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", device.getId());
                    map.put("userId", device.getUserId());
                    map.put("description", device.getDescription());
                    map.put("address", device.getAddress());
                    map.put("maxHourlyConsumption", device.getMaxHourlyConsumption());
                    map.put("createdAt", device.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/devices/{deviceId}/consumption")
    public ResponseEntity<List<Map<String, Object>>> getDeviceConsumption(
            @PathVariable String deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("Fetching consumption for device: {}, startDate: {}, endDate: {}", deviceId, startDate, endDate);
        
        List<HourlyConsumption> consumptions;
        
        if (startDate != null && endDate != null) {
            consumptions = consumptionRepository.findByDeviceIdAndTimestampBetweenOrderByTimestampAsc(
                    deviceId, startDate, endDate
            );
        } else {
            consumptions = consumptionRepository.findByDeviceIdOrderByTimestampAsc(deviceId);
        }
        
        List<Map<String, Object>> response = consumptions.stream()
                .map(consumption -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timestamp", consumption.getTimestamp());
                    map.put("consumption", consumption.getConsumptionKwh());
                    map.put("measurementCount", consumption.getMeasurementCount());
                    return map;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}
