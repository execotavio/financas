package br.com.financas.service;

import br.com.financas.dto.CardRequest;
import br.com.financas.dto.CardResponse;
import br.com.financas.mapper.CardRowMapper;
import br.com.financas.validation.CardRequestValidator;
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
        return jdbc.query("SELECT id, name, bank, last_digits, closing_day, due_day, active FROM cards ORDER BY id DESC", CardRowMapper.rowMapper());
    }

    public List<CardResponse> listCards(Integer active, Integer page, Integer size) {
        if (active == null && page == null && size == null) return listCards();
        int safeSize = size == null || size < 1 ? 50 : size;
        int safePage = page == null || page < 0 ? 0 : page;
        int offset = safePage * safeSize;
        if (active == null) {
            return jdbc.query("SELECT id, name, bank, last_digits, closing_day, due_day, active FROM cards ORDER BY id DESC LIMIT ? OFFSET ?", CardRowMapper.rowMapper(), safeSize, offset);
        }
        return jdbc.query("SELECT id, name, bank, last_digits, closing_day, due_day, active FROM cards WHERE active = ? ORDER BY id DESC LIMIT ? OFFSET ?", CardRowMapper.rowMapper(), active, safeSize, offset);
    }

    public CardResponse createCard(CardRequest request) {
        CardRequestValidator.validate(request);
        String bank = text(request.getBank());
        String lastDigits = text(request.getLastDigits());
        Integer closingDayValue = request.getClosingDay();
        int closingDay = closingDayValue == null ? 28 : closingDayValue;
        Integer dueDayValue = request.getDueDay();
        int dueDay = dueDayValue == null ? 10 : dueDayValue;
        jdbc.update("INSERT INTO cards(name, bank, last_digits, closing_day, due_day, active, created_at) VALUES(?,?,?,?,?,?,?)",
                text(request.getName()), bank, lastDigits, closingDay, dueDay, 1, now());
        return getCard(lastInsertId());
    }

    private CardResponse getCard(long id) {
        try {
            return jdbc.queryForObject("SELECT id, name, bank, last_digits, closing_day, due_day, active FROM cards WHERE id = ?", CardRowMapper.rowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Cartão não encontrado");
        }
    }
}
