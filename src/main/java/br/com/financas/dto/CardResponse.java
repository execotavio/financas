package br.com.financas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CardResponse(
        Long id,
        String name,
        String bank,
        @JsonProperty("last_digits") String lastDigits,
        @JsonProperty("closing_day") Integer closingDay,
        @JsonProperty("due_day") Integer dueDay,
        Integer active
) {
}
