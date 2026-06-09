package br.com.financas.mapper;

import br.com.financas.dto.InvoiceResponse;
import org.springframework.jdbc.core.RowMapper;

public class InvoiceRowMapper {
    private InvoiceRowMapper() {
    }

    public static RowMapper<InvoiceResponse> rowMapper() {
        return (rs, i) -> new InvoiceResponse(
                rs.getLong("id"),
                rs.getLong("card_id"),
                rs.getString("statement_month"),
                rs.getString("filename"),
                rs.getInt("total_lines"),
                rs.getInt("matched_lines"),
                rs.getString("status"),
                rs.getString("card_name")
        );
    }
}
