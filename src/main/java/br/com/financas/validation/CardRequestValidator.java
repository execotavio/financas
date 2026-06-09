package br.com.financas.validation;

import br.com.financas.dto.CardRequest;

public class CardRequestValidator {
    private CardRequestValidator() {
    }

    public static void validate(CardRequest request) {
        if (request == null) throw new IllegalArgumentException("Payload inválido");
        String name = request.getName() == null ? "" : request.getName().trim();
        if (name.isBlank()) throw new IllegalArgumentException("Nome do cartão é obrigatório");
    }
}
