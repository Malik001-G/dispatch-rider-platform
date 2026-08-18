package com.dispatchrider.platform.service;

import com.dispatchrider.platform.dto.*;
import com.dispatchrider.platform.entity.*;
import com.dispatchrider.platform.exception.ApiException;
import com.dispatchrider.platform.repository.OrderRepository;
import com.dispatchrider.platform.repository.RiderProfileRepository;
import com.dispatchrider.platform.repository.StopRepository;
import com.dispatchrider.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StopRepository stopRepository;
    private final UserRepository userRepository;
    private final RiderProfileRepository riderProfileRepository;
    private final HubService hubService;
    private final PricingService pricingService;
    private final AssignmentService assignmentService;
    private final PaystackService paystackService;
    private final SecureRandom random = new SecureRandom();

    // ---------- FR-4, FR-5, FR-6, FR-7 (initial estimate) ----------

    @Transactional
    public Order createOrder(User sender, CreateOrderRequest req) {
        Hub hub = hubService.getActiveHub();

        if (req.getPickupLat() == null || req.getPickupLng() == null) {
            throw ApiException.badRequest("Pickup coordinates are required");
        }
        // FR-6: validate pickup + every stop is within the hub's service radius before submission
        if (!hubService.isWithinCoverage(hub, req.getPickupLat(), req.getPickupLng())) {
            throw ApiException.badRequest("Pickup address is outside the coverage zone (" + hub.getName() + ")");
        }
        for (CreateStopRequest s : req.getStops()) {
            if (s.getLat() == null || s.getLng() == null) {
                throw ApiException.badRequest("Coordinates are required for stop: " + s.getAddress());
            }
            if (!hubService.isWithinCoverage(hub, s.getLat(), s.getLng())) {
                throw ApiException.badRequest("Stop out of coverage zone (" + hub.getName() + "): " + s.getAddress());
            }
        }

        Order order = Order.builder()
                .sender(sender)
                .hub(hub)
                .pickupAddress(req.getPickupAddress())
                .pickupLat(req.getPickupLat())
                .pickupLng(req.getPickupLng())
                .status(OrderStatus.PENDING_PAYMENT)
                .pickupFeeNaira(pricingService.getPickupFeeNaira())
                .totalFeeNaira(0) // computed below
                .createdAt(Instant.now())
                .build();

        List<Stop> stops = new java.util.ArrayList<>();
        for (int i = 0; i < req.getStops().size(); i++) {
            CreateStopRequest s = req.getStops().get(i);
            stops.add(Stop.builder()
                    .order(order)
                    .sequenceEntered(i)
                    .recipientName(s.getRecipientName())
                    .recipientPhone(s.getRecipientPhone())
                    .address(s.getAddress())
                    .lat(s.getLat())
                    .lng(s.getLng())
                    .itemDescription(s.getItemDescription())
                    .status(StopStatus.PENDING)
                    .build());
        }

        // Price using the initial nearest-neighbor suggestion so the sender sees a real estimate
        // up front; this gets recomputed at assignment time (FR-7a) in case admin overrides it.
        List<Stop> priced = assignmentService.sequenceAndPrice(stops, order.getPickupLat(), order.getPickupLng());
        order.setStops(priced);

        int total = order.getPickupFeeNaira() + priced.stream().mapToInt(Stop::getFeeNaira).sum();
        order.setTotalFeeNaira(total);

        return orderRepository.save(order);
    }

    // ---------- FR-7b, FR-7c: payment initiation ----------

    @Transactional
    public Map<String, Object> initiatePayment(Long orderId, User sender) {
        Order order = getOwnedOrder(orderId, sender);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw ApiException.conflict("Order is not awaiting payment");
        }

        // FR-7b: block payment entirely if no rider in the hub is currently ACTIVE
        boolean anyActiveRider = riderProfileRepository.existsByHubIdAndStatus(order.getHub().getId(), RiderStatus.ACTIVE);
        if (!anyActiveRider) {
            throw ApiException.conflict("No riders available right now - please try again shortly");
        }

        String reference = "ORDER-" + order.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        order.setPaystackReference(reference);
        orderRepository.save(order);

        return paystackService.initializeTransaction(sender.getPhoneNumber() + "@dispatchrider.local",
                order.getTotalFeeNaira(), reference);
    }

    // ---------- FR-7d, FR-13: payment webhook confirmation ----------

    @Transactional
    public void confirmPayment(String paystackReference) {
        Order order = orderRepository.findAll().stream()
                .filter(o -> paystackReference.equals(o.getPaystackReference()))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("No order for reference " + paystackReference));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return; // idempotent - webhook may fire more than once
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(Instant.now());

        // FR-13: generate a unique OTP per stop on payment confirmation
        for (Stop stop : order.getStops()) {
            stop.setOtpCode(generateOtp());
        }

        orderRepository.save(order);
    }

    private String generateOtp() {
        return String.format("%04d", random.nextInt(10000));
    }

    // ---------- FR-9, FR-7a: assignment ----------

    @Transactional
    public Order assignRider(Long orderId, AssignRiderRequest req) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        if (order.getStatus() != OrderStatus.PAID) {
            throw ApiException.conflict("Order must be PAID before assignment");
        }

        User rider = userRepository.findById(req.getRiderUserId())
                .orElseThrow(() -> ApiException.notFound("Rider not found"));
        if (rider.getRole() != Role.RIDER) {
            throw ApiException.badRequest("User is not a rider");
        }
        RiderProfile profile = riderProfileRepository.findByUserId(rider.getId())
                .orElseThrow(() -> ApiException.notFound("Rider profile not found"));
        if (profile.getStatus() != RiderStatus.ACTIVE) {
            throw ApiException.conflict("Rider is not currently ACTIVE");
        }

        // FR-7a: re-sequence/re-price, honoring an admin override if provided
        List<Stop> sequenced = (req.getStopIdSequenceOverride() == null || req.getStopIdSequenceOverride().isEmpty())
                ? assignmentService.sequenceAndPrice(order.getStops(), order.getPickupLat(), order.getPickupLng())
                : assignmentService.applyOverrideSequence(order.getStops(), req.getStopIdSequenceOverride(),
                        order.getPickupLat(), order.getPickupLng());
        order.setStops(sequenced);

        order.setAssignedRider(rider);
        order.setStatus(OrderStatus.ASSIGNED);
        order.setAssignedAt(Instant.now());

        // FR-9a: system sets rider BUSY on assignment
        profile.setStatus(RiderStatus.BUSY);
        riderProfileRepository.save(profile);

        return orderRepository.save(order);
    }

    // ---------- FR-11, FR-12, FR-13: stop status updates by the assigned rider ----------

    @Transactional
    public Order updateStopStatus(Long stopId, StopStatusUpdateRequest req, User rider) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> ApiException.notFound("Stop not found"));
        Order order = stop.getOrder();

        if (order.getAssignedRider() == null || !order.getAssignedRider().getId().equals(rider.getId())) {
            throw ApiException.forbidden("This stop is not assigned to you");
        }

        if (req.getNewStatus() == StopStatus.DELIVERED) {
            if (req.getOtp() == null || !req.getOtp().equals(stop.getOtpCode())) {
                throw ApiException.badRequest("Incorrect OTP");
            }
            stop.setOtpVerifiedAt(Instant.now());
            stop.setDeliveredAt(Instant.now());
        } else if (req.getNewStatus() == StopStatus.PICKED_UP) {
            stop.setPickedUpAt(Instant.now());
        } else if (req.getNewStatus() == StopStatus.FAILED) {
            // FR-12: a failed stop doesn't block the rest of the order.
            // FR-7f: refund that stop's fee only; admin processes the actual refund manually in
            // the pilot phase, this just flags it as refund-pending.
            stop.setFailedAt(Instant.now());
        }

        stop.setStatus(req.getNewStatus());
        stopRepository.save(stop);

        maybeCompleteOrder(order);

        return orderRepository.findById(order.getId()).orElseThrow();
    }

    /** FR-9a: once every stop is DELIVERED or FAILED, order completes and rider returns to ACTIVE. */
    private void maybeCompleteOrder(Order order) {
        boolean allTerminal = order.getStops().stream()
                .allMatch(s -> s.getStatus() == StopStatus.DELIVERED || s.getStatus() == StopStatus.FAILED);

        if (allTerminal && order.getStatus() == OrderStatus.ASSIGNED) {
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(Instant.now());
            orderRepository.save(order);

            RiderProfile profile = riderProfileRepository.findByUserId(order.getAssignedRider().getId())
                    .orElseThrow();
            profile.setStatus(RiderStatus.ACTIVE);
            riderProfileRepository.save(profile);
        }
    }

    // ---------- FR-7g: sender cancellation ----------

    @Transactional
    public void cancelOrder(Long orderId, User sender) {
        Order order = getOwnedOrder(orderId, sender);

        if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(Instant.now());
            orderRepository.save(order);
            return;
        }

        if (order.getStatus() == OrderStatus.PAID) {
            // Full refund - no rider assigned yet
            paystackService.refund(order.getPaystackReference(), null);
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(Instant.now());
            orderRepository.save(order);
            return;
        }

        // FR-7g: once a rider is assigned, cancellation requires admin intervention
        throw ApiException.conflict("Rider already assigned - cancellation now requires admin approval");
    }

    /** FR-13: sender's own order view, with all stop OTPs shown together so they can forward them at once. */
    public Order getOrderForSender(Long orderId, User sender) {
        return getOwnedOrder(orderId, sender);
    }

    public List<Order> getOrdersForSender(User sender) {
        return orderRepository.findBySenderId(sender.getId());
    }

    // ---------- helpers ----------

    private Order getOwnedOrder(Long orderId, User sender) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!order.getSender().getId().equals(sender.getId())) {
            throw ApiException.forbidden("Not your order");
        }
        return order;
    }
}
