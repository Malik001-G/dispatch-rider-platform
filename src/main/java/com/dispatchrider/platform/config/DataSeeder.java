package com.dispatchrider.platform.config;

import com.dispatchrider.platform.entity.Hub;
import com.dispatchrider.platform.entity.Role;
import com.dispatchrider.platform.entity.User;
import com.dispatchrider.platform.repository.HubRepository;
import com.dispatchrider.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the Tier 1 hub (BRD section 5) and a default admin account on first boot, so the
 * platform is immediately usable without a manual setup step - matches NFR-2 (low-friction
 * pilot deployment).
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final HubRepository hubRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.hub.default-name}") private String defaultHubName;
    @Value("${app.hub.default-center-lat}") private double defaultCenterLat;
    @Value("${app.hub.default-center-lng}") private double defaultCenterLng;
    @Value("${app.hub.default-radius-km}") private double defaultRadiusKm;

    @Override
    public void run(String... args) {
        if (hubRepository.count() == 0) {
            hubRepository.save(Hub.builder()
                    .name(defaultHubName)
                    .centerLat(defaultCenterLat)
                    .centerLng(defaultCenterLng)
                    .radiusKm(defaultRadiusKm)
                    .active(true)
                    .build());
        }

        if (!userRepository.existsByPhoneNumber("00000000000")) {
            userRepository.save(User.builder()
                    .name("Platform Admin")
                    .phoneNumber("00000000000")
                    .passwordHash(passwordEncoder.encode("changeme"))
                    .role(Role.ADMIN)
                    .build());
            // Default admin login: 00000000000 / changeme - CHANGE IMMEDIATELY after first deploy.
        }
    }
}
