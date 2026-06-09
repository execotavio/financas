package br.com.financas.service;

import br.com.financas.dto.CardRequest;
import br.com.financas.dto.CategoryRequest;
import br.com.financas.dto.MovementRequest;
import br.com.financas.dto.TransactionRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceService {
    private final CardDomainService cardDomainService;
    private final CategoryDomainService categoryDomainService;
    private final TransactionDomainService transactionDomainService;
    private final MovementDomainService movementDomainService;
    private final InvoiceDomainService invoiceDomainService;

    public FinanceService(
            CardDomainService cardDomainService,
            CategoryDomainService categoryDomainService,
            TransactionDomainService transactionDomainService,
            MovementDomainService movementDomainService,
            InvoiceDomainService invoiceDomainService
    ) {
        this.cardDomainService = cardDomainService;
        this.categoryDomainService = categoryDomainService;
        this.transactionDomainService = transactionDomainService;
        this.movementDomainService = movementDomainService;
        this.invoiceDomainService = invoiceDomainService;
    }

    public List<Map<String, Object>> listCards() {
        return cardDomainService.listCards();
    }

    public Map<String, Object> createCard(Map<String, Object> payload) {
        return cardDomainService.createCard(payload);
    }

    public Map<String, Object> createCard(CardRequest request) {
        return cardDomainService.createCard(request);
    }

    public List<Map<String, Object>> listCategories() {
        return categoryDomainService.listCategories();
    }

    public Map<String, Object> createCategory(Map<String, Object> payload) {
        return categoryDomainService.createCategory(payload);
    }

    public Map<String, Object> createCategory(CategoryRequest request) {
        return categoryDomainService.createCategory(request);
    }

    public List<Map<String, Object>> listTransactionsByMonth(String month) {
        return transactionDomainService.listTransactionsByMonth(month);
    }

    public Map<String, Object> createMovement(Map<String, Object> payload) {
        return movementDomainService.createMovement(payload);
    }

    public Map<String, Object> createMovement(MovementRequest request) {
        return movementDomainService.createMovement(request);
    }

    public Map<String, Object> createTransaction(Map<String, Object> payload) {
        return transactionDomainService.createTransaction(payload);
    }

    public Map<String, Object> createTransaction(TransactionRequest request) {
        return transactionDomainService.createTransaction(request);
    }

    public List<Map<String, Object>> listInvoices() {
        return invoiceDomainService.listInvoices();
    }

    public Map<String, Object> uploadInvoice(Long cardId, String statementMonth, MultipartFile file) throws IOException {
        return invoiceDomainService.uploadInvoice(cardId, statementMonth, file);
    }

    static List<Map<String, Object>> readSplitsFromPayload(Object value) {
        if (!(value instanceof List<?> list)) return List.of();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Map<String, Object> converted = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    converted.put(String.valueOf(entry.getKey()), entry.getValue());
                }
                out.add(converted);
            }
        }
        return out;
    }
}
