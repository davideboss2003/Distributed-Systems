package com.energy.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pentru evenimente de overconsumption
 * Acest mesaj va fi publicat în RabbitMQ când un device depășește consumul maxim
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverconsumptionEvent {
    
    private String deviceId;
    private String userId;
    private Double consumption;
    private Double maxConsumption;
    private LocalDateTime timestamp;
}
