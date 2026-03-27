package com.brenner.modern_java_crud.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ProjectCreateDto(
    @NotBlank @Size(max = 150) String name,
    @Size(max = 500) String description,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startAt,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate expectedEndAt,
    @NotNull @DecimalMin(value = "0.01") BigDecimal totalBudget,
    @NotNull @Valid MemberDto manager,
    @Size(min = 1, max = 10) @Valid Set<MemberDto> members
) {}
