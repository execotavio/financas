package br.com.financas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MovementRequest {
    @JsonProperty("tx_date")
    private String txDate;

    @JsonProperty("movement_type")
    private String movementType;

    @JsonProperty("payment_method")
    private String paymentMethod;

    private String description;
    private Object amount;

    public String getTxDate() {
        return txDate;
    }

    public void setTxDate(String txDate) {
        this.txDate = txDate;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getAmount() {
        return amount;
    }

    public void setAmount(Object amount) {
        this.amount = amount;
    }
}
