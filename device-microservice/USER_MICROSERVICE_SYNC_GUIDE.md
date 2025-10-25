# Ghid: Sincronizare User Microservice → Device Microservice

## Problema
Când modifici sau ștergi un user în **user-microservice**, modificările nu se propagă automat în **device-microservice**.

## Soluția
User-microservice trebuie să apeleze endpoint-urile de sincronizare din device-microservice după operațiile de UPDATE și DELETE.

---

## Endpoint-uri disponibile în Device Microservice

### 1. Sincronizare User (CREATE/UPDATE)
```
POST http://localhost:8082/api/sync/users
Content-Type: application/json

{
  "id": "08df323e-589e-4a83-83bb-bcbbcc606849",
  "username": "username_modificat"
}
```

### 2. Ștergere User (DELETE)
```
DELETE http://localhost:8082/api/sync/users/08df323e-589e-4a83-83bb-bcbbcc606849
```

**IMPORTANT**: Când ștergi un user, se vor șterge automat și toate device-urile asociate!

---

## Implementare în User Microservice

### Pasul 1: Adaugă RestTemplate în configurație

Creează sau modifică fișierul `config/RestTemplateConfig.java`:

```java
package ro.tuc.ds2025.users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### Pasul 2: Creează DTO pentru sincronizare

Creează `dto/UserSyncDTO.java`:

```java
package ro.tuc.ds2025.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSyncDTO {
    private UUID id;
    private String username;
}
```

### Pasul 3: Creează serviciu de sincronizare

Creează `service/DeviceMicroserviceSyncService.java`:

```java
package ro.tuc.ds2025.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ro.tuc.ds2025.users.dto.UserSyncDTO;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceMicroserviceSyncService {

    private final RestTemplate restTemplate;
    
    @Value("${device.microservice.url:http://localhost:8082}")
    private String deviceMicroserviceUrl;

    /**
     * Sincronizează user-ul în Device Microservice (CREATE/UPDATE)
     */
    public void syncUser(UUID userId, String username) {
        try {
            String url = deviceMicroserviceUrl + "/api/sync/users";
            UserSyncDTO syncDTO = new UserSyncDTO(userId, username);
            
            restTemplate.postForEntity(url, syncDTO, Void.class);
            log.info("User synced to Device Microservice: id={}, username={}", userId, username);
        } catch (Exception e) {
            log.error("Failed to sync user to Device Microservice: {}", e.getMessage());
            // Nu aruncăm excepția pentru a nu bloca operația principală
        }
    }

    /**
     * Șterge user-ul din Device Microservice (DELETE)
     * ATENȚIE: Aceasta va șterge și toate device-urile asociate!
     */
    public void deleteUser(UUID userId) {
        try {
            String url = deviceMicroserviceUrl + "/api/sync/users/" + userId;
            
            restTemplate.delete(url);
            log.info("User deleted from Device Microservice: id={}", userId);
        } catch (Exception e) {
            log.error("Failed to delete user from Device Microservice: {}", e.getMessage());
            // Nu aruncăm excepția pentru a nu bloca operația principală
        }
    }
}
```

### Pasul 4: Modifică UserService

În `service/UserService.java`, injectează și folosește serviciul de sincronizare:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final DeviceMicroserviceSyncService deviceSyncService; // ADAUGĂ ACEST FIELD

    // ... alte metode ...

    /**
     * UPDATE - Modifică un user
     */
    @Transactional
    public UserDTO updateUser(UUID id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Actualizează datele
        if (userDTO.getUsername() != null) {
            user.setUsername(userDTO.getUsername());
        }
        // ... alte câmpuri ...

        User savedUser = userRepository.save(user);
        
        // SINCRONIZEAZĂ CU DEVICE MICROSERVICE
        deviceSyncService.syncUser(savedUser.getId(), savedUser.getUsername());
        
        return mapToDTO(savedUser);
    }

    /**
     * DELETE - Șterge un user
     */
    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }

        userRepository.deleteById(id);
        
        // ȘTERGE DIN DEVICE MICROSERVICE (inclusiv device-urile asociate)
        deviceSyncService.deleteUser(id);
        
        log.info("User deleted: {}", id);
    }

    /**
     * CREATE - Creează un user nou
     */
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        // ... alte câmpuri ...

        User savedUser = userRepository.save(user);
        
        // SINCRONIZEAZĂ CU DEVICE MICROSERVICE
        deviceSyncService.syncUser(savedUser.getId(), savedUser.getUsername());
        
        return mapToDTO(savedUser);
    }
}
```

### Pasul 5: Configurează URL-ul în application.properties

Adaugă în `src/main/resources/application.properties`:

```properties
# Device Microservice URL
device.microservice.url=http://localhost:8082
```

---

## Testare

### Test 1: UPDATE User
```bash
# 1. Modifică username-ul în user-microservice
PUT http://localhost:8081/api/users/08df323e-589e-4a83-83bb-bcbbcc606849
Content-Type: application/json

{
  "username": "username_nou"
}

# 2. Verifică în ambele baze de date că username-ul s-a actualizat
```

### Test 2: DELETE User
```bash
# 1. Șterge user-ul din user-microservice
DELETE http://localhost:8081/api/users/08df323e-589e-4a83-83bb-bcbbcc606849

# 2. Verifică că:
#    - User-ul a fost șters din user-microservice
#    - User-ul a fost șters din device-microservice
#    - Toate device-urile asociate au fost șterse
```

---

## Verificare în baza de date

```sql
-- Verifică users în device-microservice
SELECT * FROM users;

-- Verifică device-urile
SELECT * FROM devices;

-- Verifică device-urile pentru un user specific
SELECT d.* FROM devices d
JOIN users u ON d.user_id = u.id
WHERE u.username = 'username_nou';
```

---

## Note importante

1. **Sincronizarea este asincronă**: Dacă device-microservice nu răspunde, operația principală (UPDATE/DELETE) va continua
2. **DELETE cascade**: Când ștergi un user, se șterg automat toate device-urile asociate
3. **Ambele microservicii trebuie să ruleze**: Pentru ca sincronizarea să funcționeze
4. **Verifică log-urile**: Dacă sincronizarea eșuează, vei vedea erori în log-uri

---

## Troubleshooting

### Problema: Sincronizarea nu funcționează
- Verifică că ambele microservicii rulează
- Verifică URL-ul în `application.properties`
- Verifică log-urile pentru erori

### Problema: Device-urile nu se șterg
- Verifică că relația `@ManyToOne` în `Device` este configurată corect
- Verifică că metoda `deleteByUser` din `DeviceRepository` funcționează
