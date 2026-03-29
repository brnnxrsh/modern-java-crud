package com.brenner.modern_java_crud.dto;

import com.brenner.modern_java_crud.domain.ProjectStatus;
import com.brenner.modern_java_crud.domain.RiskLevel;

import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Builder;

@Builder
public record ProjectFilterDto(
    Set<Long> managerIds,
    Set<Long> memberIds,
    String name,
    Set<ProjectStatus> statuses,
    LocalDate startAt,
    LocalDate endAt,
    Set<RiskLevel> riskLevels
) {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ProjectFilterDto {}

}
