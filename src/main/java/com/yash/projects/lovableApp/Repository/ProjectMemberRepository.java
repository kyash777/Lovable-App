package com.yash.projects.lovableApp.Repository;



import com.yash.projects.lovableApp.entity.ProjectMember;
import com.yash.projects.lovableApp.entity.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    List<ProjectMember> findByIdProjectId(Long projectId);
}
