package br.com.financas.service;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class FinanceService {
    private final JdbcTemplate jdbc;

    public FinanceService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> listCards() {
        return jdbc.query("SELECT id, name, bank, last_digits, closing_day, due_day, active FROM cards ORDER BY id DESC", (rs, i) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("name", rs.getString("name"));
            row.put("bank", rs.getString("bank"));
            row.put("last_digits", rs.getString("last_digits"));
            row.put("closing_day", rs.getInt("closing_day"));
            row.put("due_day", rs.getInt("due_day"));
            row.put("active", rs.getInt("active"));
            return row;
        });
    }

    public Map<String, Object> createCard(Map<String, Object> payload) {
        String name = text(payload.get("name"));
        if (name.isBlank()) throw new IllegalArgumentException("Nome do cartão é obrigatório");
        String bank = text(payload.get("bank"));
        String lastDigits = text(payload.get("last_digits"));
        int closingDay = intValue(payload.get("closing_day"), 28);
        int dueDay = intValue(payload.get("due_day"), 10);
        jdbc.update("INSERT INTO cards(name, bank, last_digits, closing_day, due_day, active, created_at) VALUES(?,?,?,?,?,?,?)",
                name, bank, lastDigits, closingDay, dueDay, 1, now());
        return getCard(lastInsertId());
    }

    public List<Map<String, Object>> listCategories() {
        return jdbc.query("SELECT id, name, color, active FROM categories ORDER BY id DESC", (rs, i) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("name", rs.getString("name"));
            row.put("color", rs.getString("color"));
            row.put("active", rs.getInt("active"));
            return row;
        });
    }

    public Map<String, Object> createCategory(Map<String, Object> payload) {
        String name = text(payload.get("name"));
        if (name.isBlank()) throw new IllegalArgumentException("Nome da categoria é obrigatório");
        String color = text(payload.get("color"));
        if (color.isBlank()) color = "#2563eb";
        jdbc.update("INSERT INTO categories(name, color, active, created_at) VALUES(?,?,?,?)", name, color, 1, now());
        return getCategory(lastInsertId());
    }

    public List<Map<String, Object>> listTransactionsByMonth(String month) {
        if (month == null || month.length() != 7) return List.of();
        String start = month + "-01";
        LocalDate endDate = LocalDate.parse(start).plusMonths(1);
        String end = endDate.format(DateTimeFormatter.ISO_DATE);

        List<Map<String, Object>> rows = jdbc.query("""
                SELECT t.id, t.tx_date, t.description, t.amount_cents, t.installment_total,
                       c.name AS card_name,
                       cat.name AS category_name,
                       CASE WHEN EXISTS (SELECT 1 FROM transaction_splits s WHERE s.transaction_id = t.id) THEN 1 ELSE 0 END AS has_splits
                FROM transactions t
                LEFT JOIN cards c ON c.id = t.card_id
                LEFT JOIN categories cat ON cat.id = t.category_id
                WHERE t.tx_date >= ? AND t.tx_date < ?
                ORDER BY t.tx_date ASC, t.id ASC
                """, (rs, i) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("tx_date", rs.getString("tx_date"));
            row.put("description", rs.getString("description"));
            row.put("amount", centsToAmount(rs.getLong("amount_cents")));
            row.put("installment_total", rs.getObject("installment_total") == null ? 1 : rs.getInt("installment_total"));
            row.put("card_name", rs.getString("card_name"));
            row.put("category_name", rs.getString("category_name"));
            row.put("has_splits", rs.getInt("has_splits") == 1);
            row.put("movement_type", "saida");
            row.put("payment_method", rs.getString("card_name") == null ? "pix" : "card");
            return row;
        }, start, end);

        rows.addAll(jdbc.query("""
                SELECT id, tx_date, movement_type, payment_method, description, amount_cents
                FROM movements
                WHERE tx_date >= ? AND tx_date < ?
                ORDER BY tx_date ASC, id ASC
                """, (rs, i) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", "m-" + rs.getLong("id"));
            row.put("tx_date", rs.getString("tx_date"));
            row.put("description", rs.getString("description"));
            row.put("amount", centsToAmount(rs.getLong("amount_cents")));
            row.put("installment_total", 1);
            row.put("card_name", null);
            row.put("category_name", rs.getString("movement_type").equals("entrada") ? "Entrada" : "Saída");
            row.put("has_splits", false);
            row.put("movement_type", rs.getString("movement_type"));
            row.put("payment_method", rs.getString("payment_method"));
            return row;
        }, start, end));

        rows.sort((a, b) -> String.valueOf(a.get("tx_date")).compareTo(String.valueOf(b.get("tx_date"))));
        return rows;
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
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", lastInsertId());
        row.put("tx_date", txDate);
        row.put("movement_type", movementType);
        row.put("payment_method", paymentMethod);
        row.put("description", description);
        row.put("amount", centsToAmount(amountCents));
        row.put("category_name", movementType.equals("entrada") ? "Entrada" : "Saída");
        return row;
    }

    public Map<String, Object> createTransaction(Map<String, Object> payload) {
        String txDate = text(payload.get("tx_date"));
        if (txDate.isBlank()) throw new IllegalArgumentException("Data é obrigatória");
        String description = text(payload.get("description"));
        if (description.isBlank()) throw new IllegalArgumentException("Descrição é obrigatória");
        long amountCents = parseMoneyToCents(payload.get("amount"));
        Long cardId = nullableLong(payload.get("card_id"));
        Long categoryId = nullableLong(payload.get("category_id"));
        Integer installmentTotal = intValue(payload.get("installment_total"), 1);

        List<Map<String, Object>> splits = readSplits(payload.get("splits"));
        if (!splits.isEmpty()) {
            long splitTotal = 0;
            for (Map<String, Object> split : splits) {
                splitTotal += parseMoneyToCents(split.get("amount"));
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

        for (Map<String, Object> split : splits) {
            Long splitCategoryId = nullableLong(split.get("category_id"));
            if (splitCategoryId == null) throw new IllegalArgumentException("Categoria do detalhamento é obrigatória");
            String splitDescription = text(split.get("description"));
            long splitAmount = parseMoneyToCents(split.get("amount"));
            jdbc.update("INSERT INTO transaction_splits(transaction_id, category_id, description, amount_cents) VALUES(?,?,?,?)",
                    id, splitCategoryId, splitDescription, splitAmount);
        }

        return getTransaction(id);
    }

    public List<Map<String, Object>> listInvoices() {
        return jdbc.query("""
                SELECT i.id, i.card_id, i.statement_month, i.filename, i.total_lines, i.matched_lines, i.status, c.name AS card_name
                FROM invoice_imports i
                LEFT JOIN cards c ON c.id = i.card_id
                ORDER BY i.id DESC
                """, (rs, i) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("card_id", rs.getLong("card_id"));
            row.put("statement_month", rs.getString("statement_month"));
            row.put("filename", rs.getString("filename"));
            row.put("total_lines", rs.getInt("total_lines"));
            row.put("matched_lines", rs.getInt("matched_lines"));
            row.put("status", rs.getString("status"));
            row.put("card_name", rs.getString("card_name"));
            return row;
        });
    }

    public Map<String, Object> uploadInvoice(Long cardId, String statementMonth, MultipartFile file) throws IOException {
        if (cardId == null) throw new IllegalArgumentException("Cartão é obrigatório");
        if (statementMonth == null || statementMonth.length() != 7) throw new IllegalArgumentException("Mês da fatura inválido");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Arquivo é obrigatório");
        Path uploads = Paths.get("data", "uploads");
        Files.createDirectories(uploads);
        String safeName = Objects.requireNonNullElse(file.getOriginalFilename(), "fatura.pdf").replaceAll("[^a-zA-Z0-9._-]", "_");
        Path target = uploads.resolve(UUID.randomUUID() + "-" + safeName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        jdbc.update("""
                INSERT INTO invoice_imports(card_id, statement_month, filename, stored_path, total_lines, matched_lines, status, created_at)
                VALUES(?,?,?,?,?,?,?,?)
                """, cardId, statementMonth, safeName, target.toString(), 0, 0, "processed", now());

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", lastInsertId());
        row.put("card_id", cardId);
        row.put("statement_month", statementMonth);
        row.put("filename", safeName);
        row.put("total_lines", 0);
        row.put("matched_lines", 0);
        return row;
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private long lastInsertId() {
        Long id = jdbc.queryForObject("SELECT last_insert_rowid()", Long.class);
        return id == null ? 0 : id;
    }

    private long parseMoneyToCents(Object value) {
        String raw = text(value);
        if (raw.isBlank()) return 0;
        String normalized;
        if (raw.contains(",")) normalized = raw.replace(".", "").replace(",", ".");
        else normalized = raw;
        BigDecimal decimal = new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);
        return decimal.movePointRight(2).longValue();
    }

    private double centsToAmount(long cents) {
        return BigDecimal.valueOf(cents).movePointLeft(2).doubleValue();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readSplits(Object value) {
        if (!(value instanceof List<?> list)) return List.of();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Map<String, Object> converted = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    converted.put(String.valueOf(entry.getKey()), entry.getValue());
                }
                out.add(converted);
            }
        }
        return out;
    }

    private Integer intValue(Object value, int fallback) {
        String raw = text(value);
        if (raw.isBlank()) return fallback;
        return Integer.parseInt(raw);
    }

    private Long nullableLong(Object value) {
        String raw = text(value);
        if (raw.isBlank()) return null;
        return Long.parseLong(raw);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private Map<String, Object> getCard(long id) {
        try {
            return jdbc.queryForObject("SELECT id, name, bank, last_digits, closing_day, due_day, active FROM cards WHERE id = ?",
                    (rs, i) -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("id", rs.getLong("id"));
                        row.put("name", rs.getString("name"));
                        row.put("bank", rs.getString("bank"));
                        row.put("last_digits", rs.getString("last_digits"));
                        row.put("closing_day", rs.getInt("closing_day"));
                        row.put("due_day", rs.getInt("due_day"));
                        row.put("active", rs.getInt("active"));
                        return row;
                    }, id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Cartão não encontrado");
        }
    }

    private Map<String, Object> getCategory(long id) {
        try {
            return jdbc.queryForObject("SELECT id, name, color, active FROM categories WHERE id = ?",
                    (rs, i) -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("id", rs.getLong("id"));
                        row.put("name", rs.getString("name"));
                        row.put("color", rs.getString("color"));
                        row.put("active", rs.getInt("active"));
                        return row;
                    }, id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Categoria não encontrada");
        }
    }

    private Map<String, Object> getTransaction(long id) {
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
                    """, (rs, i) -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getLong("id"));
                row.put("tx_date", rs.getString("tx_date"));
                row.put("description", rs.getString("description"));
                row.put("amount", centsToAmount(rs.getLong("amount_cents")));
                row.put("installment_total", rs.getObject("installment_total") == null ? 1 : rs.getInt("installment_total"));
                row.put("card_name", rs.getString("card_name"));
                row.put("category_name", rs.getString("category_name"));
                row.put("has_splits", rs.getInt("has_splits") == 1);
                row.put("movement_type", "saida");
                row.put("payment_method", rs.getString("card_name") == null ? "pix" : "card");
                return row;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Transação não encontrada");
        }
    }
}
