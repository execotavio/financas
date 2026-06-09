package br.com.financas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CardRequest {
    private String name;
    private String bank;

    @JsonProperty("last_digits")
    private String lastDigits;

    @JsonProperty("closing_day")
    private Integer closingDay;

    @JsonProperty("due_day")
    private Integer dueDay;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getLastDigits() {
        return lastDigits;
    }

    public void setLastDigits(String lastDigits) {
        this.lastDigits = lastDigits;
    }

    public Integer getClosingDay() {
        return closingDay;
    }

    public void setClosingDay(Integer closingDay) {
        this.closingDay = closingDay;
    }

    public Integer getDueDay() {
        return dueDay;
    }

    public void setDueDay(Integer dueDay) {
        this.dueDay = dueDay;
    }
}
