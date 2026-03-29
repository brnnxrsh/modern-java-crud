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
        final var dto = new ReportDto(
            projectRepository.getMetricsByStatus(Specification.unrestricted()),
            projectRepository.getAverageDurationInDays(
                ProjectSpec.withStatuses(Set.of(ProjectStatus.FINISHED))
            ),
            projectRepository
                .countUniqueMembersAllocated(Specification.unrestricted()),
            projectRepository
                .countUniqueManagersAllocated(Specification.unrestricted())
        );

        log.info(
            "[REPORT-SERVICE] Relatório de resumo gerado com sucesso: {}",
            dto
        );

        return dto;
    }

}
