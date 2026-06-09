package br.com.financas.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class InvoiceDomainService extends JdbcFinanceSupport {
    public InvoiceDomainService(JdbcTemplate jdbc) {
        super(jdbc);
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
}
