package ro.tuc.ds2025.auth.service;

import ro.tuc.ds2025.auth.dto.LoginRequestDTO;
import ro.tuc.ds2025.auth.dto.LoginResponseDTO;
import ro.tuc.ds2025.auth.dto.RegisterRequestDTO;
import ro.tuc.ds2025.auth.exception.ResourceNotFoundException;
import ro.tuc.ds2025.auth.exception.ValidationException;
import ro.tuc.ds2025.auth.model.AuthUser;
import ro.tuc.ds2025.auth.repository.AuthUserRepository;
import  ro.tuc.ds2025.auth.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        // Check if username already exists
        if (authUserRepository.existsByUsername(registerRequestDTO.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        // Check if email already exists
        if (authUserRepository.existsByEmail(registerRequestDTO.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        // Validate role
        if (!registerRequestDTO.getRole().equals("ADMIN") && !registerRequestDTO.getRole().equals("CLIENT")) {
            throw new ValidationException("Role must be either ADMIN or CLIENT");
        }

        // Create new user
        AuthUser authUser = new AuthUser();
        authUser.setUsername(registerRequestDTO.getUsername());
        authUser.setEmail(registerRequestDTO.getEmail());
        authUser.setPasswordHash(passwordEncoder.encode(registerRequestDTO.getPassword()));
        authUser.setRole(registerRequestDTO.getRole());

        AuthUser savedUser = authUserRepository.save(authUser);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole()
        );

        return new LoginResponseDTO(token, savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }


    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        // Find user by email
        AuthUser authUser = authUserRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), authUser.getPasswordHash())) {
            throw new ValidationException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(
                authUser.getId(),
                authUser.getUsername(),
                authUser.getRole()
        );

        return new LoginResponseDTO(token, authUser.getId(), authUser.getUsername(), authUser.getRole());
    }

    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

}
