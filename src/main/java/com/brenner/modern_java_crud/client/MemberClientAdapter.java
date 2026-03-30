package com.brenner.modern_java_crud.client;

import com.brenner.modern_java_crud.dto.MemberExternalCreateDto;
import com.brenner.modern_java_crud.dto.MemberExternalDto;

import org.springframework.stereotype.Component;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberClientAdapter {

    private final MemberClient client;

    @Retry(name = "member-client")
    public MemberExternalDto findById(final Long id) {
        return client.findById(id);
    }

    @Retry(name = "member-client")
    public MemberExternalDto create(final MemberExternalCreateDto dto) {
        return client.create(dto);
    }

}
