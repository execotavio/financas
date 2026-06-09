package br.com.financas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TransactionResponse(
        Object id,
        @JsonProperty("tx_date") String txDate,
        String description,
        Double amount,
        @JsonProperty("installment_total") Integer installmentTotal,
        @JsonProperty("card_name") String cardName,
        @JsonProperty("category_name") String categoryName,
        @JsonProperty("has_splits") Boolean hasSplits,
        @JsonProperty("movement_type") String movementType,
        @JsonProperty("payment_method") String paymentMethod
) {
}
