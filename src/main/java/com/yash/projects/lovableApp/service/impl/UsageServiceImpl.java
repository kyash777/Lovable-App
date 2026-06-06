package com.yash.projects.lovableApp.service.impl;

import com.yash.projects.lovableApp.DTO.subscription.PlanLimitsResponse;
import com.yash.projects.lovableApp.DTO.subscription.UsageTodayResponse;
import com.yash.projects.lovableApp.service.UsageService;
import org.springframework.stereotype.Service;

@Service
public class UsageServiceImpl implements UsageService {
    @Override
    public UsageTodayResponse getTodayUsageOfUser(Long userId) {
        return null;
    }

    @Override
    public PlanLimitsResponse getCurrentSubscriptionLimitsOfUser(Long userId) {
        return null;
    }
}
