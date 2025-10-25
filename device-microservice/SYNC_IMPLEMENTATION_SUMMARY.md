# Sincronizare Automată User-Microservice → Device-Microservice

## Problema Actuală
User-microservice trimite `id=null` în notificarea de sincronizare.

## Soluție - Verifică în User-Microservice

### 1. UserSyncDTO (în user-microservice)
```java
package ro.tuc.ds2025.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSyncDTO {
    private String id;
    private String username;
}
```

### 2. UserService (în user-microservice) - Metoda de sincronizare
```java
private void syncUserToDeviceMicroservice(User user) {
    try {
        // IMPORTANT: Asigură-te că user.getId() nu e null!
        UserSyncDTO syncDTO = new UserSyncDTO(user.getId(), user.getUsername());
        
        String url = deviceMicroserviceUrl + "/api/sync/users";
        restTemplate.postForEntity(url, syncDTO, Void.class);
        
        log.info("User synced to Device Microservice: {}", user.getId());
    } catch (Exception e) {
        log.error("Failed to sync user to Device Microservice: {} - Error: {}", 
                  user.getId(), e.getMessage());
    }
}
```

### 3. Apelează metoda DUPĂ salvare
```java
@Transactional
public UserDTO createUser(CreateUserDTO createUserDTO) {
    // ... validări ...
    
    User user = modelMapper.map(createUserDTO, User.class);
    user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));
    
    // SALVEAZĂ MAI ÎNTÂI (pentru a genera ID-ul)
    User savedUser = userRepository.save(user);
    
    // APOI sincronizează (acum savedUser.getId() există!)
    syncUserToDeviceMicroservice(savedUser);
    
    return convertToDTO(savedUser);
}
```

## Verificare Rapidă

### Test Manual
```bash
# 1. Creează user în user-microservice
POST http://localhost:8081/api/users
{
  "username": "test123",
  "password": "test123",
  "role": "CLIENT",
  "fullName": "Test User",
  "email": "test@energy.ro"
}

# 2. Verifică logurile user-microservice
# Ar trebui să vezi: "User synced to Device Microservice: {uuid}"

# 3. Verifică în baza de date device-microservice
SELECT * FROM users WHERE username = 'test123';
```

## Configurare Finală

### Device-Microservice - SecurityConfig.java
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/actuator/**").permitAll()
    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
    .requestMatchers("/api/sync/**").permitAll()  // IMPORTANT!
    .anyRequest().authenticated()
)
```

### User-Microservice - application.properties
```properties
# Pentru Docker
device.microservice.url=http://device-microservice:8082

# Pentru local testing
device.microservice.url=http://localhost:8082
```

### User-Microservice - docker-compose.yml
```yaml
environment:
  DEVICE_MICROSERVICE_URL: http://device-microservice:8082
```

## Troubleshooting

### Eroare: "id=null"
- Verifică că `syncUserToDeviceMicroservice()` e apelată DUPĂ `userRepository.save()`
- Verifică că folosești `savedUser` (nu `user`) când creezi `UserSyncDTO`

### Eroare: 403 Forbidden
- Verifică că `/api/sync/**` e în `.permitAll()` în SecurityConfig
- Rebuilduiește aplicația Docker

### Eroare: Connection Refused
- Verifică că ambele microservicii sunt în aceeași rețea Docker
- Sau folosește `http://host.docker.internal:8082` pentru servicii locale
