package br.com.financas.validation;

import br.com.financas.dto.MovementRequest;

public class MovementRequestValidator {
    private MovementRequestValidator() {
    }

    public static void validate(MovementRequest request) {
        if (request == null) throw new IllegalArgumentException("Payload inválido");
        String txDate = request.getTxDate() == null ? "" : request.getTxDate().trim();
        if (txDate.isBlank()) throw new IllegalArgumentException("Data é obrigatória");
        String movementType = request.getMovementType() == null ? "" : request.getMovementType().trim();
        if (!movementType.equals("entrada") && !movementType.equals("saida")) throw new IllegalArgumentException("Tipo inválido");
        String description = request.getDescription() == null ? "" : request.getDescription().trim();
        if (description.isBlank()) throw new IllegalArgumentException("Descrição é obrigatória");
    }
}
