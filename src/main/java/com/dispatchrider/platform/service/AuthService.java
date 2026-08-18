package com.dispatchrider.platform.service;

import com.dispatchrider.platform.dto.*;
import com.dispatchrider.platform.entity.*;
import com.dispatchrider.platform.exception.ApiException;
import com.dispatchrider.platform.repository.RiderProfileRepository;
import com.dispatchrider.platform.repository.UserRepository;
import com.dispatchrider.platform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RiderProfileRepository riderProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final HubService hubService;

    /**
     * FR-1. Registration eligibility is geography-based: the shop location must fall within
     * the active hub's coverage radius (same rule riders are held to).
     */
    @Transactional
    public AuthResponse registerBusinessOwner(RegisterBusinessOwnerRequest req) {
        if (userRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw ApiException.conflict("Phone number already registered");
        }

        Hub hub = hubService.getActiveHub();
        if (req.getShopLat() != null && req.getShopLng() != null
                && !hubService.isWithinCoverage(hub, req.getShopLat(), req.getShopLng())) {
            throw ApiException.badRequest("Shop location is outside the current coverage zone (" + hub.getName() + ")");
        }

        User user = User.builder()
                .name(req.getName())
                .phoneNumber(req.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(Role.BUSINESS_OWNER)
                .shopLocationAddress(req.getShopLocationAddress())
                .shopLat(req.getShopLat())
                .shopLng(req.getShopLng())
                .hub(hub)
                .build();
        user = userRepository.save(user);

        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getRole().name()), user.getId(), user.getRole());
    }

    /**
     * FR-2. Rider account is created immediately but stays PENDING_REVIEW / OFFLINE until an
     * admin approves it - riders cannot self-activate before approval.
     */
    @Transactional
    public AuthResponse registerRider(RegisterRiderRequest req) {
        if (userRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw ApiException.conflict("Phone number already registered");
        }

        Hub hub = hubService.getActiveHub();

        User user = User.builder()
                .name(req.getName())
                .phoneNumber(req.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(Role.RIDER)
                .hub(hub)
                .build();
        user = userRepository.save(user);

        RiderProfile profile = RiderProfile.builder()
                .user(user)
                .hub(hub)
                .idDocumentUrl(req.getIdDocumentUrl())
                .approvalStatus(RiderApprovalStatus.PENDING_REVIEW)
                .status(RiderStatus.OFFLINE)
                .build();
        riderProfileRepository.save(profile);

        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getRole().name()), user.getId(), user.getRole());
    }

    /** FR-3 */
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByPhoneNumber(req.getPhoneNumber())
                .orElseThrow(() -> ApiException.badRequest("Invalid phone number or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Invalid phone number or password");
        }

        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getRole().name()), user.getId(), user.getRole());
    }
}
