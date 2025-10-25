package ro.tuc.ds2025.devices.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.tuc.ds2025.devices.dto.UserSyncDTO;
import ro.tuc.ds2025.devices.service.UserSyncService;

import java.util.UUID;

@RestController
@RequestMapping("/api/sync/users")
@RequiredArgsConstructor
public class UserSyncController {

    private final UserSyncService userSyncService;

    /**
     * Endpoint pentru sincronizarea userilor din User Microservice
     * Acest endpoint va fi apelat de User Microservice când se creează/actualizează un user
     */
    @PostMapping
    public ResponseEntity<Void> syncUser(@RequestBody UserSyncDTO userSyncDTO) {
        userSyncService.syncUser(userSyncDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint pentru ștergerea unui user din cache-ul local
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userSyncService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}
