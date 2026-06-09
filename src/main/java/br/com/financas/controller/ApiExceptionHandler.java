package br.com.financas.controller;

import br.com.financas.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        return error("BAD_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGeneric(Exception ex) {
        return error("INTERNAL_ERROR", ex.getMessage() == null ? "Erro interno" : ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    private ApiErrorResponse error(String code, String message, int status) {
        return new ApiErrorResponse(code, message, Map.of("status", status), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
    }
}
