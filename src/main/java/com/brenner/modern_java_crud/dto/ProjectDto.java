package com.brenner.modern_java_crud.dto;

import com.brenner.modern_java_crud.domain.ProjectStatus;
import com.brenner.modern_java_crud.domain.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectDto(
    Long id,
    String name,
    String description,
    LocalDate startAt,
    LocalDate expectedEndAt,
    BigDecimal totalBudget,
    ProjectStatus status,
    RiskLevel riskLevel,
    Integer durationMonths
) {}
