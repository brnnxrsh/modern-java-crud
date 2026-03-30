package com.brenner.modern_java_crud.exception;

import static org.assertj.core.api.Assertions.assertThatObject;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import feign.FeignException;
import feign.Request;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@SpringJUnitConfig
@ActiveProfiles("test")
@ContextConfiguration(
    classes = GlobalExceptionHandlerTest.TestConfig.class,
    initializers = ConfigDataApplicationContextInitializer.class
)
class GlobalExceptionHandlerTest {

    @TestConfiguration(proxyBeanMethods = false)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class TestConfig {}

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Value("${clients.member.url}")
    private String memberUrl;

    private Request buildRequest() {
        return Request.create(
            Request.HttpMethod.GET,
            memberUrl + "/resource/1",
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
    void handleCallNotPermitted_shouldReturnServiceUnavailableProblemDetail() {
        final var exception = CallNotPermittedException
            .createCallNotPermittedException(
                CircuitBreaker.ofDefaults("member-client")
            );

        final ProblemDetail result = handler.handleCallNotPermitted(exception);

        assertThatObject(result.getStatus())
            .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThatObject(result.getDetail())
            .isEqualTo("O serviço externo está temporariamente indisponível.");
        assertThatObject(result.getType().toString())
            .isEqualTo("urn:portfolio:error:service-unavailable");
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
