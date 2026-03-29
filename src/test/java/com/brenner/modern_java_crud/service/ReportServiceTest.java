package com.brenner.modern_java_crud.service;

import static org.assertj.core.api.Assertions.assertThatObject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.brenner.modern_java_crud.dto.ReportDto;
import com.brenner.modern_java_crud.repository.ProjectRepository;
import com.brenner.modern_java_crud.repository.projection.ProjectStatusMetricsProjection;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ReportService service;

    @Test
    void getSummary_shouldInvokeWorkflowInOrder() {
        final var statusMetrics = Instancio
            .createList(ProjectStatusMetricsProjection.class);
        final var averageDurationInDays = 1.5;
        final var countUniqueMembers = 5L;
        final var countUniqueManagers = 3L;

        when(projectRepository.getMetricsByStatus(any()))
            .thenReturn(statusMetrics);
        when(projectRepository.getAverageDurationInDays(any()))
            .thenReturn(averageDurationInDays);
        when(projectRepository.countUniqueMembersAllocated(any()))
            .thenReturn(countUniqueMembers);
        when(projectRepository.countUniqueManagersAllocated(any()))
            .thenReturn(countUniqueManagers);

        final var result = service.getSummary();

        final var inOrder = inOrder(projectRepository);
        inOrder.verify(projectRepository).getMetricsByStatus(any());
        inOrder.verify(projectRepository).getAverageDurationInDays(any());
        inOrder.verify(projectRepository).countUniqueMembersAllocated(any());
        inOrder.verify(projectRepository).countUniqueManagersAllocated(any());

        assertThatObject(result).isEqualTo(
            new ReportDto(
                statusMetrics,
                averageDurationInDays,
                countUniqueMembers,
                countUniqueManagers
            )
        );
    }

}
