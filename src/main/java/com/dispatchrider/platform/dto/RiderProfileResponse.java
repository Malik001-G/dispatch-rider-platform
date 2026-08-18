package com.dispatchrider.platform.dto;

import com.dispatchrider.platform.entity.RiderApprovalStatus;
import com.dispatchrider.platform.entity.RiderProfile;
import com.dispatchrider.platform.entity.RiderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Returned instead of the raw {@link RiderProfile} entity - serializing the entity directly
 * would trip Jackson on its lazy-loaded User/Hub associations outside a Hibernate session.
 */
@Data
@AllArgsConstructor
public class RiderProfileResponse {
    private Long riderUserId;
    private String riderName;
    private String riderPhone;
    private RiderApprovalStatus approvalStatus;
    private RiderStatus status;

    public static RiderProfileResponse from(RiderProfile p) {
        return new RiderProfileResponse(
                p.getUser().getId(),
                p.getUser().getName(),
                p.getUser().getPhoneNumber(),
                p.getApprovalStatus(),
                p.getStatus()
        );
    }
}
