package com.brenner.modern_java_crud.repository.spec;

import com.brenner.modern_java_crud.domain.Member_;
import com.brenner.modern_java_crud.domain.Project;
import com.brenner.modern_java_crud.domain.ProjectStatus;
import com.brenner.modern_java_crud.domain.Project_;
import com.brenner.modern_java_crud.domain.RiskLevel;
import com.brenner.modern_java_crud.dto.ProjectFilterDto;
import com.brenner.modern_java_crud.util.SpecUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.function.Predicate;

import jakarta.persistence.criteria.CriteriaBuilder.Case;
import jakarta.persistence.criteria.Path;

import org.springframework.data.jpa.domain.Specification;

public class ProjectSpec {

    public static Specification<Project> withManagerIds(
        final Set<Long> managerIds
    ) {
        return SpecUtils.joinIn(Project_.MANAGER, Member_.ID, managerIds);
    }

    public static Specification<Project> withMemberIds(
        final Set<Long> memberIds
    ) {
        return SpecUtils.joinIn(Project_.MEMBERS, Member_.ID, memberIds);
    }

    public static Specification<Project> withName(final String name) {
        return SpecUtils.likeIgnoreCase(Project_.NAME, name);
    }

    public static Specification<Project> withStatuses(
        final Set<ProjectStatus> statuses
    ) {
        return SpecUtils.in(Project_.STATUS, statuses);
    }

    public static Specification<Project> withStartDate(
        final LocalDate startAt
    ) {
        return SpecUtils.greaterOrEqual(Project_.START_AT, startAt);
    }

    /*
    * WHERE fn_effective_date(end_at, expected_end_at) <= ?
    */
    public static Specification<Project> withEndDate(final LocalDate endAt) {
        return SpecUtils.lessOrEqual(Project_.EFFECTIVE_END_AT, endAt);
    }

    /*
    * WHERE (
    *     CASE
    *         WHEN total_budget >= ? OR fn_months_between(start_at, fn_effective_date(end_at, expected_end_at)) >= ? THEN 'HIGH'
    *         WHEN total_budget >= ? OR fn_months_between(start_at, fn_effective_date(end_at, expected_end_at)) >= ? THEN 'MEDIUM'
    *         ELSE 'LOW'
    *     END
    * ) IN ('LOW', 'MEDIUM', 'HIGH')
    */
    public static Specification<Project> withRiskLevels(
        final Set<RiskLevel> riskLevels
    ) {
        return (root, query, cb) -> {
            if (riskLevels == null || riskLevels.isEmpty())
                return null;

            final Path<BigDecimal> totalBudget = root
                .get(Project_.TOTAL_BUDGET);
            final Path<Integer> durationMonths = root
                .get(Project_.DURATION_MONTHS);

            final Case<RiskLevel> cases = cb.<RiskLevel>selectCase();

            RiskLevel.getSortedDescending()
                .stream()
                .filter(Predicate.not(RiskLevel::isFirstLevel))
                .forEach(riskLevel -> {
                    cases.when(
                        cb.or(
                            cb.greaterThanOrEqualTo(
                                totalBudget,
                                riskLevel.getMinTotalBudget()
                            ),
                            cb.greaterThanOrEqualTo(
                                durationMonths,
                                riskLevel.getMinMonths()
                            )
                        ),
                        riskLevel
                    );
                });
            cases.otherwise(RiskLevel.getFirstLevel());
            return cases.in(riskLevels);
        };
    }

    public static Specification<Project> fromFilter(
        final ProjectFilterDto filter
    ) {
        return Specification.allOf(
            withManagerIds(filter.managerIds()),
            withMemberIds(filter.memberIds()),
            withName(filter.name()),
            withStatuses(filter.statuses()),
            withStartDate(filter.startAt()),
            withEndDate(filter.endAt()),
            withRiskLevels(filter.riskLevels())
        );
    }

}
