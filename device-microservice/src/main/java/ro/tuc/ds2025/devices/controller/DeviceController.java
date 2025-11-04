package ro.tuc.ds2025.devices.controller;

import ro.tuc.ds2025.devices.dto.CreateDeviceDTO;
import ro.tuc.ds2025.devices.dto.DeviceDTO;
import ro.tuc.ds2025.devices.dto.UpdateDeviceDTO;

import ro.tuc.ds2025.devices.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")

public class DeviceController {
    private final DeviceService deviceService;

    //GET /api/devices Lista tuturor device-urilor - CLIENT și ADMIN
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public ResponseEntity<List<DeviceDTO>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/{id}")  ///clientul are acces la detaliile propriului device
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable String id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @GetMapping("/user/{userId}")  // userul vede device-rile proprii
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and #userId == authentication.principal)")
    public ResponseEntity<List<DeviceDTO>> getDevicesByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(deviceService.getDevicesByUserId(userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")  // POST /api/devices Creare device autorizare admin
    public ResponseEntity<DeviceDTO> createDevice(@Valid @RequestBody CreateDeviceDTO createDeviceDTO) {
        DeviceDTO createdDevice = deviceService.createDevice(createDeviceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);
    }

    @PutMapping("/{id}")    ///Update total / parțial autorizare admin
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeviceDTO> updateDevice(
            @PathVariable String id,
            @Valid @RequestBody UpdateDeviceDTO updateDeviceDTO) {
        return ResponseEntity.ok(deviceService.updateDevice(id, updateDeviceDTO));
    }

    @DeleteMapping("/{id}") //Ștergere
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDevice(@PathVariable String id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{deviceId}/assign/{userId}")  //Assign device la alt user
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeviceDTO> assignDeviceToUser(
            @PathVariable String deviceId,
            @PathVariable String userId) {
        return ResponseEntity.ok(deviceService.assignDeviceToUser(deviceId, userId));
    }


}
