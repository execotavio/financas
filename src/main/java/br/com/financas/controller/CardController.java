package br.com.financas.controller;

import br.com.financas.dto.CardRequest;
import br.com.financas.service.FinanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final FinanceService service;

    public CardController(FinanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> listCards() {
        return service.listCards();
    }

    @PostMapping
    public Map<String, Object> createCard(@RequestBody CardRequest payload) {
        return service.createCard(payload);
    }
}
