package com.dispatchrider.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // needed for FR-7b-i auto-refund/cancel job
public class DispatchRiderPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(DispatchRiderPlatformApplication.class, args);
    }
}
