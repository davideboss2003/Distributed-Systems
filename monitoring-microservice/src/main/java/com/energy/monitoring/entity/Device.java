package com.energy.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    
    @Id
    @Column(length = 36)
    private String id;
    
    @Column(name = "user_id", length = 36)
    private String userId;
    
    @Column(length = 255)
    private String description;
    
    @Column(length = 255)
    private String address;
    
    @Column(name = "max_hourly_consumption")
    private Double maxHourlyConsumption;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
