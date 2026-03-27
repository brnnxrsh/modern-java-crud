package com.brenner.modern_java_crud.domain;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RiskLevel {

    LOW(new BigDecimal("0.00"), 0),
    MEDIUM(new BigDecimal("100001.00"), 3),
    HIGH(new BigDecimal("500000.01"), 7);

    private final BigDecimal minTotalBudget;
    private final Integer minMonths;

    @Getter
    private static final List<RiskLevel> sortedDescending = Stream.of(values())
        .map(RiskLevel::validate)
        .sorted(
            Comparator.comparing(RiskLevel::getMinTotalBudget)
                .thenComparing(RiskLevel::getMinMonths)
                .reversed()
        )
        .toList();

    private static RiskLevel validate(final RiskLevel targetLevel) {
        if (targetLevel.minTotalBudget.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalStateException(
                "minTotalBudget não pode ser negativo"
            );

        if (targetLevel.minMonths < 0)
            throw new IllegalStateException("minMonths não pode ser negativo");

        boolean isDuplicated = Stream.of(values())
            .anyMatch(
                level -> level != targetLevel
                    && level.minTotalBudget
                        .compareTo(targetLevel.minTotalBudget) == 0
                    && level.minMonths == targetLevel.minMonths
            );

        if (isDuplicated)
            throw new IllegalStateException(
                "minTotalBudget e minMonths duplicados"
            );

        return targetLevel;
    }

    public static RiskLevel getFirstLevel() {
        final RiskLevel firstLevel = sortedDescending.getLast();

        if (firstLevel.minTotalBudget.compareTo(BigDecimal.ZERO) != 0)
            throw new IllegalStateException(
                "minTotalBudget do primeiro nível deve ser 0"
            );

        return firstLevel;
    }

    public boolean isFirstLevel() {
        return this == getFirstLevel();
    }

    private boolean isSatisfiedBy(
        final BigDecimal totalBudget,
        final long months
    ) {
        return totalBudget.compareTo(minTotalBudget) >= 0
            || months >= minMonths;
    }

    public static RiskLevel from(
        final BigDecimal totalBudget,
        final long months
    ) {
        return sortedDescending.stream()
            .filter(riskLevel -> riskLevel.isSatisfiedBy(totalBudget, months))
            .findFirst()
            .orElse(getFirstLevel());
    }

}
