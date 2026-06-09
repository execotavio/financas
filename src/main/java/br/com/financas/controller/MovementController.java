package br.com.financas.controller;

import br.com.financas.dto.MovementRequest;
import br.com.financas.dto.MovementResponse;
import br.com.financas.service.FinanceService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;

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

    @PutMapping("/{movementId}")
    public MovementResponse updateMovement(
            @PathVariable("movementId") Long movementId,
            @RequestBody MovementRequest payload
    ) {
        return service.updateMovement(movementId, payload);
    }

    @DeleteMapping("/{movementId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMovement(@PathVariable("movementId") Long movementId) {
        service.deleteMovement(movementId);
    }
}
