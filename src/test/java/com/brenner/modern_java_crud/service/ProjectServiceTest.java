package com.brenner.modern_java_crud.service;

import static org.assertj.core.api.Assertions.assertThatObject;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.brenner.modern_java_crud.domain.Project;
import com.brenner.modern_java_crud.dto.ProjectCreateDto;
import com.brenner.modern_java_crud.dto.ProjectDto;
import com.brenner.modern_java_crud.dto.ProjectNextStepDto;
import com.brenner.modern_java_crud.dto.ProjectUpdateDto;
import com.brenner.modern_java_crud.exception.ResourceNotFoundException;
import com.brenner.modern_java_crud.mapper.ProjectMapper;
import com.brenner.modern_java_crud.repository.ProjectRepository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository repository;

    @Mock
    private ProjectMapper mapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private ProjectService service;

    @Test
    void create_shouldInvokeWorkflowInOrder() {
        final var createDto = Instancio.create(ProjectCreateDto.class);
        final var entity = spy(Instancio.create(Project.class));
        final var dto = Instancio.create(ProjectDto.class);

        when(mapper.from(createDto)).thenReturn(entity);
        doNothing().when(entity).validateCreate();
        doNothing().when(memberService).attachAndValidateProject(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.from(entity)).thenReturn(dto);

        final var result = service.create(createDto);

        var inOrder = inOrder(
            mapper,
            entity,
            memberService,
            repository,
            entityManager
        );
        inOrder.verify(mapper).from(createDto);
        inOrder.verify(entity).validateCreate();
        inOrder.verify(memberService).attachAndValidateProject(entity);
        inOrder.verify(repository).save(entity);
        inOrder.verify(entityManager).flush();
        inOrder.verify(entityManager).refresh(entity);
        inOrder.verify(mapper).from(entity);

        assertThatObject(result).isEqualTo(dto);
    }

    @Test
    void update_shouldInvokeWorkflowInOrder() {
        final var updateDto = Instancio.create(ProjectUpdateDto.class);
        final var entity = spy(Instancio.create(Project.class));
        final var dto = Instancio.create(ProjectDto.class);
        final var id = 1L;

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        doNothing().when(mapper).merge(updateDto, entity);
        doNothing().when(entity).validateUpdate();
        doNothing().when(memberService).attachAndValidateProject(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.from(entity)).thenReturn(dto);

        final var result = service.update(id, updateDto);

        var inOrder = inOrder(
            repository,
            mapper,
            entity,
            memberService,
            entityManager
        );
        inOrder.verify(repository).findById(id);
        inOrder.verify(mapper).merge(updateDto, entity);
        inOrder.verify(entity).validateUpdate();
        inOrder.verify(memberService).attachAndValidateProject(entity);
        inOrder.verify(repository).save(entity);
        inOrder.verify(entityManager).flush();
        inOrder.verify(entityManager).refresh(entity);
        inOrder.verify(mapper).from(entity);

        assertThatObject(result).isEqualTo(dto);
    }

    @Test
    void delete_shouldInvokeWorkflowInOrder() {
        final var entity = spy(Instancio.create(Project.class));
        when(repository.findById(anyLong())).thenReturn(Optional.of(entity));
        doNothing().when(entity).validateDelete();
        doNothing().when(repository).delete(entity);

        service.delete(entity.getId());

        final var inOrder = inOrder(entity, repository);
        inOrder.verify(entity).validateDelete();
        inOrder.verify(repository).delete(entity);
    }

    @Test
    void advanceStep_shouldInvokeWorkflowInOrder() {
        final var entity = spy(Instancio.create(Project.class));
        final var nextStepDto = Instancio.create(ProjectNextStepDto.class);
        final var dto = Instancio.create(ProjectDto.class);

        when(repository.findById(anyLong())).thenReturn(Optional.of(entity));
        doNothing().when(mapper).merge(nextStepDto, entity);
        doNothing().when(entity).fillNextStatus();
        doNothing().when(entity).validateUpdate();
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.from(entity)).thenReturn(dto);

        final var result = service.advanceStep(entity.getId(), nextStepDto);

        var inOrder = inOrder(mapper, entity, repository, entityManager);
        inOrder.verify(mapper).merge(nextStepDto, entity);
        inOrder.verify(entity).fillNextStatus();
        inOrder.verify(entity).validateUpdate();
        inOrder.verify(repository).save(entity);
        inOrder.verify(entityManager).flush();
        inOrder.verify(entityManager).refresh(entity);
        inOrder.verify(mapper).from(entity);

        assertThatObject(result).isEqualTo(dto);
    }

    @Test
    void cancel_shouldInvokeWorkflowInOrder() {
        final var entity = spy(Instancio.create(Project.class));
        final var dto = Instancio.create(ProjectDto.class);

        when(repository.findById(anyLong())).thenReturn(Optional.of(entity));
        doNothing().when(entity).fillCancelStatus();
        doNothing().when(entity).validateUpdate();
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.from(entity)).thenReturn(dto);

        final var result = service.cancel(entity.getId());

        final var inOrder = inOrder(entity, repository, entityManager, mapper);
        inOrder.verify(entity).fillCancelStatus();
        inOrder.verify(entity).validateUpdate();
        inOrder.verify(repository).save(entity);
        inOrder.verify(entityManager).flush();
        inOrder.verify(entityManager).refresh(entity);
        inOrder.verify(mapper).from(entity);

        assertThatObject(result).isEqualTo(dto);
    }

    @Test
    void find_shouldInvokeWorkflowInOrder() {
        final var entity = Instancio.create(Project.class);
        final var dto = Instancio.create(ProjectDto.class);
        final var id = 1L;

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.from(entity)).thenReturn(dto);

        final var result = service.find(id);

        final var inOrder = inOrder(repository, mapper);
        inOrder.verify(repository).findById(id);
        inOrder.verify(mapper).from(entity);

        assertThatObject(result).isEqualTo(dto);
    }

    @Test
    void find_shouldThrowResourceNotFoundException_whenIdDoesNotExist() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_shouldReturnPage_whenProjectsExist() {
        final var entity = Instancio.create(Project.class);
        final var dto = Instancio.create(ProjectDto.class);
        final var pageable = Pageable.unpaged();
        final var entityPage = new PageImpl<>(List.of(entity));

        when(
            repository.findAll(
                ArgumentMatchers.<Specification<Project>>any(),
                any(Pageable.class)
            )
        ).thenReturn(entityPage);
        when(mapper.from(entity)).thenReturn(dto);

        final var result = service
            .findAll(ProjectTestFixtures.emptyFilter(), pageable);

        assertThatObject(result.getTotalElements()).isEqualTo(1L);
        assertThatObject(result.getContent().get(0)).isEqualTo(dto);
    }

    @Test
    void findAll_shouldReturnEmptyPage_whenNoProjectsExist() {
        final var pageable = Pageable.unpaged();
        final var emptyPage = new PageImpl<Project>(List.of());

        when(
            repository.findAll(
                ArgumentMatchers.<Specification<Project>>any(),
                any(Pageable.class)
            )
        ).thenReturn(emptyPage);

        final var result = service
            .findAll(ProjectTestFixtures.emptyFilter(), pageable);

        assertThatObject(result.hasContent()).isEqualTo(false);
    }

}
