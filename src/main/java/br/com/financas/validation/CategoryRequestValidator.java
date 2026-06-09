package br.com.financas.validation;

import br.com.financas.dto.CategoryRequest;

public class CategoryRequestValidator {
    private CategoryRequestValidator() {
    }

    public static void validate(CategoryRequest request) {
        if (request == null) throw new IllegalArgumentException("Payload inválido");
        String name = request.getName() == null ? "" : request.getName().trim();
        if (name.isBlank()) throw new IllegalArgumentException("Nome da categoria é obrigatório");
    }
}
