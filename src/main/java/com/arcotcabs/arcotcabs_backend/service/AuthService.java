package com.arcotcabs.arcotcabs_backend.service;


import com.arcotcabs.arcotcabs_backend.dto.AuthResponse;
import com.arcotcabs.arcotcabs_backend.dto.LoginRequest;
import com.arcotcabs.arcotcabs_backend.dto.SignupRequest;
import com.arcotcabs.arcotcabs_backend.model.User;
import com.arcotcabs.arcotcabs_backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.arcotcabs.arcotcabs_backend.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final com.arcotcabs.arcotcabs_backend.security.JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse signup(SignupRequest request) {

        String passwordHash = encoder.encode(request.getPassword());

        User user = new User();
        user.setUserId(request.getEmail());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordHash);
        user.setRole("USER");
        user.setCreatedAt(System.currentTimeMillis());

        userRepository.save(user);

        String token =
                jwtUtil.generateToken(user.getUserId(), user.getRole());

        return new AuthResponse(
                user.getUserId(),
                user.getRole(),
                token
        );
    }

    public AuthResponse login(LoginRequest request) {

        User user =
                userRepository.findByUserId(request.getEmail());

        if (user == null ||
                !encoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                )) {
            throw new RuntimeException("Invalid email or password");
        }

        String token =
                jwtUtil.generateToken(user.getUserId(), user.getRole());

        return new AuthResponse(
                user.getUserId(),
                user.getRole(),
                token
        );
    }
}
