package com.energy.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hourly_consumption", indexes = {
    @Index(name = "idx_device_id", columnList = "device_id"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyConsumption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "device_id", length = 36, nullable = false)
    private String deviceId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "consumption_kwh", nullable = false)
    private Double consumptionKwh;
    
    @Column(name = "measurement_count")
    private Integer measurementCount;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
