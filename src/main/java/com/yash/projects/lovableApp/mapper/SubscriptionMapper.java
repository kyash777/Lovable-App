package com.yash.projects.lovableApp.mapper;


import com.yash.projects.lovableApp.DTO.subscription.PlanResponse;
import com.yash.projects.lovableApp.DTO.subscription.SubscriptionResponse;
import com.yash.projects.lovableApp.entity.Plan;
import com.yash.projects.lovableApp.entity.Subscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionResponse toSubscriptionResponse(Subscription subscription);

    PlanResponse toPlanResponse(Plan plan);
}
