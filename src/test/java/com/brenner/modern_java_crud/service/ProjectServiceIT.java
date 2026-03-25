package com.brenner.modern_java_crud.service;

import static org.assertj.core.api.Assertions.assertThatObject;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;

import com.brenner.modern_java_crud.TestcontainersConfiguration;
import com.brenner.modern_java_crud.domain.ProjectStatus;
import com.brenner.modern_java_crud.domain.RiskLevel;
import com.brenner.modern_java_crud.dto.ProjectCreateDto;
import com.brenner.modern_java_crud.dto.ProjectDto;
import com.brenner.modern_java_crud.dto.ProjectNextStepDto;
import com.brenner.modern_java_crud.dto.ProjectUpdateDto;
import com.brenner.modern_java_crud.exception.BusinessException;
import com.brenner.modern_java_crud.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class ProjectServiceIT {

    @Autowired
    private ProjectService service;

    private ProjectDto createWithTotalBudget(final BigDecimal totalBudget) {
        final var createDto = Instancio.of(ProjectCreateDto.class)
            .set(field(ProjectCreateDto::totalBudget), totalBudget)
            .set(field(ProjectCreateDto::startAt), LocalDate.now())
            .set(field(ProjectCreateDto::expectedEndAt), LocalDate.now())
            .create();

        return service.create(createDto);
    }

    private ProjectDto updateWithTotalBudget(
        final ProjectDto oldDto,
        final BigDecimal totalBudget
    ) {
        final var updateDto = Instancio.of(ProjectUpdateDto.class)
            .set(field(ProjectUpdateDto::totalBudget), totalBudget)
            .set(field(ProjectUpdateDto::startAt), oldDto.startAt())
            .set(field(ProjectUpdateDto::expectedEndAt), oldDto.expectedEndAt())
            .set(field(ProjectUpdateDto::endAt), null)
            .create();

        return service.update(oldDto.id(), updateDto);
    }

    private ProjectDto createWithDurationMonths(final Integer durationMonths) {
        final var createDto = Instancio.of(ProjectCreateDto.class)
            .set(field(ProjectCreateDto::totalBudget), BigDecimal.ZERO)
            .set(field(ProjectCreateDto::startAt), LocalDate.now())
            .set(
                field(ProjectCreateDto::expectedEndAt),
                LocalDate.now().plusMonths(durationMonths)
            )
            .create();

        return service.create(createDto);
    }

    private ProjectDto updateWithDurationMonths(
        final ProjectDto oldDto,
        final Integer durationMonths
    ) {
        final var updateDto = Instancio.of(ProjectUpdateDto.class)
            .set(field(ProjectUpdateDto::totalBudget), BigDecimal.ZERO)
            .set(field(ProjectUpdateDto::startAt), oldDto.startAt())
            .set(
                field(ProjectUpdateDto::expectedEndAt),
                oldDto.startAt().plusMonths(durationMonths)
            )
            .set(field(ProjectUpdateDto::endAt), null)
            .create();

        return service.update(oldDto.id(), updateDto);
    }

    private ProjectDto createBasicAndAdvanceTo(
        final ProjectStatus targetStatus
    ) {
        final var createDto = Instancio.of(ProjectCreateDto.class)
            .set(field(ProjectCreateDto::totalBudget), BigDecimal.ZERO)
            .set(field(ProjectCreateDto::startAt), LocalDate.now())
            .set(field(ProjectCreateDto::expectedEndAt), LocalDate.now())
            .create();

        ProjectDto dto = service.create(createDto);
        assertThatObject(dto.status()).isEqualTo(ProjectStatus.getInitial());

        while (dto.status() != targetStatus && !dto.status().isFinal())
            dto = service
                .advanceStep(dto.id(), new ProjectNextStepDto(LocalDate.now()));

        return dto;
    }

    @ParameterizedTest
    @CsvSource(
        {
            "500000.01, HIGH",
            "500000.00, MEDIUM",
            "100001.00, MEDIUM",
            "100000.99, LOW",
            "     0.00, LOW"

        }
    )
    void create_shouldCalculateCorrectRiskLevelByTotalBudget(
        final BigDecimal totalBudget,
        final RiskLevel riskLevel
    ) {
        final ProjectDto dto = createWithTotalBudget(totalBudget);
        assertThatObject(dto.totalBudget()).isEqualTo(totalBudget);
        assertThatObject(dto.riskLevel()).isEqualTo(riskLevel);
    }

    @ParameterizedTest
    @CsvSource(
        {
            "     0.00, 500000.01,  LOW,    HIGH",
            "500000.01, 500000.00,  HIGH,   MEDIUM",
            "500000.00, 100001.00,  MEDIUM, MEDIUM",
            "100001.00, 100000.99,  MEDIUM, LOW",
            "100000.99, 0.00,       LOW,    LOW"
        }
    )
    void update_shouldCalculateCorrectRiskLevelByTotalBudget(
        final BigDecimal oldTotalBudget,
        final BigDecimal newTotalBudget,
        final RiskLevel oldRiskLevel,
        final RiskLevel newRiskLevel
    ) {
        final ProjectDto oldDto = createWithTotalBudget(oldTotalBudget);
        assertThatObject(oldDto.totalBudget()).isEqualTo(oldTotalBudget);
        assertThatObject(oldDto.riskLevel()).isEqualTo(oldRiskLevel);

        final ProjectDto newDto = updateWithTotalBudget(oldDto, newTotalBudget);
        assertThatObject(newDto.totalBudget()).isEqualTo(newTotalBudget);
        assertThatObject(newDto.riskLevel()).isEqualTo(newRiskLevel);
    }

    @ParameterizedTest()
    @CsvSource(
        {
            "7, HIGH",
            "6, MEDIUM",
            "3, MEDIUM",
            "2, LOW",
            "0, LOW"
        }
    )
    void create_shouldCalculateCorrectRiskLevelByDurationMonths(
        final Integer durationMonths,
        final RiskLevel riskLevel
    ) {
        final ProjectDto dto = createWithDurationMonths(durationMonths);
        assertThatObject(dto.durationMonths()).isEqualTo(durationMonths);
        assertThatObject(dto.riskLevel()).isEqualTo(riskLevel);
    }

    @ParameterizedTest()
    @CsvSource(
        {
            "0, 7, LOW,    HIGH",
            "7, 6, HIGH,   MEDIUM",
            "6, 3, MEDIUM, MEDIUM",
            "3, 2, MEDIUM, LOW",
            "2, 0, LOW,    LOW"
        }
    )
    void update_shouldCalculateCorrectRiskLevelByDurationMonths(
        final Integer oldDurationMonths,
        final Integer newDurationMonths,
        final RiskLevel oldRiskLevel,
        final RiskLevel newRiskLevel
    ) {
        final ProjectDto oldDto = createWithDurationMonths(oldDurationMonths);
        assertThatObject(oldDto.durationMonths()).isEqualTo(oldDurationMonths);
        assertThatObject(oldDto.riskLevel()).isEqualTo(oldRiskLevel);

        final ProjectDto newDto = updateWithDurationMonths(
            oldDto,
            newDurationMonths
        );
        assertThatObject(newDto.durationMonths()).isEqualTo(newDurationMonths);
        assertThatObject(newDto.riskLevel()).isEqualTo(newRiskLevel);
    }

    @Test
    void find_shouldThrowException_whenIdDoesNotExist() {
        final var nonExistentId = 9999L;
        assertThatThrownBy(() -> service.find(nonExistentId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class)
    void delete_shouldDelete_whenAllowed(final ProjectStatus status) {
        final ProjectDto dto = this.createBasicAndAdvanceTo(status);

        if (!dto.status().canDelete())
            return;

        service.delete(dto.id());

        assertThatThrownBy(() -> service.find(dto.id()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class)
    void delete_shouldThrow_whenNotAllowed(final ProjectStatus status) {
        final ProjectDto dto = this.createBasicAndAdvanceTo(status);

        if (dto.status().canDelete())
            return;

        assertThatThrownBy(() -> service.delete(dto.id()))
            .isInstanceOf(BusinessException.class);
    }

    @ParameterizedTest
    @EnumSource(
        value = ProjectStatus.class,
        names = {
            "CANCELLED"
        },
        mode = EnumSource.Mode.EXCLUDE
    )
    void advanceStep_shouldAdvanceStatusSuccessfully(
        final ProjectStatus targetStatus
    ) {
        final ProjectDto dto = this.createBasicAndAdvanceTo(targetStatus);
        assertThatObject(dto.status()).isEqualTo(targetStatus);
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class)
    void cancel_shouldCancelSuccessfully(final ProjectStatus targetStatus) {
        final ProjectDto dto = this.createBasicAndAdvanceTo(targetStatus);
        final ProjectDto newDto = service.cancel(dto.id());
        assertThatObject(newDto.status()).isEqualTo(ProjectStatus.CANCELLED);
    }

}
