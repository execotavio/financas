package br.com.financas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MovementResponse(
        Long id,
        @JsonProperty("tx_date") String txDate,
        @JsonProperty("movement_type") String movementType,
        @JsonProperty("payment_method") String paymentMethod,
        String description,
        Double amount,
        @JsonProperty("category_name") String categoryName
) {
}
