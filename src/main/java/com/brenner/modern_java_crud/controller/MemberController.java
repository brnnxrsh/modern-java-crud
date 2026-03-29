package com.brenner.modern_java_crud.controller;

import com.brenner.modern_java_crud.dto.MemberExternalCreateDto;
import com.brenner.modern_java_crud.dto.MemberExternalDto;
import com.brenner.modern_java_crud.service.MemberService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService service;

    @GetMapping("/{id}")
    public MemberExternalDto findById(@PathVariable final Long id) {
        log.debug("[MEMBER-CONTROLLER] Buscando membro ID: {}", id);
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberExternalDto create(
        @RequestBody @Valid final MemberExternalCreateDto dto
    ) {
        log.debug("[MEMBER-CONTROLLER] Criando novo membro: {}", dto);
        return service.create(dto);
    }

}
