package com.brenner.modern_java_crud.service;

import static com.brenner.modern_java_crud.service.ProjectTestFixtures.MANAGER_DTO;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.MEMBER_DTO;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.MEMBER_DTO_3;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.createBasic;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.createBasicAndAdvanceTo;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.createWithDates;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.createWithDurationMonths;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.createWithMembers;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.createWithName;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.createWithTotalBudget;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.emptyFilter;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.filterByEndAt;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.filterByManagerIds;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.filterByMemberIds;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.filterByName;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.filterByRiskLevels;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.filterByStartAt;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.filterByStatuses;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.updateWithDurationMonths;
import static com.brenner.modern_java_crud.service.ProjectTestFixtures.updateWithTotalBudget;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.brenner.modern_java_crud.TestcontainersConfiguration;
import com.brenner.modern_java_crud.domain.ProjectStatus;
import com.brenner.modern_java_crud.domain.RiskLevel;
import com.brenner.modern_java_crud.dto.ProjectDto;
import com.brenner.modern_java_crud.dto.ProjectNextStepDto;
import com.brenner.modern_java_crud.exception.BusinessException;
import com.brenner.modern_java_crud.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class ProjectServiceIT {

    static Stream<Arguments> riskLevelByBudgetCases() {
        return ProjectTestFixtures.riskLevelByBudgetCases();
    }

    static Stream<Arguments> riskLevelUpdateByBudgetCases() {
        return ProjectTestFixtures.riskLevelUpdateByBudgetCases();
    }

    static Stream<Arguments> riskLevelByDurationCases() {
        return ProjectTestFixtures.riskLevelByDurationCases();
    }

    static Stream<Arguments> riskLevelUpdateByDurationCases() {
        return ProjectTestFixtures.riskLevelUpdateByDurationCases();
    }

    static Stream<ProjectStatus> deletableStatuses() {
        return ProjectTestFixtures.deletableStatuses();
    }

    static Stream<ProjectStatus> nonDeletableStatuses() {
        return ProjectTestFixtures.nonDeletableStatuses();
    }

    @Autowired
    private ProjectService service;

    @ParameterizedTest
    @MethodSource("riskLevelByBudgetCases")
    void create_shouldCalculateCorrectRiskLevelByTotalBudget(
        final BigDecimal totalBudget,
        final RiskLevel riskLevel
    ) {
        final ProjectDto dto = createWithTotalBudget(service, totalBudget);
        assertThatObject(dto.totalBudget()).isEqualTo(totalBudget);
        assertThatObject(dto.riskLevel()).isEqualTo(riskLevel);
    }

    @ParameterizedTest
    @MethodSource("riskLevelUpdateByBudgetCases")
    void update_shouldCalculateCorrectRiskLevelByTotalBudget(
        final BigDecimal oldTotalBudget,
        final BigDecimal newTotalBudget,
        final RiskLevel oldRiskLevel,
        final RiskLevel newRiskLevel
    ) {
        final ProjectDto oldDto = createWithTotalBudget(
            service,
            oldTotalBudget
        );
        assertThatObject(oldDto.totalBudget()).isEqualTo(oldTotalBudget);
        assertThatObject(oldDto.riskLevel()).isEqualTo(oldRiskLevel);

        final ProjectDto newDto = updateWithTotalBudget(
            service,
            oldDto,
            newTotalBudget
        );
        assertThatObject(newDto.totalBudget()).isEqualTo(newTotalBudget);
        assertThatObject(newDto.riskLevel()).isEqualTo(newRiskLevel);
    }

    @ParameterizedTest()
    @MethodSource("riskLevelByDurationCases")
    void create_shouldCalculateCorrectRiskLevelByDurationMonths(
        final Integer durationMonths,
        final RiskLevel riskLevel
    ) {
        final ProjectDto dto = createWithDurationMonths(
            service,
            durationMonths
        );
        assertThatObject(dto.durationMonths()).isEqualTo(durationMonths);
        assertThatObject(dto.riskLevel()).isEqualTo(riskLevel);
    }

    @ParameterizedTest()
    @MethodSource("riskLevelUpdateByDurationCases")
    void update_shouldCalculateCorrectRiskLevelByDurationMonths(
        final Integer oldDurationMonths,
        final Integer newDurationMonths,
        final RiskLevel oldRiskLevel,
        final RiskLevel newRiskLevel
    ) {
        final ProjectDto oldDto = createWithDurationMonths(
            service,
            oldDurationMonths
        );
        assertThatObject(oldDto.durationMonths()).isEqualTo(oldDurationMonths);
        assertThatObject(oldDto.riskLevel()).isEqualTo(oldRiskLevel);

        final ProjectDto newDto = updateWithDurationMonths(
            service,
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
    @MethodSource("deletableStatuses")
    void delete_shouldDelete_whenAllowed(final ProjectStatus status) {
        final ProjectDto dto = createBasicAndAdvanceTo(service, status);

        service.delete(dto.id());

        assertThatThrownBy(() -> service.find(dto.id()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @ParameterizedTest
    @MethodSource("nonDeletableStatuses")
    void delete_shouldThrow_whenNotAllowed(final ProjectStatus status) {
        final ProjectDto dto = createBasicAndAdvanceTo(service, status);

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
        final ProjectDto dto = createBasicAndAdvanceTo(service, targetStatus);
        assertThatObject(dto.status()).isEqualTo(targetStatus);
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class)
    void cancel_shouldCancelSuccessfully(final ProjectStatus targetStatus) {
        final ProjectDto dto = createBasicAndAdvanceTo(service, targetStatus);
        final ProjectDto newDto = service.cancel(dto.id());
        assertThatObject(newDto.status()).isEqualTo(ProjectStatus.CANCELLED);
    }

    @Test
    void find_shouldReturnProject_whenExists() {
        final ProjectDto created = createBasic(service);
        final ProjectDto found = service.find(created.id());
        assertThatObject(found.id()).isEqualTo(created.id());
    }

    @Test
    void findAll_shouldReturnAllProjects_whenNoFilterApplied() {
        createBasic(service);
        createBasic(service);

        final var page = service.findAll(emptyFilter(), Pageable.unpaged());

        assertThatObject(page.getTotalElements()).isEqualTo(2L);
    }

    @Test
    void findAll_shouldFilterByName() {
        createWithName(service, "UNIQUE_ALPHA_TEST_PROJECT");
        createWithName(service, "UNIQUE_BETA_TEST_PROJECT");

        final var page = service
            .findAll(filterByName("ALPHA"), Pageable.unpaged());

        assertThatObject(page.getTotalElements()).isEqualTo(1L);
        assertThatObject(page.getContent().get(0).name())
            .isEqualTo("UNIQUE_ALPHA_TEST_PROJECT");
    }

    @Test
    void findAll_shouldFilterByStatus() {
        final ProjectDto inReview = createBasic(service);
        service.advanceStep(
            createBasic(service).id(),
            new ProjectNextStepDto(LocalDate.now())
        );

        final var page = service.findAll(
            filterByStatuses(Set.of(ProjectStatus.IN_REVIEW)),
            Pageable.unpaged()
        );

        assertThatObject(page.getTotalElements()).isEqualTo(1L);
        assertThatObject(page.getContent().get(0).id())
            .isEqualTo(inReview.id());
    }

    @Test
    void findAll_shouldFilterByManagerId() {
        createBasic(service);

        final var filterMatch = filterByManagerIds(Set.of(MANAGER_DTO.id()));
        final var filterNoMatch = filterByManagerIds(Set.of(9999L));

        assertThatObject(
            service.findAll(filterMatch, Pageable.unpaged()).getTotalElements()
        ).isEqualTo(1L);
        assertThatObject(
            service.findAll(filterNoMatch, Pageable.unpaged())
                .getTotalElements()
        ).isEqualTo(0L);
    }

    @Test
    void findAll_shouldFilterByMemberId() {
        final ProjectDto withMember2 = createWithMembers(
            service,
            Set.of(MEMBER_DTO)
        );
        createWithMembers(service, Set.of(MEMBER_DTO_3));

        final var page = service.findAll(
            filterByMemberIds(Set.of(MEMBER_DTO.id())),
            Pageable.unpaged()
        );

        assertThatObject(page.getTotalElements()).isEqualTo(1L);
        assertThatObject(page.getContent().get(0).id())
            .isEqualTo(withMember2.id());
    }

    @Test
    void findAll_shouldFilterByStartAt() {
        createWithDates(service, LocalDate.now(), LocalDate.now());
        final ProjectDto future = createWithDates(
            service,
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(10)
        );

        final var page = service.findAll(
            filterByStartAt(LocalDate.now().plusDays(5)),
            Pageable.unpaged()
        );

        assertThatObject(page.getTotalElements()).isEqualTo(1L);
        assertThatObject(page.getContent().get(0).id()).isEqualTo(future.id());
    }

    @Test
    void findAll_shouldFilterByEndAt() {
        final ProjectDto endsToday = createWithDurationMonths(service, 0);
        createWithDurationMonths(service, 1);

        final var page = service
            .findAll(filterByEndAt(LocalDate.now()), Pageable.unpaged());

        assertThatObject(page.getTotalElements()).isEqualTo(1L);
        assertThatObject(page.getContent().get(0).id())
            .isEqualTo(endsToday.id());
    }

    @ParameterizedTest
    @MethodSource("riskLevelByBudgetCases")
    void findAll_shouldFilterByRiskLevel_byBudget(
        final BigDecimal totalBudget,
        final RiskLevel riskLevel
    ) {
        createWithTotalBudget(service, totalBudget);

        final var page = service
            .findAll(filterByRiskLevels(Set.of(riskLevel)), Pageable.unpaged());

        assertThatObject(page.getTotalElements()).isEqualTo(1L);
        assertThatObject(page.getContent().get(0).riskLevel())
            .isEqualTo(riskLevel);
    }

    @ParameterizedTest
    @MethodSource("riskLevelByDurationCases")
    void findAll_shouldFilterByRiskLevel_byDuration(
        final Integer durationMonths,
        final RiskLevel riskLevel
    ) {
        createWithDurationMonths(service, durationMonths);

        final var page = service
            .findAll(filterByRiskLevels(Set.of(riskLevel)), Pageable.unpaged());

        assertThatObject(page.getTotalElements()).isEqualTo(1L);
        assertThatObject(page.getContent().get(0).riskLevel())
            .isEqualTo(riskLevel);
    }

}
