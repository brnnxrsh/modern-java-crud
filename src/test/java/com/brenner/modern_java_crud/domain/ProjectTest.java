package com.brenner.modern_java_crud.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import com.brenner.modern_java_crud.exception.BusinessException;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class ProjectTest {

    protected record Action(
        Consumer<Project> targetMethod,
        List<Consumer<Project>> methodsDoNothing
    ) {}

    protected void runTestOnAllActions(
        final List<Action> actions,
        final Project entity,
        final BiConsumer<Runnable, Project> callback
    ) {
        actions.forEach(action -> {
            final var spyEntity = spy(entity);

            action.methodsDoNothing.forEach(method -> {
                doNothing().when(spyEntity);
                method.accept(spyEntity);
            });

            callback
                .accept(() -> action.targetMethod.accept(spyEntity), spyEntity);
        });
    }

    @Nested
    @DisplayName("Tests for fillStatus method")
    class FillStatusTests {

        @Test
        void fillStatus_ShouldThrowException_WhenNewStatusIsNull() {
            final var entity = Instancio.create(Project.class);
            assertThatThrownBy(() -> entity.fillStatus(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void fillStatus_ShouldThrowException_WhenCurrentStatusIsNull() {
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStatus), null)
                .create();
            final var status = Instancio.create(ProjectStatus.class);
            assertThatThrownBy(() -> entity.fillStatus(status))
                .isInstanceOf(IllegalStateException.class);
        }

        @ParameterizedTest
        @EnumSource(ProjectStatus.class)
        void fillStatus_ShouldAllowCancelledStatus_FromAnyCurrentStatus(
            final ProjectStatus status
        ) {
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStatus), status)
                .create();
            entity.fillStatus(ProjectStatus.CANCELLED);
            assertThatObject(entity.getStatus())
                .isEqualTo(ProjectStatus.CANCELLED);
        }

        @ParameterizedTest
        @EnumSource(
            value = ProjectStatus.class,
            names = {
                "CANCELLED"
            },
            mode = EnumSource.Mode.EXCLUDE
        )
        void fillStatus_ShouldOnlyAllowNextStatus_FromAnyCurrentStatusExceptCancelled(
            final ProjectStatus currentStatus
        ) {

            for (
                final var targetStatus : EnumSet
                    .complementOf(EnumSet.of(ProjectStatus.CANCELLED))
            ) {
                final var entity = Instancio.of(Project.class)
                    .set(field(Project::getStatus), currentStatus)
                    .create();

                if (currentStatus.canChangeTo(targetStatus)) {
                    entity.fillStatus(targetStatus);
                    assertThatObject(entity.getStatus())
                        .isEqualTo(targetStatus);
                }
                else {
                    assertThatThrownBy(() -> entity.fillStatus(targetStatus))
                        .isInstanceOf(BusinessException.class);
                }

            }

        }

    }

    @Nested
    @DisplayName("Tests for fillNextStatus method")
    class FillNextStatusTests {

        @ParameterizedTest
        @EnumSource(ProjectStatus.class)
        void fillNextStatus_ShouldAllowCallFillStatusMethod_FromAnyNextStatus(
            final ProjectStatus status
        ) {
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStatus), status)
                .create();
            entity.fillNextStatus();
            assertThatObject(entity.getStatus()).isEqualTo(status.getNext());
        }

    }

    @Nested
    @DisplayName("Tests for fillCancelStatus method")
    class FillCancelStatusTests {

        @ParameterizedTest
        @EnumSource(ProjectStatus.class)
        void fillCancelStatus_ShouldAllowCallFillStatusMethod_FromAnyCurrentStatus(
            final ProjectStatus status
        ) {
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStatus), status)
                .create();
            entity.fillCancelStatus();
            assertThatObject(entity.getStatus())
                .isEqualTo(ProjectStatus.CANCELLED);
        }

    }

    @Nested
    @DisplayName("Tests for validateDateRange method")
    class ValidateDateRangeTests {

        private final List<Action> actions = List.of(
            new Action(Project::validateCreate, List.of()),
            new Action(
                Project::validateUpdate,
                List.of(Project::validateEndDate)
            )
        );

        @Test
        void shouldSuccessfully_WhenRangeIsValid() {
            final var today = LocalDate.now();
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStartAt), today)
                .set(field(Project::getExpectedEndAt), today)
                .create();

            runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                assertThatCode(method::run).doesNotThrowAnyException();
            });
        }

        @Test
        void shouldThrowException_WhenStartDateIsNull() {
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStartAt), null)
                .create();

            runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                assertThatThrownBy(method::run)
                    .isInstanceOf(IllegalStateException.class);
            });
        }

        @Test
        void shouldThrowException_WhenExpectedEndDateIsNull() {
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getExpectedEndAt), null)
                .create();

            runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                assertThatThrownBy(method::run)
                    .isInstanceOf(IllegalStateException.class);
            });
        }

        @Test
        void shouldThrowException_WhenRangeIsInvalid() {
            final var today = LocalDate.now();
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStartAt), today)
                .set(field(Project::getExpectedEndAt), today.minusDays(1))
                .create();

            runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                assertThatThrownBy(method::run)
                    .isInstanceOf(BusinessException.class);
            });
        }

    }

    @Nested
    @DisplayName("Tests for validateEndDate method")
    class ValidateEndDateTests {

        private final List<Action> actions = List.of(
            new Action(
                Project::validateUpdate,
                List.of(Project::validateDateRange)
            )
        );

        @Test
        void shouldSuccessfully_WhenEndDateIsValid() {
            final var today = LocalDate.now();
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStartAt), today)
                .set(field(Project::getEndAt), today)
                .create();

            runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                assertThatCode(method::run).doesNotThrowAnyException();
            });
        }

        @Test
        void shouldThrowException_WhenStatusIsNull() {
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStatus), null)
                .create();

            runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                assertThatThrownBy(method::run)
                    .isInstanceOf(IllegalStateException.class);
            });
        }

        @Test
        void shouldThrowException_WhenStatusIsFinalAndEndDateIsNull() {
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStatus), ProjectStatus.getFinal())
                .set(field(Project::getEndAt), null)
                .create();

            runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                assertThatThrownBy(method::run)
                    .isInstanceOf(BusinessException.class);
            });
        }

        @Test
        void shouldThrowException_WhenStartDateIsNull() {
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStartAt), null)
                .create();

            runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                assertThatThrownBy(method::run)
                    .isInstanceOf(IllegalStateException.class);
            });
        }

        @Test
        void shouldThrowException_WhenEndDateIsBeforeStartDate() {
            final var today = LocalDate.now();
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStartAt), today)
                .set(field(Project::getEndAt), today.minusDays(1))
                .create();

            runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                assertThatThrownBy(method::run)
                    .isInstanceOf(BusinessException.class);
            });
        }

    }

    @Nested
    @DisplayName("Tests for validateDelete method")
    class ValidateDeleteTests {

        private final List<Action> actions = List
            .of(new Action(Project::validateDelete, List.of()));

        @ParameterizedTest
        @EnumSource(ProjectStatus.class)
        void shouldSuccessfully_WhenStatusCanDelete(
            final ProjectStatus status
        ) {
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStatus), status)
                .create();

            if (entity.getStatus().canDelete()) {
                runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                    assertThatCode(method::run).doesNotThrowAnyException();
                });
            }
            else {
                runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                    assertThatThrownBy(method::run)
                        .isInstanceOf(BusinessException.class);
                });
            }

        }

        @Test
        void shouldThrowException_WhenStatusIsNull() {
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStatus), null)
                .create();

            runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                assertThatThrownBy(method::run)
                    .isInstanceOf(IllegalStateException.class);
            });
        }

    }

    @Nested
    @DisplayName("Tests for prePersist method")
    class PrePersistTests {

        private final List<Action> actions = List.of(
            new Action(Project::prePersist, List.of(Project::validateCreate))
        );

        @Test
        void shouldSuccessfully_WhenFillInitialStatus() {
            final var entity = Instancio.of(Project.class)
                .set(field(Project::getStatus), null)
                .create();

            runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                method.run();
                assertThatObject(spyEntity.getStatus())
                    .isEqualTo(ProjectStatus.getInitial());
            });
        }

    }

    @Nested
    @DisplayName("Tests for preUpdate method")
    class PreUpdateTests {

        private final List<Action> actions = List.of(
            new Action(Project::preUpdate, List.of(Project::validateUpdate))
        );

        @Test
        void shouldSuccessfully() {
            final var entity = Instancio.create(Project.class);

            runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                method.run();
            });
        }

    }

    @Nested
    @DisplayName("Tests for preRemove method")
    class PreRemoveTests {

        private final List<Action> actions = List.of(
            new Action(Project::preRemove, List.of(Project::validateDelete))
        );

        @Test
        void shouldSuccessfully() {
            final var entity = Instancio.create(Project.class);

            runTestOnAllActions(actions, entity, (method, spyEntity) -> {
                method.run();
            });
        }

    }

}
