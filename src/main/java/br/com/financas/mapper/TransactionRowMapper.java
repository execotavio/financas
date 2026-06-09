package br.com.financas.mapper;

import br.com.financas.dto.TransactionResponse;
import org.springframework.jdbc.core.RowMapper;

import java.util.function.LongFunction;

public class TransactionRowMapper {
    private TransactionRowMapper() {
    }

    public static RowMapper<TransactionResponse> transactionRowMapper(LongFunction<Double> centsToAmount) {
        return (rs, i) -> new TransactionResponse(
                rs.getLong("id"),
                rs.getString("tx_date"),
                rs.getString("description"),
                centsToAmount.apply(rs.getLong("amount_cents")),
                rs.getObject("installment_total") == null ? 1 : rs.getInt("installment_total"),
                rs.getString("card_name"),
                rs.getString("category_name"),
                rs.getInt("has_splits") == 1,
                "saida",
                rs.getString("card_name") == null ? "pix" : "card"
        );
    }

    public static RowMapper<TransactionResponse> movementRowMapper(LongFunction<Double> centsToAmount) {
        return (rs, i) -> new TransactionResponse(
                "m-" + rs.getLong("id"),
                rs.getString("tx_date"),
                rs.getString("description"),
                centsToAmount.apply(rs.getLong("amount_cents")),
                1,
                null,
                rs.getString("movement_type").equals("entrada") ? "Entrada" : "Saída",
                false,
                rs.getString("movement_type"),
                rs.getString("payment_method")
        );
    }
}
