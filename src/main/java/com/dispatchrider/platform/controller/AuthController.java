package com.dispatchrider.platform.controller;

import com.dispatchrider.platform.dto.*;
import com.dispatchrider.platform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/business-owner") // FR-1
    public AuthResponse registerBusinessOwner(@Valid @RequestBody RegisterBusinessOwnerRequest req) {
        return authService.registerBusinessOwner(req);
    }

    @PostMapping("/register/rider") // FR-2
    public AuthResponse registerRider(@Valid @RequestBody RegisterRiderRequest req) {
        return authService.registerRider(req);
    }

    @PostMapping("/login") // FR-3
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
