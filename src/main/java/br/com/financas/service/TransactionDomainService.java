package br.com.financas.service;

import br.com.financas.dto.SplitRequest;
import br.com.financas.dto.TransactionRequest;
import br.com.financas.dto.TransactionResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionDomainService extends JdbcFinanceSupport {
    public TransactionDomainService(JdbcTemplate jdbc) {
        super(jdbc);
    }

    public List<TransactionResponse> listTransactionsByMonth(String month) {
        if (month == null || month.length() != 7) return List.of();
        String start = month + "-01";
        LocalDate endDate = LocalDate.parse(start).plusMonths(1);
        String end = endDate.format(DateTimeFormatter.ISO_DATE);

        List<TransactionResponse> rows = new ArrayList<>(jdbc.query("""
                SELECT t.id, t.tx_date, t.description, t.amount_cents, t.installment_total,
                       c.name AS card_name,
                       cat.name AS category_name,
                       CASE WHEN EXISTS (SELECT 1 FROM transaction_splits s WHERE s.transaction_id = t.id) THEN 1 ELSE 0 END AS has_splits
                FROM transactions t
                LEFT JOIN cards c ON c.id = t.card_id
                LEFT JOIN categories cat ON cat.id = t.category_id
                WHERE t.tx_date >= ? AND t.tx_date < ?
                ORDER BY t.tx_date ASC, t.id ASC
                """, (rs, i) -> new TransactionResponse(
                rs.getLong("id"),
                rs.getString("tx_date"),
                rs.getString("description"),
                centsToAmount(rs.getLong("amount_cents")),
                rs.getObject("installment_total") == null ? 1 : rs.getInt("installment_total"),
                rs.getString("card_name"),
                rs.getString("category_name"),
                rs.getInt("has_splits") == 1,
                "saida",
                rs.getString("card_name") == null ? "pix" : "card"
        ), start, end));

        rows.addAll(jdbc.query("""
                SELECT id, tx_date, movement_type, payment_method, description, amount_cents
                FROM movements
                WHERE tx_date >= ? AND tx_date < ?
                ORDER BY tx_date ASC, id ASC
                """, (rs, i) -> new TransactionResponse(
                "m-" + rs.getLong("id"),
                rs.getString("tx_date"),
                rs.getString("description"),
                centsToAmount(rs.getLong("amount_cents")),
                1,
                null,
                rs.getString("movement_type").equals("entrada") ? "Entrada" : "Saída",
                false,
                rs.getString("movement_type"),
                rs.getString("payment_method")
        ), start, end));

        rows.sort((a, b) -> String.valueOf(a.txDate()).compareTo(String.valueOf(b.txDate())));
        return rows;
    }

    public TransactionResponse createTransaction(TransactionRequest request) {
        String txDate = text(request.getTxDate());
        if (txDate.isBlank()) throw new IllegalArgumentException("Data é obrigatória");
        String description = text(request.getDescription());
        if (description.isBlank()) throw new IllegalArgumentException("Descrição é obrigatória");
        long amountCents = parseMoneyToCents(request.getAmount());
        Long cardId = request.getCardId();
        Long categoryId = request.getCategoryId();
        Integer installmentTotalValue = request.getInstallmentTotal();
        Integer installmentTotal = installmentTotalValue == null ? 1 : installmentTotalValue;
        List<SplitRequest> splits = request.getSplits() == null ? List.of() : request.getSplits();

        if (!splits.isEmpty()) {
            long splitTotal = 0;
            for (SplitRequest split : splits) {
                splitTotal += parseMoneyToCents(split.getAmount());
            }
            if (splitTotal != amountCents) throw new IllegalArgumentException("A soma dos detalhamentos deve bater com o valor total");
            categoryId = null;
        }

        jdbc.update("""
                INSERT INTO transactions(tx_date, card_id, source, merchant_id, description, normalized_description, amount_cents,
                                         category_id, installment_number, installment_total, parent_group, status, created_at)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                txDate, cardId, "manual", null, description, normalize(description), amountCents,
                categoryId, 1, installmentTotal, null, "open", now());
        long id = lastInsertId();

        for (SplitRequest split : splits) {
            Long splitCategoryId = split.getCategoryId();
            if (splitCategoryId == null) throw new IllegalArgumentException("Categoria do detalhamento é obrigatória");
            String splitDescription = text(split.getDescription());
            long splitAmount = parseMoneyToCents(split.getAmount());
            jdbc.update("INSERT INTO transaction_splits(transaction_id, category_id, description, amount_cents) VALUES(?,?,?,?)",
                    id, splitCategoryId, splitDescription, splitAmount);
        }

        return getTransaction(id);
    }

    private TransactionResponse getTransaction(long id) {
        try {
            return jdbc.queryForObject("""
                    SELECT t.id, t.tx_date, t.description, t.amount_cents, t.installment_total,
                           c.name AS card_name,
                           cat.name AS category_name,
                           CASE WHEN EXISTS (SELECT 1 FROM transaction_splits s WHERE s.transaction_id = t.id) THEN 1 ELSE 0 END AS has_splits
                    FROM transactions t
                    LEFT JOIN cards c ON c.id = t.card_id
                    LEFT JOIN categories cat ON cat.id = t.category_id
                    WHERE t.id = ?
                    """, (rs, i) -> new TransactionResponse(
                    rs.getLong("id"),
                    rs.getString("tx_date"),
                    rs.getString("description"),
                    centsToAmount(rs.getLong("amount_cents")),
                    rs.getObject("installment_total") == null ? 1 : rs.getInt("installment_total"),
                    rs.getString("card_name"),
                    rs.getString("category_name"),
                    rs.getInt("has_splits") == 1,
                    "saida",
                    rs.getString("card_name") == null ? "pix" : "card"
            ), id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Transação não encontrada");
        }
    }
}
