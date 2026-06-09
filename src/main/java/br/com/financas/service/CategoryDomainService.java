package br.com.financas.service;

import br.com.financas.dto.CategoryRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryDomainService extends JdbcFinanceSupport {
    public CategoryDomainService(JdbcTemplate jdbc) {
        super(jdbc);
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

    public Map<String, Object> createCategory(CategoryRequest request) {
        String name = text(request.getName());
        if (name.isBlank()) throw new IllegalArgumentException("Nome da categoria é obrigatório");
        String color = text(request.getColor());
        if (color.isBlank()) color = "#2563eb";
        jdbc.update("INSERT INTO categories(name, color, active, created_at) VALUES(?,?,?,?)", name, color, 1, now());
        return getCategory(lastInsertId());
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
}
