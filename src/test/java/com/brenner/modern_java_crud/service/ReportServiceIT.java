package com.brenner.modern_java_crud.service;

import static com.brenner.modern_java_crud.service.ProjectTestFixtures.createBasic;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.createFinishedWithEndAt;
import static org.assertj.core.api.Assertions.assertThatObject;

import com.brenner.modern_java_crud.TestcontainersConfiguration;
import com.brenner.modern_java_crud.domain.ProjectStatus;
import com.brenner.modern_java_crud.repository.projection.ProjectStatusMetricsProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("docker")
class ReportServiceIT extends TestcontainersConfiguration {

    @Autowired
    private ReportService service;

    @Autowired
    private ProjectService projectService;

    @Test
    void getSummary_shouldReturnAggregatedMetrics_whenProjectsExist() {
        final var today = LocalDate.now();

        createBasic(projectService);
        createFinishedWithEndAt(projectService, today.plusDays(4));
        createFinishedWithEndAt(projectService, today.plusDays(6));

        final var report = service.getSummary();
        final var statusMetrics = mapByStatus(report.statusMetrics());

        assertThatObject(statusMetrics.size()).isEqualTo(2);
        assertThatObject(statusMetrics.get(ProjectStatus.IN_REVIEW).count())
            .isEqualTo(1L);
        assertThatObject(statusMetrics.get(ProjectStatus.FINISHED).count())
            .isEqualTo(2L);
        assertThatObject(report.averageDurationInDays()).isEqualTo(5.0);
        assertThatObject(report.countUniqueMembersAllocated()).isEqualTo(1L);
        assertThatObject(report.countUniqueManagersAllocated()).isEqualTo(1L);
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
