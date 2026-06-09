package br.com.financas.service;

import br.com.financas.dto.CategoryRequest;
import br.com.financas.dto.CategoryResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryDomainService extends JdbcFinanceSupport {
    public CategoryDomainService(JdbcTemplate jdbc) {
        super(jdbc);
    }

    public List<CategoryResponse> listCategories() {
        return jdbc.query("SELECT id, name, color, active FROM categories ORDER BY id DESC", (rs, i) ->
                new CategoryResponse(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("color"),
                        rs.getInt("active")
                ));
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        String name = text(request.getName());
        if (name.isBlank()) throw new IllegalArgumentException("Nome da categoria é obrigatório");
        String color = text(request.getColor());
        if (color.isBlank()) color = "#2563eb";
        jdbc.update("INSERT INTO categories(name, color, active, created_at) VALUES(?,?,?,?)", name, color, 1, now());
        return getCategory(lastInsertId());
    }

    private CategoryResponse getCategory(long id) {
        try {
            return jdbc.queryForObject("SELECT id, name, color, active FROM categories WHERE id = ?",
                    (rs, i) -> new CategoryResponse(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("color"),
                            rs.getInt("active")
                    ), id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Categoria não encontrada");
        }
    }
}
