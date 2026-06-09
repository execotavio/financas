package br.com.financas.controller;

import br.com.financas.dto.CategoryRequest;
import br.com.financas.dto.CategoryResponse;
import br.com.financas.service.FinanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final FinanceService service;

    public CategoryController(FinanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoryResponse> listCategories() {
        return service.listCategories();
    }

    @PostMapping
    public CategoryResponse createCategory(@RequestBody CategoryRequest payload) {
        return service.createCategory(payload);
    }
}
