package br.com.financas.mapper;

import br.com.financas.dto.CardResponse;
import org.springframework.jdbc.core.RowMapper;

public class CardRowMapper {
    private CardRowMapper() {
    }

    public static RowMapper<CardResponse> rowMapper() {
        return (rs, i) -> new CardResponse(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("bank"),
                rs.getString("last_digits"),
                rs.getInt("closing_day"),
                rs.getInt("due_day"),
                rs.getInt("active")
        );
    }
}
