package com.brenner.modern_java_crud.exception;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleDtoException(
        final MethodArgumentNotValidException e
    ) {
        final Map<String, List<String>> groupedErrorMessages = e
            .getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(
                groupingBy(
                    FieldError::getField,
                    mapping(FieldError::getDefaultMessage, toList())
                )
            );

        return buildProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Um ou mais campos de DTO estão inválidos.",
            "dto-constraint",
            Map.of("errors", groupedErrorMessages)
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleEntityException(
        final ConstraintViolationException exception
    ) {
        final Map<String, List<String>> groupedErrorMessages = exception
            .getConstraintViolations()
            .stream()
            .collect(
                groupingBy(
                    constraint -> constraint.getPropertyPath().toString(),
                    mapping(ConstraintViolation::getMessage, toList())
                )
            );

        return buildProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Um ou mais campos de entidade estão inválidos.",
            "entity-constraint",
            Map.of("errors", groupedErrorMessages)
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(
        final DataIntegrityViolationException e
    ) {
        return buildProblemDetail(
            HttpStatus.CONFLICT,
            "Erro de integridade de dados.",
            "data-integrity"
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleInvalidRequestBody(
        final HttpMessageNotReadableException e
    ) {
        return buildProblemDetail(
            HttpStatus.BAD_REQUEST,
            "O corpo da requisição possui erros de sintaxe ou formatos de dados inválidos",
            "request-body"
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleInvalidQueryParam(
        final MethodArgumentTypeMismatchException e
    ) {
        return buildProblemDetail(
            HttpStatus.BAD_REQUEST,
            "A requisição possui parâmetros com erros de sintaxe ou formatos de dados inválidos",
            "request-query-param"
        );
    }

    @ExceptionHandler(NullPointerException.class)
    public ProblemDetail handleNullPointer(final NullPointerException e) {
        return buildProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            String.format(
                "Uma dependência obrigatória não foi fornecida (%s)",
                e.getMessage()
            ),
            "null-pointer"
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(
        final ResourceNotFoundException e
    ) {
        return buildProblemDetail(
            HttpStatus.NOT_FOUND,
            e.getMessage(),
            "resource-not-found"
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(final BusinessException e) {
        return buildProblemDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            e.getMessage(),
            "business-rule"
        );
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLockingFailureException(
        final ObjectOptimisticLockingFailureException e
    ) {
        return buildProblemDetail(
            HttpStatus.CONFLICT,
            "Os dados foram atualizados por outro usuário ou processo. Por favor, recarregue os dados.",
            "conflict"
        );
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ProblemDetail handleFeignNotFoundException(
        final FeignException.NotFound e
    ) {
        final var errorMessage = "O membro informado não existe no serviço externo de membros (Mock API).";

        log.warn(
            "[MEMBER-CLIENT] {} | URL: {}",
            errorMessage,
            e.request().url()
        );

        return buildProblemDetail(
            HttpStatus.NOT_FOUND,
            errorMessage,
            "member-client-not-found"
        );
    }

    @ExceptionHandler(FeignException.class)
    public ProblemDetail handleFeignException(final FeignException e) {
        final var errorMessage = "Erro na comunicação com o serviço externo de membros (Mock API).";

        log.error(
            "[MEMBER-CLIENT] {} | URL: {}",
            errorMessage,
            e.request().url(),
            e
        );

        return buildProblemDetail(
            HttpStatus.BAD_GATEWAY,
            errorMessage,
            "member-client-generic-error"
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(final Exception e) {
        log.error("Erro inesperado no servidor: ", e);

        return buildProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocorreu um erro interno inesperado no servidor.",
            "internal-server-error"
        );
    }

    private ProblemDetail buildProblemDetail(
        final HttpStatus status,
        final String detail,
        final String errorType
    ) {
        final ProblemDetail problemDetail = ProblemDetail
            .forStatusAndDetail(status, detail);
        problemDetail.setType(URI.create("urn:portfolio:error:" + errorType));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    private ProblemDetail buildProblemDetail(
        final HttpStatus status,
        final String detail,
        final String errorType,
        final Map<String, Object> properties
    ) {
        final ProblemDetail problemDetail = buildProblemDetail(
            status,
            detail,
            errorType
        );
        problemDetail.setProperties(properties);
        return problemDetail;
    }

}
