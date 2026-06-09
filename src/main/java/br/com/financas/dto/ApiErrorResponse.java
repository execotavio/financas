package br.com.financas.dto;

import java.util.Map;

public record ApiErrorResponse(String code, String message, Map<String, Object> details, String timestamp) {
}
