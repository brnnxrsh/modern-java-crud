package com.brenner.modern_java_crud.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import feign.FeignException;
import feign.Request;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private Request buildRequest() {
        return Request.create(
            Request.HttpMethod.GET,
            "http://mock-api/members/1",
            Map.of(),
            (byte[]) null,
            null
        );
    }

    @Test
    void handleFeignNotFoundException_shouldReturnNotFoundProblemDetail() {
        final var exception = new FeignException.NotFound(
            "Not Found",
            buildRequest(),
            null,
            Map.of()
        );

        final ProblemDetail result = handler
            .handleFeignNotFoundException(exception);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getDetail()).isEqualTo(
            "O membro informado não existe no serviço externo de membros (Mock API)."
        );
        assertThat(result.getType().toString())
            .isEqualTo("urn:portfolio:error:member-client-not-found");
        assertThat(result.getProperties()).containsKey("timestamp");
    }

    @Test
    void handleFeignException_shouldReturnBadGatewayProblemDetail() {
        final var exception = new FeignException.InternalServerError(
            "Internal Server Error",
            buildRequest(),
            null,
            Map.of()
        );

        final ProblemDetail result = handler.handleFeignException(exception);

        assertThat(result.getStatus())
            .isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(result.getDetail()).isEqualTo(
            "Erro na comunicação com o serviço externo de membros (Mock API)."
        );
        assertThat(result.getType().toString())
            .isEqualTo("urn:portfolio:error:member-client-generic-error");
        assertThat(result.getProperties()).containsKey("timestamp");
    }

}
