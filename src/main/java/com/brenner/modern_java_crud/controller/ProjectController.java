package com.brenner.modern_java_crud.controller;

import com.brenner.modern_java_crud.dto.ProjectCreateDto;
import com.brenner.modern_java_crud.dto.ProjectDto;
import com.brenner.modern_java_crud.dto.ProjectFilterDto;
import com.brenner.modern_java_crud.dto.ProjectNextStepDto;
import com.brenner.modern_java_crud.dto.ProjectUpdateDto;
import com.brenner.modern_java_crud.service.ProjectService;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService service;

    @GetMapping
    public Page<ProjectDto> findAll(
        @ParameterObject final ProjectFilterDto filter,
        @ParameterObject final Pageable pageable
    ) {
        log.debug(
            "[PROJECT-CONTROLLER] Listando projetos para os filtros {} e paginação {}",
            filter,
            pageable
        );

        return service.findAll(filter, pageable);
    }

    @GetMapping("/{id}")
    public ProjectDto find(@PathVariable final Long id) {
        log.debug(
            "[PROJECT-CONTROLLER] Buscando detalhes do projeto ID: {}",
            id
        );

        return service.find(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto create(@RequestBody @Valid final ProjectCreateDto dto) {
        log.debug("[PROJECT-CONTROLLER] Criando novo projeto {}", dto);

        return service.create(dto);
    }

    @PutMapping("/{id}")
    public ProjectDto update(
        @PathVariable final Long id,
        @RequestBody @Valid final ProjectUpdateDto dto
    ) {
        log.debug(
            "[PROJECT-CONTROLLER] Atualizando projeto ID {} com os dados {}",
            id,
            dto
        );

        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final Long id) {
        log.debug("[PROJECT-CONTROLLER] Excluindo projeto ID {}", id);

        service.delete(id);
    }

    @PatchMapping("/{id}/next-step")
    public ProjectDto advanceStep(
        @PathVariable final Long id,
        @RequestBody @Valid final ProjectNextStepDto dto
    ) {
        log.debug(
            "[PROJECT-CONTROLLER] Avançando etapa do projeto ID {} com os dados {}",
            id,
            dto
        );

        return service.advanceStep(id, dto);
    }

    @PatchMapping("/{id}/cancel")
    public ProjectDto cancel(@PathVariable final Long id) {
        log.debug("[PROJECT-CONTROLLER] Cancelando projeto ID {}", id);

        return service.cancel(id);
    }

}
