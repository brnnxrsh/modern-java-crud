package com.brenner.modern_java_crud.exception;

import static org.assertj.core.api.Assertions.assertThatObject;

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
            "http://mock-api/resource/1",
            Map.of(),
            Request.Body.empty(),
            null
        );
    }

    @Test
    void handleFeignNotFoundException_shouldReturnNotFoundProblemDetail() {
        final FeignException exception = FeignException.errorStatus(
            "GET",
            feign.Response.builder()
                .status(404)
                .reason("Not Found")
                .request(buildRequest())
                .build()
        );

        final ProblemDetail result = handler
            .handleFeignNotFoundException((FeignException.NotFound) exception);

        assertThatObject(result.getStatus())
            .isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThatObject(result.getDetail())
            .isEqualTo("O recurso informado não existe no serviço externo.");
        assertThatObject(result.getType().toString())
            .isEqualTo("urn:portfolio:error:feign-not-found");
    }

    @Test
    void handleFeignException_shouldReturnBadGatewayProblemDetail() {
        final FeignException exception = FeignException.errorStatus(
            "GET",
            feign.Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(buildRequest())
                .build()
        );

        final ProblemDetail result = handler.handleFeignException(exception);

        assertThatObject(result.getStatus())
            .isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThatObject(result.getDetail())
            .isEqualTo("Erro na comunicação com o serviço externo.");
        assertThatObject(result.getType().toString())
            .isEqualTo("urn:portfolio:error:feign-generic-error");
    }

}
