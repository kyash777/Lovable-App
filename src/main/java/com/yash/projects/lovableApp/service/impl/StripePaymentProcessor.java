package com.yash.projects.lovableApp.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.yash.projects.lovableApp.DTO.subscription.CheckoutRequest;
import com.yash.projects.lovableApp.DTO.subscription.CheckoutResponse;
import com.yash.projects.lovableApp.DTO.subscription.PortalResponse;
import com.yash.projects.lovableApp.Repository.PlanRepository;
import com.yash.projects.lovableApp.Repository.UserRepository;
import com.yash.projects.lovableApp.entity.Plan;
import com.yash.projects.lovableApp.entity.User;
import com.yash.projects.lovableApp.errors.ResourceNotFoundException;
import com.yash.projects.lovableApp.security.AuthUtil;
import com.yash.projects.lovableApp.service.PaymentProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripePaymentProcessor implements PaymentProcessor {

    private final AuthUtil authUtil;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

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
    public PortalResponse openCustomerPortal(Long userId) {
        return null;
    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        log.info("Webhook event received - Type: {}", type);
    }
}




















