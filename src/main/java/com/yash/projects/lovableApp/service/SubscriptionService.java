package com.yash.projects.lovableApp.service;

import com.yash.projects.lovableApp.DTO.subscription.SubscriptionResponse;
import com.yash.projects.lovableApp.enums.SubscriptionStatus;

import java.time.Instant;

public interface SubscriptionService {
    SubscriptionResponse getCurrentSubscription();

    void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId);

    void updateSubscription(String subscriptionId, SubscriptionStatus status, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId);

    void cancelSubscription(String subscriptionId);

    void renewSubscriptionPeriod(String subscriptionId, Instant periodStart, Instant periodEnd);

    void markSubscriptionPastDue(String subscriptionId);

    boolean canCreateNewProject();
}
