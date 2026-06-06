package com.yash.projects.lovableApp.service.impl;

import com.yash.projects.lovableApp.DTO.subscription.PlanResponse;
import com.yash.projects.lovableApp.service.PlanService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanServiceImpl implements PlanService {
    @Override
    public List<PlanResponse> getAllActivePlans() {
        return List.of();
    }
}
