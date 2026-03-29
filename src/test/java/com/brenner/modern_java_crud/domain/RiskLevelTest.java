package com.brenner.modern_java_crud.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatObject;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RiskLevelTest {

    @Test
    void getFirstLevel_shouldReturnLow() {
        assertThatObject(RiskLevel.getFirstLevel()).isEqualTo(RiskLevel.LOW);
    }

    @Test
    void getFirstLevel_shouldNotThrow_whenEnumDefinitionIsValid() {
        assertThatCode(RiskLevel::getFirstLevel).doesNotThrowAnyException();
    }

    @Test
    void isFirstLevel_shouldReturnTrue_whenLow() {
        assertThatObject(RiskLevel.LOW.isFirstLevel()).isEqualTo(true);
    }

    @ParameterizedTest
    @EnumSource(
        value = RiskLevel.class,
        names = "LOW",
        mode = EnumSource.Mode.EXCLUDE
    )
    void isFirstLevel_shouldReturnFalse_whenNotLow(final RiskLevel level) {
        assertThatObject(level.isFirstLevel()).isEqualTo(false);
    }

    @Test
    void sortedDescending_shouldBeOrderedFromHighToLow() {
        assertThatObject(RiskLevel.getSortedDescending()).isEqualTo(
            List.of(RiskLevel.HIGH, RiskLevel.MEDIUM, RiskLevel.LOW)
        );
    }

    @Test
    void validate_shouldNotThrow_whenEnumDefinitionHasNoViolations() {
        assertThatCode(RiskLevel::values).doesNotThrowAnyException();
    }

}
