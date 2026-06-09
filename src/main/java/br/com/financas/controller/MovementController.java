package br.com.financas.controller;

import br.com.financas.dto.MovementRequest;
import br.com.financas.dto.MovementResponse;
import br.com.financas.service.FinanceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movements")
public class MovementController {
    private final FinanceService service;

    public MovementController(FinanceService service) {
        this.service = service;
    }

    @PostMapping
    public MovementResponse createMovement(@RequestBody MovementRequest payload) {
        return service.createMovement(payload);
    }
}
