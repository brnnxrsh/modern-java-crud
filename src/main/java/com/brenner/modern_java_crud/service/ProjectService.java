package com.brenner.modern_java_crud.service;

import com.brenner.modern_java_crud.domain.Project;
import com.brenner.modern_java_crud.domain.ProjectMapper;
import com.brenner.modern_java_crud.dto.ProjectCreateDto;
import com.brenner.modern_java_crud.dto.ProjectDto;
import com.brenner.modern_java_crud.dto.ProjectFilterDto;
import com.brenner.modern_java_crud.dto.ProjectNextStepDto;
import com.brenner.modern_java_crud.dto.ProjectUpdateDto;
import com.brenner.modern_java_crud.exception.ResourceNotFoundException;
import com.brenner.modern_java_crud.repository.ProjectRepository;
import com.brenner.modern_java_crud.repository.spec.ProjectSpec;

import jakarta.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectMapper mapper;
    private final ProjectRepository repository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<ProjectDto> findAll(
        final ProjectFilterDto filter,
        final Pageable pageable
    ) {
        final Page<ProjectDto> page = this.repository
            .findAll(ProjectSpec.fromFilter(filter), pageable)
            .map(this.mapper::from);

        if (!page.hasContent())
            log.info(
                "[PROJECT-SERVICE] Nenhum projeto encontrado para os filtros {} e paginação {}",
                filter,
                pageable
            );

        return page;
    }

    @Transactional(readOnly = true)
    public ProjectDto find(final Long id) {
        return this.mapper.from(this.findEntity(id));
    }

    @Transactional
    public ProjectDto create(final ProjectCreateDto dto) {
        Project entity = this.mapper.from(dto);
        entity.validateCreate();
        entity = this.saveAndRefresh(entity);

        log.info("[PROJECT-SERVICE] Projeto criado com sucesso: {}", entity);

        return this.mapper.from(entity);
    }

    @Transactional
    public ProjectDto update(final Long id, final ProjectUpdateDto dto) {
        Project entity = this.findEntity(id);
        this.mapper.merge(dto, entity);
        entity.validateUpdate();
        entity = this.saveAndRefresh(entity);

        log.info(
            "[PROJECT-SERVICE] Projeto ID {} atualizado com sucesso: {}",
            id,
            entity
        );

        return this.mapper.from(entity);
    }

    @Transactional
    public void delete(final Long id) {
        final Project entity = this.findEntity(id);
        entity.validateDelete();
        this.repository.delete(entity);

        log.info(
            "[PROJECT-SERVICE] Projeto ID {} deletado com sucesso: {}",
            id,
            entity
        );
    }

    @Transactional
    public ProjectDto advanceStep(final Long id, final ProjectNextStepDto dto) {
        Project entity = this.findEntity(id);
        this.mapper.merge(dto, entity);
        entity.fillNextStatus();
        entity.validateUpdate();
        entity = this.saveAndRefresh(entity);

        log.info(
            "[PROJECT-SERVICE] Projeto ID {} avançado de etapa com sucesso: {}",
            id,
            entity
        );

        return this.mapper.from(entity);
    }

    @Transactional
    public ProjectDto cancel(final Long id) {
        Project entity = this.findEntity(id);
        entity.fillCancelStatus();
        entity.validateUpdate();
        entity = this.saveAndRefresh(entity);

        log.info(
            "[PROJECT-SERVICE] Projeto ID {} cancelado com sucesso: {}",
            id,
            entity
        );

        return this.mapper.from(entity);
    }

    private Project saveAndRefresh(final Project entity) {
        final Project newEntity = this.repository.save(entity);
        this.entityManager.flush();
        this.entityManager.refresh(newEntity);
        return newEntity;
    }

    private Project findEntity(final Long id) {
        return this.repository.findById(id).orElseThrow(() -> {
            log.warn("[PROJECT-SERVICE] Projeto ID {} não encontrado", id);

            return new ResourceNotFoundException("Projeto não encontrado!");
        });
    }

}
