package com.yash.projects.lovableApp.service;

import com.yash.projects.lovableApp.DTO.subscription.PlanResponse;

import java.util.List;

public interface PlanService {
    List<PlanResponse> getAllActivePlans();
}

