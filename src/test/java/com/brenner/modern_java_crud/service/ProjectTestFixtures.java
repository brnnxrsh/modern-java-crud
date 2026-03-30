package com.brenner.modern_java_crud.service;

import com.brenner.modern_java_crud.domain.ProjectStatus;
import com.brenner.modern_java_crud.domain.RiskLevel;
import com.brenner.modern_java_crud.dto.MemberDto;
import com.brenner.modern_java_crud.dto.ProjectCreateDto;
import com.brenner.modern_java_crud.dto.ProjectDto;
import com.brenner.modern_java_crud.dto.ProjectFilterDto;
import com.brenner.modern_java_crud.dto.ProjectNextStepDto;
import com.brenner.modern_java_crud.dto.ProjectUpdateDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.params.provider.Arguments;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectTestFixtures {

    public static final MemberDto MANAGER_DTO_ID_1 = new MemberDto(1L);
    public static final MemberDto MEMBER_DTO_ID_2 = new MemberDto(2L);
    public static final MemberDto MEMBER_DTO_ID_3 = new MemberDto(3L);

    public static Stream<Arguments> riskLevelByBudgetCases() {
        return Stream.of(
            Arguments.of("500000.01", "HIGH"),
            Arguments.of("500000.00", "MEDIUM"),
            Arguments.of("100001.00", "MEDIUM"),
            Arguments.of("100000.99", "LOW"),
            Arguments.of("0.00", "LOW")
        );
    }

    public static Stream<Arguments> riskLevelUpdateByBudgetCases() {
        return Stream.of(
            Arguments.of("0.00", "500000.01", "LOW", "HIGH"),
            Arguments.of("500000.01", "500000.00", "HIGH", "MEDIUM"),
            Arguments.of("500000.00", "100001.00", "MEDIUM", "MEDIUM"),
            Arguments.of("100001.00", "100000.99", "MEDIUM", "LOW"),
            Arguments.of("100000.99", "0.00", "LOW", "LOW")
        );
    }

    public static Stream<Arguments> riskLevelByDurationCases() {
        return Stream.of(
            Arguments.of("7", "HIGH"),
            Arguments.of("6", "MEDIUM"),
            Arguments.of("3", "MEDIUM"),
            Arguments.of("2", "LOW"),
            Arguments.of("0", "LOW")
        );
    }

    public static Stream<Arguments> riskLevelUpdateByDurationCases() {
        return Stream.of(
            Arguments.of("0", "7", "LOW", "HIGH"),
            Arguments.of("7", "6", "HIGH", "MEDIUM"),
            Arguments.of("6", "3", "MEDIUM", "MEDIUM"),
            Arguments.of("3", "2", "MEDIUM", "LOW"),
            Arguments.of("2", "0", "LOW", "LOW")
        );
    }

    public static ProjectDto createWithTotalBudget(
        final ProjectService service,
        final BigDecimal totalBudget
    ) {
        final var createDto = Instancio.of(ProjectCreateDto.class)
            .set(Select.field(ProjectCreateDto::totalBudget), totalBudget)
            .set(Select.field(ProjectCreateDto::startAt), LocalDate.now())
            .set(Select.field(ProjectCreateDto::expectedEndAt), LocalDate.now())
            .set(Select.field(ProjectCreateDto::manager), MANAGER_DTO_ID_1)
            .set(
                Select.field(ProjectCreateDto::members),
                Set.of(MEMBER_DTO_ID_2)
            )
            .create();

        return service.create(createDto);
    }

    public static ProjectDto updateWithTotalBudget(
        final ProjectService service,
        final ProjectDto oldDto,
        final BigDecimal totalBudget
    ) {
        final var updateDto = Instancio.of(ProjectUpdateDto.class)
            .set(Select.field(ProjectUpdateDto::totalBudget), totalBudget)
            .set(Select.field(ProjectUpdateDto::startAt), oldDto.startAt())
            .set(
                Select.field(ProjectUpdateDto::expectedEndAt),
                oldDto.expectedEndAt()
            )
            .set(Select.field(ProjectUpdateDto::endAt), null)
            .set(Select.field(ProjectUpdateDto::manager), oldDto.manager())
            .set(Select.field(ProjectUpdateDto::members), oldDto.members())
            .create();

        return service.update(oldDto.id(), updateDto);
    }

    public static ProjectDto createWithDurationMonths(
        final ProjectService service,
        final Integer durationMonths
    ) {
        final var createDto = Instancio.of(ProjectCreateDto.class)
            .set(Select.field(ProjectCreateDto::totalBudget), BigDecimal.ZERO)
            .set(Select.field(ProjectCreateDto::startAt), LocalDate.now())
            .set(
                Select.field(ProjectCreateDto::expectedEndAt),
                LocalDate.now().plusMonths(durationMonths)
            )
            .set(Select.field(ProjectCreateDto::manager), MANAGER_DTO_ID_1)
            .set(
                Select.field(ProjectCreateDto::members),
                Set.of(MEMBER_DTO_ID_2)
            )
            .create();

        return service.create(createDto);
    }

    public static ProjectDto updateWithDurationMonths(
        final ProjectService service,
        final ProjectDto oldDto,
        final Integer durationMonths
    ) {
        final var updateDto = Instancio.of(ProjectUpdateDto.class)
            .set(Select.field(ProjectUpdateDto::totalBudget), BigDecimal.ZERO)
            .set(Select.field(ProjectUpdateDto::startAt), oldDto.startAt())
            .set(
                Select.field(ProjectUpdateDto::expectedEndAt),
                oldDto.startAt().plusMonths(durationMonths)
            )
            .set(Select.field(ProjectUpdateDto::endAt), null)
            .set(Select.field(ProjectUpdateDto::manager), oldDto.manager())
            .set(Select.field(ProjectUpdateDto::members), oldDto.members())
            .create();

        return service.update(oldDto.id(), updateDto);
    }

    public static ProjectDto createBasic(final ProjectService service) {
        return createWithTotalBudget(service, BigDecimal.ZERO);
    }

    public static ProjectDto createWithName(
        final ProjectService service,
        final String name
    ) {
        final var createDto = Instancio.of(ProjectCreateDto.class)
            .set(Select.field(ProjectCreateDto::name), name)
            .set(Select.field(ProjectCreateDto::totalBudget), BigDecimal.ZERO)
            .set(Select.field(ProjectCreateDto::startAt), LocalDate.now())
            .set(Select.field(ProjectCreateDto::expectedEndAt), LocalDate.now())
            .set(Select.field(ProjectCreateDto::manager), MANAGER_DTO_ID_1)
            .set(
                Select.field(ProjectCreateDto::members),
                Set.of(MEMBER_DTO_ID_2)
            )
            .create();
        return service.create(createDto);
    }

    public static ProjectDto createWithMembers(
        final ProjectService service,
        final Set<MemberDto> members
    ) {
        final var createDto = Instancio.of(ProjectCreateDto.class)
            .set(Select.field(ProjectCreateDto::totalBudget), BigDecimal.ZERO)
            .set(Select.field(ProjectCreateDto::startAt), LocalDate.now())
            .set(Select.field(ProjectCreateDto::expectedEndAt), LocalDate.now())
            .set(Select.field(ProjectCreateDto::manager), MANAGER_DTO_ID_1)
            .set(Select.field(ProjectCreateDto::members), members)
            .create();
        return service.create(createDto);
    }

    public static ProjectDto createWithDates(
        final ProjectService service,
        final LocalDate startAt,
        final LocalDate expectedEndAt
    ) {
        final var createDto = Instancio.of(ProjectCreateDto.class)
            .set(Select.field(ProjectCreateDto::totalBudget), BigDecimal.ZERO)
            .set(Select.field(ProjectCreateDto::startAt), startAt)
            .set(Select.field(ProjectCreateDto::expectedEndAt), expectedEndAt)
            .set(Select.field(ProjectCreateDto::manager), MANAGER_DTO_ID_1)
            .set(
                Select.field(ProjectCreateDto::members),
                Set.of(MEMBER_DTO_ID_2)
            )
            .create();
        return service.create(createDto);
    }

    public static ProjectDto createBasicAndAdvanceTo(
        final ProjectService service,
        final ProjectStatus targetStatus
    ) {
        final var createDto = Instancio.of(ProjectCreateDto.class)
            .set(Select.field(ProjectCreateDto::totalBudget), BigDecimal.ZERO)
            .set(Select.field(ProjectCreateDto::startAt), LocalDate.now())
            .set(Select.field(ProjectCreateDto::expectedEndAt), LocalDate.now())
            .set(Select.field(ProjectCreateDto::manager), MANAGER_DTO_ID_1)
            .set(
                Select.field(ProjectCreateDto::members),
                Set.of(MEMBER_DTO_ID_2)
            )
            .create();

        ProjectDto dto = service.create(createDto);

        while (
            dto.status() != targetStatus && !dto.status().isFinalOrCanceled()
        )
            dto = service
                .advanceStep(dto.id(), new ProjectNextStepDto(LocalDate.now()));

        if (dto.status() != targetStatus && targetStatus.isCanceled())
            dto = service.cancel(dto.id());

        return dto;
    }

    public static ProjectDto createFinishedWithEndAt(
        final ProjectService service,
        final LocalDate endAt
    ) {
        ProjectDto dto = createBasic(service);
        while (
            dto.status() != ProjectStatus.FINISHED
                && !dto.status().isFinalOrCanceled()
        )
            dto = service.advanceStep(dto.id(), new ProjectNextStepDto(endAt));
        return dto;
    }

    public static Stream<ProjectStatus> deletableStatuses() {
        return Stream.of(ProjectStatus.values())
            .filter(ProjectStatus::canDelete);
    }

    public static Stream<ProjectStatus> nonDeletableStatuses() {
        return Stream.of(ProjectStatus.values()).filter(s -> !s.canDelete());
    }

    public static ProjectFilterDto emptyFilter() {
        return ProjectFilterDto.builder().build();
    }

    public static ProjectFilterDto filterByManagerIds(
        final Set<Long> managerIds
    ) {
        return ProjectFilterDto.builder().managerIds(managerIds).build();
    }

    public static ProjectFilterDto filterByMemberIds(
        final Set<Long> memberIds
    ) {
        return ProjectFilterDto.builder().memberIds(memberIds).build();
    }

    public static ProjectFilterDto filterByName(final String name) {
        return ProjectFilterDto.builder().name(name).build();
    }

    public static ProjectFilterDto filterByStatuses(
        final Set<ProjectStatus> statuses
    ) {
        return ProjectFilterDto.builder().statuses(statuses).build();
    }

    public static ProjectFilterDto filterByStartAt(final LocalDate startAt) {
        return ProjectFilterDto.builder().startAt(startAt).build();
    }

    public static ProjectFilterDto filterByEndAt(final LocalDate endAt) {
        return ProjectFilterDto.builder().endAt(endAt).build();
    }

    public static ProjectFilterDto filterByRiskLevels(
        final Set<RiskLevel> riskLevels
    ) {
        return ProjectFilterDto.builder().riskLevels(riskLevels).build();
    }

}
