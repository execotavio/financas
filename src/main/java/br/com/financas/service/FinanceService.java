package br.com.financas.service;

import br.com.financas.dto.CardRequest;
import br.com.financas.dto.CardResponse;
import br.com.financas.dto.CategoryRequest;
import br.com.financas.dto.CategoryResponse;
import br.com.financas.dto.InvoiceResponse;
import br.com.financas.dto.MovementRequest;
import br.com.financas.dto.MovementResponse;
import br.com.financas.dto.TransactionRequest;
import br.com.financas.dto.TransactionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    public List<CardResponse> listCards() {
        return cardDomainService.listCards();
    }

    public CardResponse createCard(CardRequest request) {
        return cardDomainService.createCard(request);
    }

    public List<CategoryResponse> listCategories() {
        return categoryDomainService.listCategories();
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        return categoryDomainService.createCategory(request);
    }

    public List<TransactionResponse> listTransactionsByMonth(String month) {
        return transactionDomainService.listTransactionsByMonth(month);
    }

    public MovementResponse createMovement(MovementRequest request) {
        return movementDomainService.createMovement(request);
    }

    public TransactionResponse createTransaction(TransactionRequest request) {
        return transactionDomainService.createTransaction(request);
    }

    public List<InvoiceResponse> listInvoices() {
        return invoiceDomainService.listInvoices();
    }

    public InvoiceResponse uploadInvoice(Long cardId, String statementMonth, MultipartFile file) throws IOException {
        return invoiceDomainService.uploadInvoice(cardId, statementMonth, file);
    }
}
