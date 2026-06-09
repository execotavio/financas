package br.com.financas.service;

import br.com.financas.dto.CardRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CardDomainService extends JdbcFinanceSupport {
    public CardDomainService(JdbcTemplate jdbc) {
        super(jdbc);
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

    public Map<String, Object> createCard(CardRequest request) {
        String name = text(request.getName());
        if (name.isBlank()) throw new IllegalArgumentException("Nome do cartão é obrigatório");
        String bank = text(request.getBank());
        String lastDigits = text(request.getLastDigits());
        int closingDay = request.getClosingDay() == null ? 28 : request.getClosingDay();
        int dueDay = request.getDueDay() == null ? 10 : request.getDueDay();
        jdbc.update("INSERT INTO cards(name, bank, last_digits, closing_day, due_day, active, created_at) VALUES(?,?,?,?,?,?,?)",
                name, bank, lastDigits, closingDay, dueDay, 1, now());
        return getCard(lastInsertId());
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
}
