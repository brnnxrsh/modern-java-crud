package com.brenner.modern_java_crud.util;

import java.util.Objects;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.NONE)
public final class Ensure {

    public static void notNull(final Object entity, final Object... fields) {
        final boolean hasNullField = Stream.of(fields)
            .anyMatch(Objects::isNull);

        if (hasNullField) {
            log.error(
                "[OBJECT-STATE-ERROR] Objeto {} com estado inválido: {}",
                entity.getClass().getSimpleName(),
                entity
            );

            throw new IllegalStateException(
                "Registro com estado não permitido"
            );
        }

    }

}
