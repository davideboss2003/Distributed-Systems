package ro.tuc.ds2025.devices.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceCreatedEvent {
    
    private String event = "DEVICE_CREATED";
    private String deviceId;
    private String userId;
    private String description;
    private String address;
    private Double maxHourlyConsumption;
}
