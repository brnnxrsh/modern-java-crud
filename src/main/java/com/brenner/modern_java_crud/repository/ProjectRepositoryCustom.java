package com.brenner.modern_java_crud.repository;

import com.brenner.modern_java_crud.domain.Project;
import com.brenner.modern_java_crud.repository.projection.ProjectStatusMetricsProjection;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

public interface ProjectRepositoryCustom {

    List<ProjectStatusMetricsProjection> getMetricsByStatus(
        Specification<Project> spec
    );

    Double getAverageDurationInDays(Specification<Project> spec);

    Long countUniqueMembersAllocated(Specification<Project> spec);

    Long countUniqueManagersAllocated(Specification<Project> spec);

}
