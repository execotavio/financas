package br.com.financas.controller;

import br.com.financas.dto.TransactionRequest;
import br.com.financas.service.FinanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final FinanceService service;

    public TransactionController(FinanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> listByMonth(@RequestParam("month") String month) {
        return service.listTransactionsByMonth(month);
    }

    @PostMapping
    public Map<String, Object> createTransaction(@RequestBody TransactionRequest payload) {
        return service.createTransaction(payload);
    }
}
