package com.brenner.modern_java_crud.service;

import static org.assertj.core.api.Assertions.assertThatObject;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.brenner.modern_java_crud.client.MemberClient;
import com.brenner.modern_java_crud.domain.Member;
import com.brenner.modern_java_crud.domain.MemberRole;
import com.brenner.modern_java_crud.domain.Project;
import com.brenner.modern_java_crud.dto.MemberExternalCreateDto;
import com.brenner.modern_java_crud.dto.MemberExternalDto;
import com.brenner.modern_java_crud.exception.BusinessException;
import com.brenner.modern_java_crud.repository.ProjectRepository;

import java.util.Set;

import jakarta.persistence.EntityManager;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MemberClient memberClient;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private MemberService service;

    @Test
    void attachAndValidateProject_shouldInvokeWorkflowInOrder() {
        final var manager = Instancio.create(Member.class);
        final var member = Instancio.create(Member.class);
        final var project = spy(
            Instancio.of(Project.class)
                .set(field(Project::getManager), manager)
                .set(field(Project::getMembers), Set.of(member))
                .create()
        );

        final var mergedManager = Instancio.create(Member.class);
        final var managerExternal = new MemberExternalDto(
            mergedManager.getId(),
            "Manager",
            MemberRole.MANAGER
        );
        final var memberExternal = new MemberExternalDto(
            member.getId(),
            "Employee",
            MemberRole.EMPLOYEE
        );

        when(entityManager.merge(manager)).thenReturn(mergedManager);
        when(entityManager.merge(member)).thenReturn(member);
        doNothing().when(project).validateManager();
        doNothing().when(project).validateMembers();
        when(memberClient.findById(mergedManager.getId()))
            .thenReturn(managerExternal);
        when(memberClient.findById(member.getId())).thenReturn(memberExternal);
        when(
            projectRepository
                .count(ArgumentMatchers.<Specification<Project>>any())
        ).thenReturn(0L);

        service.attachAndValidateProject(project);

        final var inOrder = inOrder(
            entityManager,
            project,
            memberClient,
            projectRepository
        );
        inOrder.verify(entityManager).merge(manager);
        inOrder.verify(project).validateManager();
        inOrder.verify(memberClient).findById(mergedManager.getId());
        inOrder.verify(entityManager).merge(member);
        inOrder.verify(project).validateMembers();
        inOrder.verify(memberClient).findById(member.getId());
        inOrder.verify(projectRepository)
            .count(ArgumentMatchers.<Specification<Project>>any());
    }

    @Test
    void attachAndValidateProject_shouldThrowException_WhenManagerRoleIsNotManager() {
        final var manager = Instancio.create(Member.class);
        final var project = spy(
            Instancio.of(Project.class)
                .set(field(Project::getManager), manager)
                .create()
        );

        final var mergedManager = Instancio.create(Member.class);
        final var managerExternal = new MemberExternalDto(
            mergedManager.getId(),
            "Employee",
            MemberRole.EMPLOYEE
        );

        when(entityManager.merge(manager)).thenReturn(mergedManager);
        doNothing().when(project).validateManager();
        when(memberClient.findById(mergedManager.getId()))
            .thenReturn(managerExternal);

        assertThatThrownBy(() -> service.attachAndValidateProject(project))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void attachAndValidateProject_shouldThrowException_WhenMemberRoleIsNotEmployee() {
        final var manager = Instancio.create(Member.class);
        final var member = Instancio.create(Member.class);
        final var project = spy(
            Instancio.of(Project.class)
                .set(field(Project::getManager), manager)
                .set(field(Project::getMembers), Set.of(member))
                .create()
        );

        final var mergedManager = Instancio.create(Member.class);
        final var managerExternal = new MemberExternalDto(
            mergedManager.getId(),
            "Manager",
            MemberRole.MANAGER
        );
        final var memberExternal = new MemberExternalDto(
            member.getId(),
            "Manager",
            MemberRole.MANAGER
        );

        when(entityManager.merge(manager)).thenReturn(mergedManager);
        when(entityManager.merge(member)).thenReturn(member);
        doNothing().when(project).validateManager();
        doNothing().when(project).validateMembers();
        when(memberClient.findById(mergedManager.getId()))
            .thenReturn(managerExternal);
        when(memberClient.findById(member.getId())).thenReturn(memberExternal);

        assertThatThrownBy(() -> service.attachAndValidateProject(project))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void attachAndValidateProject_shouldThrowException_WhenMemberExceedsActiveProjectsLimit() {
        final var manager = Instancio.create(Member.class);
        final var member = Instancio.create(Member.class);
        final var project = spy(
            Instancio.of(Project.class)
                .set(field(Project::getManager), manager)
                .set(field(Project::getMembers), Set.of(member))
                .create()
        );

        final var mergedManager = Instancio.create(Member.class);
        final var managerExternal = new MemberExternalDto(
            mergedManager.getId(),
            "Manager",
            MemberRole.MANAGER
        );
        final var memberExternal = new MemberExternalDto(
            member.getId(),
            "Employee",
            MemberRole.EMPLOYEE
        );

        when(entityManager.merge(manager)).thenReturn(mergedManager);
        when(entityManager.merge(member)).thenReturn(member);
        doNothing().when(project).validateManager();
        doNothing().when(project).validateMembers();
        when(memberClient.findById(mergedManager.getId()))
            .thenReturn(managerExternal);
        when(memberClient.findById(member.getId())).thenReturn(memberExternal);
        when(
            projectRepository
                .count(ArgumentMatchers.<Specification<Project>>any())
        ).thenReturn(3L);

        assertThatThrownBy(() -> service.attachAndValidateProject(project))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void create_shouldReturnMember() {
        final var dto = Instancio.create(MemberExternalCreateDto.class);
        final var expected = Instancio.create(MemberExternalDto.class);

        when(memberClient.create(dto)).thenReturn(expected);

        final var result = service.create(dto);

        assertThatObject(result).isEqualTo(expected);
    }

    @Test
    void findById_shouldReturnMember() {
        final var expected = Instancio.create(MemberExternalDto.class);

        when(memberClient.findById(expected.id())).thenReturn(expected);

        final var result = service.findById(expected.id());

        assertThatObject(result).isEqualTo(expected);
    }

}
