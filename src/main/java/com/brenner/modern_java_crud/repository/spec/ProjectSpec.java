package com.brenner.modern_java_crud.repository.spec;

import com.brenner.modern_java_crud.domain.Project;
import com.brenner.modern_java_crud.domain.ProjectStatus;
import com.brenner.modern_java_crud.domain.Project_;
import com.brenner.modern_java_crud.domain.RiskLevel;
import com.brenner.modern_java_crud.dto.ProjectFilterDto;
import com.brenner.modern_java_crud.util.SpecUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Predicate;

import jakarta.persistence.criteria.CriteriaBuilder.Case;
import jakarta.persistence.criteria.Path;

import org.springframework.data.jpa.domain.Specification;

public class ProjectSpec {

    public static Specification<Project> withName(final String name) {
        return SpecUtils.likeIgnoreCase(Project_.NAME, name);
    }

    public static Specification<Project> withStatus(
        final ProjectStatus status
    ) {
        return SpecUtils.equals(Project_.STATUS, status);
    }

    public static Specification<Project> withStartDate(
        final LocalDate startAt
    ) {
        return SpecUtils.greaterOrEqual(Project_.START_AT, startAt);
    }

    public static Specification<Project> withEndDate(final LocalDate endAt) {
        return SpecUtils.lessOrEqual(Project_.EFFECTIVE_END_AT, endAt);
    }

    public static Specification<Project> withRiskLevel(
        final RiskLevel targetRiskLevel
    ) {
        return (root, query, cb) -> {
            if (targetRiskLevel == null)
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

            return cb.equal(cases, targetRiskLevel);
        };
    }

    public static Specification<Project> fromFilter(
        final ProjectFilterDto filter
    ) {
        return Specification.allOf(
            withName(filter.name()),
            withStatus(filter.status()),
            withStartDate(filter.startAt()),
            withEndDate(filter.endAt()),
            withRiskLevel(filter.riskLevel())
        );
    }

}
