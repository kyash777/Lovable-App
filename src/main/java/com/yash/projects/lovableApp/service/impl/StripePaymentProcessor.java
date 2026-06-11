package com.yash.projects.lovableApp.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.yash.projects.lovableApp.DTO.subscription.CheckoutRequest;
import com.yash.projects.lovableApp.DTO.subscription.CheckoutResponse;
import com.yash.projects.lovableApp.DTO.subscription.PortalResponse;
import com.yash.projects.lovableApp.Repository.PlanRepository;
import com.yash.projects.lovableApp.Repository.UserRepository;
import com.yash.projects.lovableApp.entity.Plan;
import com.yash.projects.lovableApp.entity.User;
import com.yash.projects.lovableApp.enums.SubscriptionStatus;
import com.yash.projects.lovableApp.errors.BadRequestException;
import com.yash.projects.lovableApp.errors.ResourceNotFoundException;
import com.yash.projects.lovableApp.security.AuthUtil;
import com.yash.projects.lovableApp.service.PaymentProcessor;
import com.yash.projects.lovableApp.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripePaymentProcessor implements PaymentProcessor {

    private final AuthUtil authUtil;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Value("${client.url}")
    private String frontendUrl;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
        // Verify Stripe is initialized
        if (Stripe.apiKey == null || Stripe.apiKey.isEmpty()) {
            log.error("Stripe API key is not initialized!");
            throw new RuntimeException("Stripe payment processor is not properly configured");
        }

        log.info("Stripe API Key is set: {}...", Stripe.apiKey.substring(0, Math.min(20, Stripe.apiKey.length())));

        Plan plan = planRepository.findById(request.planId()).orElseThrow(() ->
                new ResourceNotFoundException("Plan", request.planId().toString()));

        // Validate that plan has a valid stripe price id
        if(plan.getStripePriceId() == null || plan.getStripePriceId().trim().isEmpty()) {
            log.error("Plan with ID {} does not have a Stripe price ID configured", plan.getId());
            throw new IllegalArgumentException("Plan does not have a valid Stripe price ID. Please contact support.");
        }

        Long userId = authUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("user", userId.toString()));

        var params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(plan.getStripePriceId())
                                .setQuantity(1L)
                                .build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSubscriptionData(
                        new SessionCreateParams.SubscriptionData.Builder()
                                .setBillingMode(SessionCreateParams.SubscriptionData.BillingMode.builder()
                                        .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                                        .build())
                                .build()
                )
                .setSuccessUrl(frontendUrl + "/success.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/cancel.html")
                .putMetadata("user_id", userId.toString())
                .putMetadata("plan_id", plan.getId().toString());

        try {
            String stripeCustomerId = user.getStripeCustomerId();
            if(stripeCustomerId == null || stripeCustomerId.trim().isEmpty()) {
                params.setCustomerEmail(user.getUsername());
                log.info("Creating checkout session with customer email: {}", user.getUsername());
            } else {
                params.setCustomer(stripeCustomerId);
                log.info("Creating checkout session with existing Stripe customer: {}", stripeCustomerId);
            }

            log.info("Creating Stripe checkout session for plan: {} (ID: {}) with price: {}",
                    plan.getName(), plan.getId(), plan.getStripePriceId());

            Session session = Session.create(params.build());

            log.info("Checkout session created successfully: {}", session.getId());
            return new CheckoutResponse(session.getUrl());

        } catch (StripeException e) {
            log.error("Stripe API error - Code: {}, Message: {}, Request ID: {}",
                    e.getCode(), e.getMessage(), e.getRequestId());

            if("resource_missing".equals(e.getCode())) {
                String errorMsg = "The Stripe price ID for this plan no longer exists or is invalid. " +
                        "Price ID: " + plan.getStripePriceId() + ". Please contact support.";
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }


    @Override
    public PortalResponse openCustomerPortal() {
        Long userId = authUtil.getCurrentUserId();
        User user = getUser(userId);
        String stripeCustomerId = user.getStripeCustomerId();

        if(stripeCustomerId == null || stripeCustomerId.isEmpty()) {
            throw new BadRequestException("User does not have a Stripe Customer Id, UserId:"+userId);
        }

        try {
            var portalSession = com.stripe.model.billingportal.Session.create(
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(stripeCustomerId)
                            .setReturnUrl(frontendUrl)
                            .build()
            );

            return new PortalResponse(portalSession.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        log.debug("Handling stripe event: {}", type);

        switch (type) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted((Session) stripeObject, metadata); // one-time, on checkout completed
            case "customer.subscription.updated" -> handleCustomerSubscriptionUpdated((Subscription) stripeObject); // when user cancels, upgrades or any updates
            case "customer.subscription.deleted" -> handleCustomerSubscriptionDeleted((Subscription) stripeObject); // when subscription ends, revoke the access
            case "invoice.paid" -> handleInvoicePaid((Invoice) stripeObject); // when invoice is paid
            case "invoice.payment_failed" -> handleInvoicePaymentFailed((Invoice) stripeObject); // when invoice is not paid, mark as PAST_DUE
            default -> log.debug("Ignoring the event: {}", type);
        }
    }

    private void handleCheckoutSessionCompleted(Session session, Map<String, String> metadata) {
        if(session == null) {
            log.error("session object was null");
            return;
        }

        Long userId = Long.parseLong(metadata.get("user_id"));
        Long planId = Long.parseLong(metadata.get("plan_id"));

        String subscriptionId = session.getSubscription();
        String customerId = session.getCustomer();

        User user = getUser(userId);
        if(user.getStripeCustomerId() == null) {
            user.setStripeCustomerId(customerId);
            userRepository.save(user);
            log.error("Stripe id is set");
        }

        subscriptionService.activateSubscription(userId, planId, subscriptionId, customerId);
    }

    private void handleCustomerSubscriptionUpdated(Subscription subscription) {
        if (subscription == null) {
            log.error("subscription object was null inside handleCustomerSubscriptionUpdated");
            return;
        }

        SubscriptionStatus status = mapStripeStatusToEnum(subscription.getStatus());
        if (status == null) {
            log.warn("Unknown status '{}' for subscription {}", subscription.getStatus(), subscription.getId());
            return;
        }

        SubscriptionItem item = subscription.getItems().getData().get(0);
        Instant periodStart = toInstant(item.getCurrentPeriodStart());
        Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

        Long planId = resolvePlanId(item.getPrice());

        subscriptionService.updateSubscription(
                subscription.getId(), status, periodStart, periodEnd,
                subscription.getCancelAtPeriodEnd(), planId
        );

    }

    private void handleCustomerSubscriptionDeleted(Subscription subscription) {
        if (subscription == null) {
            log.error("subscription object was null inside handleCustomerSubscriptionDeleted");
            return;
        }
        subscriptionService.cancelSubscription(subscription.getId());
    }

    private void handleInvoicePaid(Invoice invoice) {
        String subId = extractSubscriptionId(invoice);
        if(subId == null) return;

        try {
            Subscription subscription = Subscription.retrieve(subId); //sdk calling the Stripe server
            var item = subscription.getItems().getData().get(0);

            Instant periodStart = toInstant(item.getCurrentPeriodStart());
            Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

            subscriptionService.renewSubscriptionPeriod(
                    subId,
                    periodStart,
                    periodEnd
            );

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }

    private void handleInvoicePaymentFailed(Invoice invoice) {
        String subId = extractSubscriptionId(invoice);
        if(subId == null) return;

        subscriptionService.markSubscriptionPastDue(subId);
    }


    /// // Utility Methods

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("user", userId.toString()));
    }

    private SubscriptionStatus mapStripeStatusToEnum(String status) {
        return switch (status) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due", "unpaid", "paused", "incomplete_expired" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELED;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            default -> {
                log.warn("Unmapped Stripe status: {}", status);
                yield null;
            }
        };
    }

    private Instant toInstant(Long epoch) {
        return epoch != null ? Instant.ofEpochSecond(epoch) : null;
    }

    private Long resolvePlanId(Price price) {
        if (price == null || price.getId() == null) return null;
        return planRepository.findByStripePriceId(price.getId())
                .map(Plan::getId)
                .orElse(null);
    }

    private String extractSubscriptionId(Invoice invoice) {
        var parent = invoice.getParent();
        if (parent == null) return null;

        var subDetails = parent.getSubscriptionDetails();
        if (subDetails == null) return null;

        return subDetails.getSubscription();
    }
}




















