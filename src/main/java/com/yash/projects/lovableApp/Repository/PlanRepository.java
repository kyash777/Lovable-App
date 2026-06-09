package com.yash.projects.lovableApp.Repository;
import com.yash.projects.lovableApp.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}
