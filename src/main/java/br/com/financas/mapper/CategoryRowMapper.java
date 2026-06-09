package br.com.financas.mapper;

import br.com.financas.dto.CategoryResponse;
import org.springframework.jdbc.core.RowMapper;

public class CategoryRowMapper {
    private CategoryRowMapper() {
    }

    public static RowMapper<CategoryResponse> rowMapper() {
        return (rs, i) -> new CategoryResponse(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("color"),
                rs.getInt("active")
        );
    }
}
