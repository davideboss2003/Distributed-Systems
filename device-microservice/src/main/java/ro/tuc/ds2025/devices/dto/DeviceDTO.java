package ro.tuc.ds2025.devices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDTO {
    private String id;
    private String description;
    private String address;
    private Double maximumHourlyConsumption;
    private String userId;
    private String username;  //  despre user
    private String userEmail; 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
