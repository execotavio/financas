package br.com.financas.service;

import br.com.financas.dto.CardRequest;
import br.com.financas.dto.CardResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardDomainService extends JdbcFinanceSupport {
    public CardDomainService(JdbcTemplate jdbc) {
        super(jdbc);
    }

    public List<CardResponse> listCards() {
        return jdbc.query("SELECT id, name, bank, last_digits, closing_day, due_day, active FROM cards ORDER BY id DESC", (rs, i) ->
                new CardResponse(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("bank"),
                        rs.getString("last_digits"),
                        rs.getInt("closing_day"),
                        rs.getInt("due_day"),
                        rs.getInt("active")
                ));
    }

    public CardResponse createCard(CardRequest request) {
        String name = text(request.getName());
        if (name.isBlank()) throw new IllegalArgumentException("Nome do cartão é obrigatório");
        String bank = text(request.getBank());
        String lastDigits = text(request.getLastDigits());
        Integer closingDayValue = request.getClosingDay();
        int closingDay = closingDayValue == null ? 28 : closingDayValue;
        Integer dueDayValue = request.getDueDay();
        int dueDay = dueDayValue == null ? 10 : dueDayValue;
        jdbc.update("INSERT INTO cards(name, bank, last_digits, closing_day, due_day, active, created_at) VALUES(?,?,?,?,?,?,?)",
                name, bank, lastDigits, closingDay, dueDay, 1, now());
        return getCard(lastInsertId());
    }

    private CardResponse getCard(long id) {
        try {
            return jdbc.queryForObject("SELECT id, name, bank, last_digits, closing_day, due_day, active FROM cards WHERE id = ?",
                    (rs, i) -> new CardResponse(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("bank"),
                            rs.getString("last_digits"),
                            rs.getInt("closing_day"),
                            rs.getInt("due_day"),
                            rs.getInt("active")
                    ), id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Cartão não encontrado");
        }
    }
}
