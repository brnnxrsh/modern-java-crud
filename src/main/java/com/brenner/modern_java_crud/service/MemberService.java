package com.brenner.modern_java_crud.service;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import com.brenner.modern_java_crud.client.MemberClient;
import com.brenner.modern_java_crud.domain.Member;
import com.brenner.modern_java_crud.domain.MemberRole;
import com.brenner.modern_java_crud.domain.Project;
import com.brenner.modern_java_crud.domain.ProjectStatus;
import com.brenner.modern_java_crud.exception.BusinessException;
import com.brenner.modern_java_crud.repository.ProjectRepository;

import java.util.Set;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final ProjectRepository projectRepository;
    private final MemberClient memberClient;
    private final EntityManager entityManager;

    @Transactional
    public void attachAndValidateProject(final Project project) {
        this.attachManager(project);
        this.validateManager(project);

        log.info(
            "[MEMBER-SERVICE] Gerente ID {} vinculado ao projeto '{}' e validado com sucesso",
            project.getManager().getId(),
            project.getName()
        );

        this.attachMembers(project);
        this.validateMembers(project);

        log.info(
            "[MEMBER-SERVICE] Membros IDs [{}] vinculados ao projeto '{}' e validados com sucesso",
            project.getMembers()
                .stream()
                .map(Member::getId)
                .map(String::valueOf)
                .collect(joining(", ")),
            project.getName()
        );
    }

    private void attachManager(final Project project) {
        final var manager = this.entityManager.merge(project.getManager());
        project.setManager(manager);
    }

    private void attachMembers(final Project project) {
        final var members = project.getMembers()
            .stream()
            .map(this.entityManager::merge)
            .collect(toSet());
        project.setMembers(members);
    }

    private void validateMembers(final Project project) {
        project.validateMembers();

        for (final Member member : project.getMembers()) {
            final var memberId = member.getId();
            final var memberExternal = this.memberClient.findById(memberId);

            if (memberExternal.role() != MemberRole.EMPLOYEE)
                throw new BusinessException(
                    String.format(
                        "Membro ID %d deve ter a atribuição %s.",
                        memberId,
                        MemberRole.EMPLOYEE
                    )
                );

            final long countedProjects = this.projectRepository
                .countProjectsByMemberIdAndStatusNotIn(
                    memberId,
                    Set.of(ProjectStatus.FINISHED, ProjectStatus.CANCELLED)
                );

            if (countedProjects >= 3)
                throw new BusinessException(
                    String.format(
                        "Membro ID %d não pode ser vinculado porque já atingiu o limite de 3 projetos ativos.",
                        memberId
                    )
                );
        }

    }

    private void validateManager(final Project project) {
        project.validateManager();

        final var managerId = project.getManager().getId();
        final var managerExternal = this.memberClient.findById(managerId);

        if (managerExternal.role() != MemberRole.MANAGER)
            throw new BusinessException(
                String.format(
                    "O ID %d informado para gerente responsável deve ter atribuição %s.",
                    managerId,
                    MemberRole.MANAGER
                )
            );
    }

}
