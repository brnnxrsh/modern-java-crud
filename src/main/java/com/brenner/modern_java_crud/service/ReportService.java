package com.brenner.modern_java_crud.service;

import com.brenner.modern_java_crud.domain.ProjectStatus;
import com.brenner.modern_java_crud.dto.ReportDto;
import com.brenner.modern_java_crud.repository.ProjectRepository;
import com.brenner.modern_java_crud.repository.spec.ProjectSpec;

import java.util.Set;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ProjectRepository projectRepository;

    public ReportDto getSummary() {
        final var statusMetrics = projectRepository
            .getMetricsByStatus(Specification.unrestricted());
        final var averageDurationInDays = projectRepository
            .getAverageDurationInDays(
                ProjectSpec.withStatuses(Set.of(ProjectStatus.FINISHED))
            );
        final var countUniqueMembersAllocated = projectRepository
            .countUniqueMembersAllocated(Specification.unrestricted());
        final var countUniqueManagersAllocated = projectRepository
            .countUniqueManagersAllocated(Specification.unrestricted());

        final var dto = new ReportDto(
            statusMetrics,
            averageDurationInDays,
            countUniqueMembersAllocated,
            countUniqueManagersAllocated
        );

        log.info(
            "[REPORT-SERVICE] Relatório de resumo gerado com sucesso: {}",
            dto
        );

        return dto;
    }

}
