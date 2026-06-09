package br.com.financas.controller;

import br.com.financas.dto.CardRequest;
import br.com.financas.dto.CardResponse;
import br.com.financas.service.FinanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final FinanceService service;

    public CardController(FinanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<CardResponse> listCards() {
        return service.listCards();
    }

    @PostMapping
    public CardResponse createCard(@RequestBody CardRequest payload) {
        return service.createCard(payload);
    }
}
