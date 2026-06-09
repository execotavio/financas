package br.com.financas.service;

import br.com.financas.dto.MovementRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MovementDomainService extends JdbcFinanceSupport {
    public MovementDomainService(JdbcTemplate jdbc) {
        super(jdbc);
    }

    public Map<String, Object> createMovement(Map<String, Object> payload) {
        String txDate = text(payload.get("tx_date"));
        if (txDate.isBlank()) throw new IllegalArgumentException("Data é obrigatória");
        String movementType = text(payload.get("movement_type"));
        if (!movementType.equals("entrada") && !movementType.equals("saida")) throw new IllegalArgumentException("Tipo inválido");
        String paymentMethod = text(payload.get("payment_method"));
        if (paymentMethod.isBlank()) paymentMethod = "pix";
        String description = text(payload.get("description"));
        if (description.isBlank()) throw new IllegalArgumentException("Descrição é obrigatória");
        long amountCents = parseMoneyToCents(payload.get("amount"));
        jdbc.update("INSERT INTO movements(tx_date, movement_type, payment_method, description, amount_cents, created_at) VALUES(?,?,?,?,?,?)",
                txDate, movementType, paymentMethod, description, amountCents, now());
        return movementRow(lastInsertId(), txDate, movementType, paymentMethod, description, amountCents);
    }

    public Map<String, Object> createMovement(MovementRequest request) {
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
        return movementRow(lastInsertId(), txDate, movementType, paymentMethod, description, amountCents);
    }

    private Map<String, Object> movementRow(long id, String txDate, String movementType, String paymentMethod, String description, long amountCents) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("tx_date", txDate);
        row.put("movement_type", movementType);
        row.put("payment_method", paymentMethod);
        row.put("description", description);
        row.put("amount", centsToAmount(amountCents));
        row.put("category_name", movementType.equals("entrada") ? "Entrada" : "Saída");
        return row;
    }
}
