package com.energy.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataMessage {
    
    @JsonProperty("device_id")
    private String deviceId;
    
    @JsonProperty("measurement_value")
    private Double measurementValue;
    
    private String timestamp; // ISO format string
}
