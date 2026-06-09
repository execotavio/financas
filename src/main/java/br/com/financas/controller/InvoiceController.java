package br.com.financas.controller;

import br.com.financas.dto.InvoiceResponse;
import br.com.financas.service.FinanceService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final FinanceService service;

    public InvoiceController(FinanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<InvoiceResponse> listInvoices(
            @RequestParam(value = "card_id", required = false) Long cardId,
            @RequestParam(value = "statement_month", required = false) String statementMonth,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        return service.listInvoices(cardId, statementMonth, page, size);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public InvoiceResponse uploadInvoice(
            @RequestParam("card_id") Long cardId,
            @RequestParam("statement_month") String statementMonth,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return service.uploadInvoice(cardId, statementMonth, file);
    }
}
