package com.energy.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pentru evenimente de overconsumption primite din RabbitMQ
 * Acest mesaj vine de la Monitoring Microservice
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
