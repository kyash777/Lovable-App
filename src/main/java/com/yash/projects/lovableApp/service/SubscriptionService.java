package com.yash.projects.lovableApp.service;

import com.yash.projects.lovableApp.DTO.subscription.CheckoutRequest;
import com.yash.projects.lovableApp.DTO.subscription.CheckoutResponse;
import com.yash.projects.lovableApp.DTO.subscription.PortalResponse;
import com.yash.projects.lovableApp.DTO.subscription.SubscriptionResponse;

public interface SubscriptionService {
    SubscriptionResponse getCurrentSubscription(Long userId);
}
