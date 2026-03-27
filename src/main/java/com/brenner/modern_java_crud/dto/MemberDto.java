package com.brenner.modern_java_crud.dto;

import jakarta.validation.constraints.NotNull;

public record MemberDto(
    @NotNull Long id
) {}
