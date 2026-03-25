package com.brenner.modern_java_crud.dto;

import com.brenner.modern_java_crud.domain.ProjectStatus;
import com.brenner.modern_java_crud.domain.RiskLevel;

import java.time.LocalDate;

public record ProjectFilterDto(
    String name,
    ProjectStatus status,
    LocalDate startAt,
    LocalDate endAt,
    RiskLevel riskLevel
) {}
