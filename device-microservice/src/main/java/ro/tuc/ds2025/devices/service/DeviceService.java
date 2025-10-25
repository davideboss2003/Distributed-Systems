package ro.tuc.ds2025.devices.service;

import ro.tuc.ds2025.devices.dto.CreateDeviceDTO;
import ro.tuc.ds2025.devices.dto.DeviceDTO;
import ro.tuc.ds2025.devices.dto.UpdateDeviceDTO;
import ro.tuc.ds2025.devices.exception.ResourceNotFoundException;
import ro.tuc.ds2025.devices.exception.ValidationException;
import ro.tuc.ds2025.devices.model.Device;
import ro.tuc.ds2025.devices.model.User;
import ro.tuc.ds2025.devices.repository.DeviceRepository;
import ro.tuc.ds2025.devices.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor

public class DeviceService {
    private final DeviceRepository deviceRepository;  // CRUD către tabela devices
    private final UserRepository userRepository;      // CRUD către tabela users (copie locală)
    private final ModelMapper modelMapper;            // mapare automată entity ↔ DTO
    private final RestTemplate restTemplate;          // client HTTP sincron către user-microservice
    
    @org.springframework.beans.factory.annotation.Value("${user.microservice.url}")
    private String userMicroserviceUrl;


    ///nu ret entitatile catre ext exp doar Dto fara coloane sensibile
    @Transactional(readOnly = true)
    public List<DeviceDTO> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Metodă helper pentru conversie Device -> DeviceDTO cu informații User
    private DeviceDTO convertToDTO(Device device) {
        DeviceDTO dto = modelMapper.map(device, DeviceDTO.class);
        if (device.getUser() != null) {
            dto.setUserId(String.valueOf(device.getUser().getId()));
            dto.setUsername(device.getUser().getUsername());

        }
        return dto;
    }


    //arunca 404 daca nu exista adica GlobalExceptionHandler
    @Transactional(readOnly = true)
    public DeviceDTO getDeviceById(String id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
        return convertToDTO(device);
    }

    @Transactional(readOnly = true)
    public List<DeviceDTO> getDevicesByUserId(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return deviceRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    ////CRUDs

///verifica daca un sure cu userId exista cu adv inainte sa legam device-ul de el



    // Sincronizează user-ul din User Microservice în baza locală
    private User syncUserFromUserMicroservice(String userId) {
        try {
            String userServiceUrl = userMicroserviceUrl + "/api/users/" + userId;
            // Presupunem că User Microservice returnează un JSON cu: id, username, email, role
            var userResponse = restTemplate.getForObject(userServiceUrl, java.util.Map.class);
            
            if (userResponse == null) {
                throw new ValidationException("User not found with id: " + userId);
            }
            
            // Verifică dacă user-ul există deja local
            User localUser = userRepository.findById(UUID.fromString(userId)).orElse(new User());
            localUser.setId(UUID.fromString(userId));
            localUser.setUsername((String) userResponse.get("username"));

            
            return userRepository.save(localUser);
        } catch (Exception e) {
            throw new ValidationException("User not found with id: " + userId);
        }
    }
    
    @Transactional
    public DeviceDTO createDevice(CreateDeviceDTO createDeviceDTO) {
        Device device = modelMapper.map(createDeviceDTO, Device.class);
        
        // Dacă userId este furnizat, sincronizează user-ul
        if (createDeviceDTO.getUserId() != null) {
            User user = syncUserFromUserMicroservice(createDeviceDTO.getUserId());
            device.setUser(user);
        }

        Device savedDevice = deviceRepository.save(device);
        return convertToDTO(savedDevice);
    }

    @Transactional
    public DeviceDTO updateDevice(String id, UpdateDeviceDTO updateDeviceDTO) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        // Update only non-null fields
        if (updateDeviceDTO.getDescription() != null) {
            device.setDescription(updateDeviceDTO.getDescription());
        }

        if (updateDeviceDTO.getAddress() != null) {
            device.setAddress(updateDeviceDTO.getAddress());
        }

        if (updateDeviceDTO.getMaximumHourlyConsumption() != null) {
            device.setMaximumHourlyConsumption(updateDeviceDTO.getMaximumHourlyConsumption());
        }

        if (updateDeviceDTO.getUserId() != null) {
            User user = syncUserFromUserMicroservice(updateDeviceDTO.getUserId());
            device.setUser(user);
        }

        Device updatedDevice = deviceRepository.save(device);
        return convertToDTO(updatedDevice);
    }

    @Transactional
    public void deleteDevice(String id) {
        if (!deviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Device not found with id: " + id);
        }
        deviceRepository.deleteById(id);
    }

    @Transactional
    public DeviceDTO assignDeviceToUser(String deviceId, String userId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        User user = syncUserFromUserMicroservice(userId);
        device.setUser(user);

        Device updatedDevice = deviceRepository.save(device);
        return convertToDTO(updatedDevice);
    }

}
