package br.com.financas.service;

import br.com.financas.dto.CategoryRequest;
import br.com.financas.dto.CategoryResponse;
import br.com.financas.mapper.CategoryRowMapper;
import br.com.financas.validation.CategoryRequestValidator;
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
        return jdbc.query("SELECT id, name, color, active FROM categories ORDER BY id DESC", CategoryRowMapper.rowMapper());
    }

    public List<CategoryResponse> listCategories(Integer active, Integer page, Integer size) {
        if (active == null && page == null && size == null) return listCategories();
        int safeSize = size == null || size < 1 ? 50 : size;
        int safePage = page == null || page < 0 ? 0 : page;
        int offset = safePage * safeSize;
        if (active == null) {
            return jdbc.query("SELECT id, name, color, active FROM categories ORDER BY id DESC LIMIT ? OFFSET ?", CategoryRowMapper.rowMapper(), safeSize, offset);
        }
        return jdbc.query("SELECT id, name, color, active FROM categories WHERE active = ? ORDER BY id DESC LIMIT ? OFFSET ?", CategoryRowMapper.rowMapper(), active, safeSize, offset);
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        CategoryRequestValidator.validate(request);
        String color = text(request.getColor());
        if (color.isBlank()) color = "#2563eb";
        jdbc.update("INSERT INTO categories(name, color, active, created_at) VALUES(?,?,?,?)", text(request.getName()), color, 1, now());
        return getCategory(lastInsertId());
    }

    private CategoryResponse getCategory(long id) {
        try {
            return jdbc.queryForObject("SELECT id, name, color, active FROM categories WHERE id = ?", CategoryRowMapper.rowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Categoria não encontrada");
        }
    }
}
