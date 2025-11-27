package com.energy.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncEventMessage {
    
    private String event; // "USER_CREATED" or "DEVICE_CREATED"
    
    // User fields
    @JsonProperty("userId")
    private String userId;
    
    private String username;
    private String email;
    
    // Device fields
    @JsonProperty("deviceId")
    private String deviceId;
    
    private String description;
    private String address;
    
    @JsonProperty("maxHourlyConsumption")
    private Double maxHourlyConsumption;
}
