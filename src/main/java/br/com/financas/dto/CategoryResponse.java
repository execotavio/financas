package br.com.financas.dto;

public record CategoryResponse(
        Long id,
        String name,
        String color,
        Integer active
) {
}
