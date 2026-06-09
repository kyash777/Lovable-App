package com.yash.projects.lovableApp.config;

import com.stripe.Stripe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
public class PaymentConfig {

    @Value("${stripe.api.secret}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        if (stripeSecretKey == null || stripeSecretKey.isEmpty()) {
            log.error("Stripe secret key is not configured!");
            throw new IllegalArgumentException("Stripe secret key must be configured in application.yaml");
        }

        Stripe.apiKey = stripeSecretKey;
        log.info("Stripe API initialized successfully with key: {}...",
                stripeSecretKey.substring(0, Math.min(20, stripeSecretKey.length())));
    }
}

