package com.energy.monitoring.listener;

import com.energy.monitoring.dto.SyncEventMessage;
import com.energy.monitoring.entity.Device;
import com.energy.monitoring.entity.User;
import com.energy.monitoring.repository.DeviceRepository;
import com.energy.monitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SyncQueueListener {
    
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    
    @RabbitListener(queues = "${app.rabbitmq.sync-queue}")
    public void onSyncEvent(SyncEventMessage message) {
        log.info("Received sync event: {}", message.getEvent());
        
        try {
            switch (message.getEvent()) {
                case "USER_CREATED":
                    handleUserCreated(message);
                    break;
                case "DEVICE_CREATED":
                    handleDeviceCreated(message);
                    break;
                default:
                    log.warn("Unknown event type: {}", message.getEvent());
            }
        } catch (Exception e) {
            log.error("Error processing sync event: {}", e.getMessage(), e);
        }
    }
    
    private void handleUserCreated(SyncEventMessage message) {
        User user = new User();
        user.setId(message.getUserId());
        user.setUsername(message.getUsername());
        user.setEmail(message.getEmail());
        
        userRepository.save(user);
        
        log.info("User synced: id={}, username={}", user.getId(), user.getUsername());
    }
    
    private void handleDeviceCreated(SyncEventMessage message) {
        Device device = new Device();
        device.setId(message.getDeviceId());
        device.setUserId(message.getUserId());
        device.setDescription(message.getDescription());
        device.setAddress(message.getAddress());
        device.setMaxHourlyConsumption(message.getMaxHourlyConsumption());
        
        deviceRepository.save(device);
        
        log.info("Device synced: id={}, description={}", device.getId(), device.getDescription());
    }
}
