package com.brenner.modern_java_crud.repository;

import com.brenner.modern_java_crud.domain.Project;
import com.brenner.modern_java_crud.domain.ProjectStatus;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    @Query("""
            SELECT COUNT(p)
            FROM Project p
            JOIN p.members m
            WHERE m.id = :memberId
              AND p.status NOT IN (:statuses)
               """)
    long countProjectsByMemberIdAndStatusNotIn(
        @Param("memberId") final Long memberId,
        @Param("statuses") final Set<ProjectStatus> statuses
    );

}
