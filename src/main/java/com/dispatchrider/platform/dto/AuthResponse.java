package com.dispatchrider.platform.dto;

import com.dispatchrider.platform.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private Role role;
}
