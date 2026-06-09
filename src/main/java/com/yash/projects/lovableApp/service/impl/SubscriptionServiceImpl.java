package com.yash.projects.lovableApp.service.impl;

import com.yash.projects.lovableApp.DTO.subscription.CheckoutRequest;
import com.yash.projects.lovableApp.DTO.subscription.CheckoutResponse;
import com.yash.projects.lovableApp.DTO.subscription.PortalResponse;
import com.yash.projects.lovableApp.DTO.subscription.SubscriptionResponse;
import com.yash.projects.lovableApp.service.SubscriptionService;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    @Override
    public SubscriptionResponse getCurrentSubscription(Long userId) {
        return null;
    }

}
