package com.brenner.modern_java_crud.domain;

import static org.assertj.core.api.Assertions.assertThatObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ProjectStatusTest {

    @Test
    void isFinalOrCanceled_shouldReturnTrue_whenFinished() {
        assertThatObject(ProjectStatus.FINISHED.isFinalOrCanceled())
            .isEqualTo(true);
    }

    @Test
    void isFinalOrCanceled_shouldReturnTrue_whenCancelled() {
        assertThatObject(ProjectStatus.CANCELLED.isFinalOrCanceled())
            .isEqualTo(true);
    }

    @ParameterizedTest
    @EnumSource(
        value = ProjectStatus.class,
        names = {
            "FINISHED",
            "CANCELLED"
        },
        mode = EnumSource.Mode.EXCLUDE
    )
    void isFinalOrCanceled_shouldReturnFalse_whenNeitherFinalNorCancelled(
        final ProjectStatus status
    ) {
        assertThatObject(status.isFinalOrCanceled()).isEqualTo(false);
    }

}
