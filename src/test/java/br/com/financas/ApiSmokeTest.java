package br.com.financas;

import br.com.financas.service.FinanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApiSmokeTest {
    @Autowired
    private FinanceService service;

    @Test
    void shouldCreateAndListCoreData() {
        Map<String, Object> card = service.createCard(Map.of(
                "name", "Teste",
                "bank", "Banco",
                "last_digits", "1234",
                "closing_day", 28,
                "due_day", 10
        ));
        Map<String, Object> category = service.createCategory(Map.of(
                "name", "Mercado",
                "color", "#111111"
        ));
        Map<String, Object> movement = service.createMovement(Map.of(
                "tx_date", "2026-06-05",
                "movement_type", "entrada",
                "payment_method", "pix",
                "description", "Salario",
                "amount", "1000,00"
        ));
        Map<String, Object> transaction = service.createTransaction(Map.of(
                "tx_date", "2026-06-06",
                "card_id", String.valueOf(card.get("id")),
                "category_id", String.valueOf(category.get("id")),
                "description", "Compra teste",
                "amount", "100,00",
                "installment_total", 1
        ));

        assertNotNull(card.get("id"));
        assertNotNull(category.get("id"));
        assertNotNull(movement.get("id"));
        assertNotNull(transaction.get("id"));

        List<Map<String, Object>> monthRows = service.listTransactionsByMonth("2026-06");
        assertFalse(monthRows.isEmpty());
        assertTrue(monthRows.stream().anyMatch(row -> "entrada".equals(row.get("movement_type"))));
        assertTrue(monthRows.stream().anyMatch(row -> "Compra teste".equals(row.get("description"))));
    }
}
