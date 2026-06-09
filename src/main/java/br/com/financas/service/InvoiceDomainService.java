package br.com.financas.service;

import br.com.financas.dto.InvoiceResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class InvoiceDomainService extends JdbcFinanceSupport {
    public InvoiceDomainService(JdbcTemplate jdbc) {
        super(jdbc);
    }

    public List<InvoiceResponse> listInvoices() {
        return jdbc.query("""
                SELECT i.id, i.card_id, i.statement_month, i.filename, i.total_lines, i.matched_lines, i.status, c.name AS card_name
                FROM invoice_imports i
                LEFT JOIN cards c ON c.id = i.card_id
                ORDER BY i.id DESC
                """, (rs, i) -> new InvoiceResponse(
                rs.getLong("id"),
                rs.getLong("card_id"),
                rs.getString("statement_month"),
                rs.getString("filename"),
                rs.getInt("total_lines"),
                rs.getInt("matched_lines"),
                rs.getString("status"),
                rs.getString("card_name")
        ));
    }

    public InvoiceResponse uploadInvoice(Long cardId, String statementMonth, MultipartFile file) throws IOException {
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

        return new InvoiceResponse(lastInsertId(), cardId, statementMonth, safeName, 0, 0, "processed", null);
    }
}
