package com.brenner.modern_java_crud.repository.projection;

import com.brenner.modern_java_crud.domain.ProjectStatus;

import java.math.BigDecimal;

public record ProjectStatusMetricsProjection(
    ProjectStatus status,
    Long count,
    BigDecimal totalBudget
) {}
