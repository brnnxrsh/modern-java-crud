package com.brenner.modern_java_crud.dto;

import com.brenner.modern_java_crud.repository.projection.ProjectStatusMetricsProjection;

import java.util.List;

public record ReportDto(
    List<ProjectStatusMetricsProjection> statusMetrics,
    Double averageDurationInDays,
    Long countUniqueMembersAllocated,
    Long countUniqueManagersAllocated
) {

    public ReportDto {
        averageDurationInDays = averageDurationInDays == null ? 0.0
            : averageDurationInDays;
    }

}
