package ro.tuc.ds2025.users.service;

import ro.tuc.ds2025.users.dto.CreateUserDTO;
import ro.tuc.ds2025.users.dto.UpdateUserDTO;
import ro.tuc.ds2025.users.dto.UserDTO;
import ro.tuc.ds2025.users.dto.UserSyncDTO;
import ro.tuc.ds2025.users.exception.ResourceNotFoundException;
import ro.tuc.ds2025.users.exception.ValidationException;
import ro.tuc.ds2025.users.model.User;
import ro.tuc.ds2025.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/*
@Service ii spune containerului "clasa asta contine logica de bussiness fami un bean din ea"
@RequiredArgsConstructor lombok genereaza constructorul cu toate campurile final injectare fara autowireTransactional
@Transactional incepe/finalizeaza tranzatile hiberanate/jpa readOnly=true

*/
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    
    @Value("${device.microservice.url}")
    private String deviceMicroserviceUrl;

    @Transactional(readOnly = true)
    //list all users
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> modelMapper.map(u, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return modelMapper.map(user, UserDTO.class);
    }

     // CRUDS ==>
    @Transactional
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        // Validate unique username
        if (userRepository.existsByUsername(createUserDTO.getUsername())) {
            throw new ValidationException("Username already exists: " + createUserDTO.getUsername());
        }

        // Validate unique email
        if (userRepository.existsByEmail(createUserDTO.getEmail())) {
            throw new ValidationException("Email already exists: " + createUserDTO.getEmail());
        }

        User user = modelMapper.map(createUserDTO, User.class);

        // Encode password
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));

        User savedUser = userRepository.save(user);
        
        // Sincronizare cu Device Microservice
        syncUserToDeviceMicroservice(savedUser);
        
        return modelMapper.map(savedUser, UserDTO.class);
    }
    
    private void syncUserToDeviceMicroservice(User user) {
        try {
            UserSyncDTO syncDTO = new UserSyncDTO(user.getId(), user.getUsername());
            restTemplate.postForEntity(
                deviceMicroserviceUrl + "/api/sync/users",
                syncDTO,
                Void.class
            );
            log.info("User synced to Device Microservice: {} - {}", user.getId(), user.getUsername());
        } catch (Exception e) {
            log.error("Failed to sync user to Device Microservice: {} - Error: {}", 
                user.getId(), e.getMessage());
            // Nu aruncăm excepție - sincronizarea e opțională
        }
    }

    @Transactional
    public UserDTO updateUser(UUID id, UpdateUserDTO updateUserDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update only non-null fields
        if (updateUserDTO.getUsername() != null) {
            if (userRepository.existsByUsername(updateUserDTO.getUsername()) &&
                    !user.getUsername().equals(updateUserDTO.getUsername())) {
                throw new ValidationException("Username already exists: " + updateUserDTO.getUsername());
            }
            user.setUsername(updateUserDTO.getUsername());
        }

        if (updateUserDTO.getEmail() != null) {
            if (userRepository.existsByEmail(updateUserDTO.getEmail()) &&
                    !user.getEmail().equals(updateUserDTO.getEmail())) {
                throw new ValidationException("Email already exists: " + updateUserDTO.getEmail());
            }
            user.setEmail(updateUserDTO.getEmail());
        }

        if (updateUserDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateUserDTO.getPassword()));
        }

        if (updateUserDTO.getRole() != null) {
            user.setRole(updateUserDTO.getRole());
        }

        if (updateUserDTO.getFullName() != null) {
            user.setFullName(updateUserDTO.getFullName());
        }

        User updatedUser = userRepository.save(user);
        
        // Sincronizare UPDATE cu Device Microservice
        syncUserToDeviceMicroservice(updatedUser);
        
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        
        // Sincronizare DELETE cu Device Microservice (înainte de ștergere)
        deleteUserFromDeviceMicroservice(id);
        
        userRepository.deleteById(id);
    }
    
    private void deleteUserFromDeviceMicroservice(UUID userId) {
        try {
            restTemplate.delete(deviceMicroserviceUrl + "/api/sync/users/" + userId);
            log.info("User deleted from Device Microservice: {}", userId);
        } catch (Exception e) {
            log.error("Failed to delete user from Device Microservice: {} - Error: {}", 
                userId, e.getMessage());
            // Nu aruncăm excepție - sincronizarea e opțională
        }
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return modelMapper.map(user, UserDTO.class);
    }





}
