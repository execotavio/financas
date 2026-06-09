package br.com.financas.controller;

import br.com.financas.service.FinanceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/movements")
public class MovementController {
    private final FinanceService service;

    public MovementController(FinanceService service) {
        this.service = service;
    }

    @PostMapping
    public Map<String, Object> createMovement(@RequestBody Map<String, Object> payload) {
        return service.createMovement(payload);
    }
}
