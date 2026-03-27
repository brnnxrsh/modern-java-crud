package com.brenner.modern_java_crud.dto;

import com.brenner.modern_java_crud.domain.MemberRole;

public record MemberExternalDto(
    Long id,
    String name,
    MemberRole role
) {}
