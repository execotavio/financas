package br.com.financas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InvoiceResponse(
        Long id,
        @JsonProperty("card_id") Long cardId,
        @JsonProperty("statement_month") String statementMonth,
        String filename,
        @JsonProperty("total_lines") Integer totalLines,
        @JsonProperty("matched_lines") Integer matchedLines,
        String status,
        @JsonProperty("card_name") String cardName
) {
}
