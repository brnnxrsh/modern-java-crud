package com.brenner.modern_java_crud.repository;

import static com.brenner.modern_java_crud.service.ProjectTestFixtures.MEMBER_DTO_ID_3;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.createBasic;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.createFinishedWithEndAt;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.createWithMembers;
import static org.assertj.core.api.Assertions.assertThatObject;

import com.brenner.modern_java_crud.TestcontainersConfiguration;
import com.brenner.modern_java_crud.domain.ProjectStatus;
import com.brenner.modern_java_crud.repository.projection.ProjectStatusMetricsProjection;
import com.brenner.modern_java_crud.repository.spec.ProjectSpec;
import com.brenner.modern_java_crud.service.ProjectService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("docker")
class ProjectRepositoryIT extends TestcontainersConfiguration {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    @Test
    void getMetricsByStatus_shouldReturnAllGroups_whenUnrestricted() {
        final var today = LocalDate.now();
        createBasic(projectService);
        createFinishedWithEndAt(projectService, today);
        createFinishedWithEndAt(projectService, today);

        final var byStatus = mapByStatus(
            projectRepository.getMetricsByStatus(Specification.unrestricted())
        );

        assertThatObject(byStatus.size()).isEqualTo(2);
        assertThatObject(byStatus.get(ProjectStatus.IN_REVIEW).count())
            .isEqualTo(1L);
        assertThatObject(byStatus.get(ProjectStatus.FINISHED).count())
            .isEqualTo(2L);
    }

    @Test
    void getMetricsByStatus_shouldFilterByStatus_whenSpecIsProvided() {
        final var today = LocalDate.now();
        createBasic(projectService);
        createFinishedWithEndAt(projectService, today);
        createFinishedWithEndAt(projectService, today);

        final var metrics = projectRepository.getMetricsByStatus(
            ProjectSpec.withStatuses(Set.of(ProjectStatus.FINISHED))
        );

        assertThatObject(metrics.size()).isEqualTo(1);
        assertThatObject(metrics.getFirst().status())
            .isEqualTo(ProjectStatus.FINISHED);
        assertThatObject(metrics.getFirst().count()).isEqualTo(2L);
    }

    @Test
    void getAverageDurationInDays_shouldReturnZero_whenNoProjectsMatch() {
        createBasic(projectService);

        final var avg = projectRepository.getAverageDurationInDays(
            ProjectSpec.withStatuses(Set.of(ProjectStatus.CANCELLED))
        );

        assertThatObject(avg).isEqualTo(0.0);
    }

    @Test
    void getAverageDurationInDays_shouldAverageOnlyMatchingProjects_whenSpecIsProvided() {
        final var today = LocalDate.now();
        createBasic(projectService);
        createFinishedWithEndAt(projectService, today.plusDays(4));
        createFinishedWithEndAt(projectService, today.plusDays(6));

        final var avg = projectRepository.getAverageDurationInDays(
            ProjectSpec.withStatuses(Set.of(ProjectStatus.FINISHED))
        );

        assertThatObject(avg).isEqualTo(5.0);
    }

    @Test
    void countUniqueMembersAllocated_shouldCountDistinctMembers_whenUnrestricted() {
        createBasic(projectService);
        createBasic(projectService);

        final var count = projectRepository
            .countUniqueMembersAllocated(Specification.unrestricted());

        assertThatObject(count).isEqualTo(1L);
    }

    @Test
    void countUniqueMembersAllocated_shouldRespectSpecFilter_whenFilterApplied() {
        final var today = LocalDate.now();
        createWithMembers(projectService, Set.of(MEMBER_DTO_ID_3));
        createFinishedWithEndAt(projectService, today);

        final var finishedSpec = ProjectSpec
            .withStatuses(Set.of(ProjectStatus.FINISHED));

        assertThatObject(
            projectRepository.countUniqueMembersAllocated(finishedSpec)
        ).isEqualTo(1L);
        assertThatObject(
            projectRepository
                .countUniqueMembersAllocated(Specification.unrestricted())
        ).isEqualTo(2L);
    }

    @Test
    void countUniqueManagersAllocated_shouldCountDistinctManagers() {
        final var today = LocalDate.now();
        createBasic(projectService);
        createFinishedWithEndAt(projectService, today);

        final var count = projectRepository
            .countUniqueManagersAllocated(Specification.unrestricted());

        assertThatObject(count).isEqualTo(1L);
    }

    private static Map<ProjectStatus, ProjectStatusMetricsProjection> mapByStatus(
        final List<ProjectStatusMetricsProjection> statusMetrics
    ) {
        return statusMetrics.stream()
            .collect(
                Collectors.toMap(
                    ProjectStatusMetricsProjection::status,
                    Function.identity()
                )
            );
    }

}
