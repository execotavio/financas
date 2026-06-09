package br.com.financas.service;

import br.com.financas.dto.MovementRequest;
import br.com.financas.dto.MovementResponse;
import br.com.financas.validation.MovementRequestValidator;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MovementDomainService extends JdbcFinanceSupport {
    public MovementDomainService(JdbcTemplate jdbc) {
        super(jdbc);
    }

    public MovementResponse createMovement(MovementRequest request) {
        MovementRequestValidator.validate(request);
        String txDate = text(request.getTxDate());
        String movementType = text(request.getMovementType());
        String paymentMethod = text(request.getPaymentMethod());
        if (paymentMethod.isBlank()) paymentMethod = "pix";
        String description = text(request.getDescription());
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

    public MovementResponse updateMovement(Long movementId, MovementRequest request) {
        if (movementId == null || movementId <= 0) {
            throw new IllegalArgumentException("Movimentação inválida");
        }
        MovementRequestValidator.validate(request);
        String txDate = text(request.getTxDate());
        String movementType = text(request.getMovementType());
        String paymentMethod = text(request.getPaymentMethod());
        if (paymentMethod.isBlank()) paymentMethod = "pix";
        String description = text(request.getDescription());
        long amountCents = parseMoneyToCents(request.getAmount());
        int updated = jdbc.update("UPDATE movements SET tx_date=?, movement_type=?, payment_method=?, description=?, amount_cents=? WHERE id=?",
                txDate, movementType, paymentMethod, description, amountCents, movementId);
        if (updated == 0) {
            throw new IllegalArgumentException("Movimentação não encontrada");
        }
        return getMovement(movementId);
    }

    public void deleteMovement(Long movementId) {
        if (movementId == null || movementId <= 0) {
            throw new IllegalArgumentException("Movimentação inválida");
        }
        int deleted = jdbc.update("DELETE FROM movements WHERE id=?", movementId);
        if (deleted == 0) {
            throw new IllegalArgumentException("Movimentação não encontrada");
        }
    }

    private MovementResponse getMovement(Long movementId) {
        try {
            return jdbc.queryForObject("""
                    SELECT id, tx_date, movement_type, payment_method, description, amount_cents
                    FROM movements
                    WHERE id = ?
                    """, (rs, i) -> new MovementResponse(
                    rs.getLong("id"),
                    rs.getString("tx_date"),
                    rs.getString("movement_type"),
                    rs.getString("payment_method"),
                    rs.getString("description"),
                    centsToAmount(rs.getLong("amount_cents")),
                    rs.getString("movement_type").equals("entrada") ? "Entrada" : "Saída"
            ), movementId);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Movimentação não encontrada");
        }
    }
}
