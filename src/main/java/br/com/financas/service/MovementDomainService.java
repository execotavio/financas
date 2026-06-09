package br.com.financas.service;

import br.com.financas.dto.MovementRequest;
import br.com.financas.dto.MovementResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MovementDomainService extends JdbcFinanceSupport {
    public MovementDomainService(JdbcTemplate jdbc) {
        super(jdbc);
    }

    public MovementResponse createMovement(MovementRequest request) {
        String txDate = text(request.getTxDate());
        if (txDate.isBlank()) throw new IllegalArgumentException("Data é obrigatória");
        String movementType = text(request.getMovementType());
        if (!movementType.equals("entrada") && !movementType.equals("saida")) throw new IllegalArgumentException("Tipo inválido");
        String paymentMethod = text(request.getPaymentMethod());
        if (paymentMethod.isBlank()) paymentMethod = "pix";
        String description = text(request.getDescription());
        if (description.isBlank()) throw new IllegalArgumentException("Descrição é obrigatória");
        long amountCents = parseMoneyToCents(request.getAmount());
        jdbc.update("INSERT INTO movements(tx_date, movement_type, payment_method, description, amount_cents, created_at) VALUES(?,?,?,?,?,?)",
                txDate, movementType, paymentMethod, description, amountCents, now());
        return new MovementResponse(
                lastInsertId(),
                txDate,
                movementType,
                paymentMethod,
                description,
                centsToAmount(amountCents),
                movementType.equals("entrada") ? "Entrada" : "Saída"
        );
    }
}
