package br.com.financas;

import br.com.financas.dto.CardRequest;
import br.com.financas.dto.CardResponse;
import br.com.financas.dto.CategoryRequest;
import br.com.financas.dto.CategoryResponse;
import br.com.financas.dto.MovementRequest;
import br.com.financas.dto.MovementResponse;
import br.com.financas.dto.TransactionRequest;
import br.com.financas.dto.TransactionResponse;
import br.com.financas.service.FinanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApiSmokeTest {
    @Autowired
    private FinanceService service;

    @Test
    void shouldCreateAndListCoreData() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        CardRequest cardRequest = new CardRequest();
        cardRequest.setName("Teste-" + suffix);
        cardRequest.setBank("Banco");
        cardRequest.setLastDigits("1234");
        cardRequest.setClosingDay(28);
        cardRequest.setDueDay(10);
        CardResponse card = service.createCard(cardRequest);

        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName("Mercado-" + suffix);
        categoryRequest.setColor("#111111");
        CategoryResponse category = service.createCategory(categoryRequest);

        MovementRequest movementRequest = new MovementRequest();
        movementRequest.setTxDate("2026-06-05");
        movementRequest.setMovementType("entrada");
        movementRequest.setPaymentMethod("pix");
        movementRequest.setDescription("Salario");
        movementRequest.setAmount("1000,00");
        MovementResponse movement = service.createMovement(movementRequest);

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setTxDate("2026-06-06");
        transactionRequest.setCardId(card.id());
        transactionRequest.setCategoryId(category.id());
        transactionRequest.setDescription("Compra teste");
        transactionRequest.setAmount("100,00");
        transactionRequest.setInstallmentTotal(1);
        TransactionResponse transaction = service.createTransaction(transactionRequest);

        assertNotNull(card.id());
        assertNotNull(category.id());
        assertNotNull(movement.id());
        assertNotNull(transaction.id());

        List<TransactionResponse> monthRows = service.listTransactionsByMonth("2026-06");
        assertFalse(monthRows.isEmpty());
        assertTrue(monthRows.stream().anyMatch(row -> "entrada".equals(row.movementType())));
        assertTrue(monthRows.stream().anyMatch(row -> "Compra teste".equals(row.description())));
    }
}
