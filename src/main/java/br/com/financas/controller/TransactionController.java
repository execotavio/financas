package br.com.financas.controller;

import br.com.financas.dto.TransactionRequest;
import br.com.financas.dto.TransactionResponse;
import br.com.financas.service.FinanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final FinanceService service;

    public TransactionController(FinanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<TransactionResponse> listByMonth(@RequestParam("month") String month) {
        return service.listTransactionsByMonth(month);
    }

    @PostMapping
    public TransactionResponse createTransaction(@RequestBody TransactionRequest payload) {
        return service.createTransaction(payload);
    }
}
