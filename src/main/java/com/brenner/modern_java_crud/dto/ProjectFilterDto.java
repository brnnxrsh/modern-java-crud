package com.brenner.modern_java_crud.dto;

import com.brenner.modern_java_crud.domain.ProjectStatus;
import com.brenner.modern_java_crud.domain.RiskLevel;

import java.time.LocalDate;
import java.util.Set;

public record ProjectFilterDto(
    Set<Long> managerIds,
    Set<Long> memberIds,
    String name,
    Set<ProjectStatus> statuses,
    LocalDate startAt,
    LocalDate endAt,
    Set<RiskLevel> riskLevels
) {}
