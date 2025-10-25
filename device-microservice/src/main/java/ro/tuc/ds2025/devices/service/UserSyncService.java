package ro.tuc.ds2025.devices.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.tuc.ds2025.devices.dto.UserSyncDTO;
import ro.tuc.ds2025.devices.model.User;
import ro.tuc.ds2025.devices.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserRepository userRepository;

    /**
     * Sincronizează un user din User Microservice în baza locală
     */
    @Transactional
    public void syncUser(UserSyncDTO userSyncDTO) {
        log.info("Syncing user from User Microservice: id={}, username={}", 
                 userSyncDTO.getId(), userSyncDTO.getUsername());

        User user = userRepository.findById(userSyncDTO.getId())
                .orElse(new User());

        user.setId(userSyncDTO.getId());
        user.setUsername(userSyncDTO.getUsername());

        userRepository.save(user);
        
        log.info("User synced successfully: {}", userSyncDTO.getId());
    }

    /**
     * Șterge un user din cache-ul local
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user from local cache: {}", userId);
        userRepository.deleteById(userId);
        log.info("User deleted successfully: {}", userId);
    }
}
