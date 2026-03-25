package com.brenner.modern_java_crud.dto;

import com.brenner.modern_java_crud.domain.ProjectStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ProjectUpdateDto(
    @NotBlank @Size(max = 150) String name,
    @Size(max = 500) String description,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startAt,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate expectedEndAt,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate endAt,
    @NotNull @DecimalMin(value = "0.01") BigDecimal totalBudget,
    @NotNull ProjectStatus status
) {}
