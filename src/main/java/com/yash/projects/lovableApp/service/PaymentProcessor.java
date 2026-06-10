package com.yash.projects.lovableApp.service;
import com.stripe.model.StripeObject;
import com.yash.projects.lovableApp.DTO.subscription.CheckoutRequest;
import com.yash.projects.lovableApp.DTO.subscription.CheckoutResponse;
import com.yash.projects.lovableApp.DTO.subscription.PortalResponse;

import java.util.Map;

public interface PaymentProcessor {

    CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request);

    PortalResponse openCustomerPortal();

    void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata);
}
