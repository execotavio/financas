package br.com.financas.validation;

import br.com.financas.dto.SplitRequest;
import br.com.financas.dto.TransactionRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class TransactionRequestValidator {
    private TransactionRequestValidator() {
    }

    public static void validate(TransactionRequest request) {
        if (request == null) throw new IllegalArgumentException("Payload inválido");
        String txDate = request.getTxDate() == null ? "" : request.getTxDate().trim();
        if (txDate.isBlank()) throw new IllegalArgumentException("Data é obrigatória");
        String description = request.getDescription() == null ? "" : request.getDescription().trim();
        if (description.isBlank()) throw new IllegalArgumentException("Descrição é obrigatória");
        long amountCents = parseMoneyToCents(request.getAmount());

        List<SplitRequest> splits = request.getSplits() == null ? List.of() : request.getSplits();
        if (!splits.isEmpty()) {
            long splitTotal = 0;
            for (SplitRequest split : splits) {
                if (split.getCategoryId() == null) throw new IllegalArgumentException("Categoria do detalhamento é obrigatória");
                splitTotal += parseMoneyToCents(split.getAmount());
            }
            if (splitTotal != amountCents) throw new IllegalArgumentException("A soma dos detalhamentos deve bater com o valor total");
        }
    }

    public static long parseMoneyToCents(Object value) {
        String raw = value == null ? "" : String.valueOf(value).trim();
        if (raw.isBlank()) return 0;
        String normalized;
        if (raw.contains(",")) normalized = raw.replace(".", "").replace(",", ".");
        else normalized = raw;
        BigDecimal decimal = new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);
        return decimal.movePointRight(2).longValue();
    }
}
