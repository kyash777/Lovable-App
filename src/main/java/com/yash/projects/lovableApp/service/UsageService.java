package com.yash.projects.lovableApp.service;

import com.yash.projects.lovableApp.DTO.subscription.PlanLimitsResponse;
import com.yash.projects.lovableApp.DTO.subscription.UsageTodayResponse;

public interface UsageService {
    UsageTodayResponse getTodayUsageOfUser(Long userId);

    PlanLimitsResponse getCurrentSubscriptionLimitsOfUser(Long userId);
}
