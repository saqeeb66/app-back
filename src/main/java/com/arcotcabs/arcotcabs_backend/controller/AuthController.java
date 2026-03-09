package com.arcotcabs.arcotcabs_backend.controller;


import com.arcotcabs.arcotcabs_backend.dto.AuthResponse;
import com.arcotcabs.arcotcabs_backend.dto.LoginRequest;
import com.arcotcabs.arcotcabs_backend.dto.SignupRequest;
import com.arcotcabs.arcotcabs_backend.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @RestController
    @RequestMapping("/api/test")
    @CrossOrigin(origins = "*")
    public class TestController {

        @GetMapping("/ping")
        public String ping() {
            System.out.println("🔥 BACKEND PING HIT");
            return "BACKEND CONNECTED";
        }
    }

}

