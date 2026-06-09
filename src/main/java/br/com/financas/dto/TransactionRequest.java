package br.com.financas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TransactionRequest {
    @JsonProperty("tx_date")
    private String txDate;

    @JsonProperty("card_id")
    private Long cardId;

    @JsonProperty("category_id")
    private Long categoryId;

    private String description;
    private Object amount;

    @JsonProperty("installment_total")
    private Integer installmentTotal;

    private List<SplitRequest> splits;

    public String getTxDate() {
        return txDate;
    }

    public void setTxDate(String txDate) {
        this.txDate = txDate;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public Integer getInstallmentTotal() {
        return installmentTotal;
    }

    public void setInstallmentTotal(Integer installmentTotal) {
        this.installmentTotal = installmentTotal;
    }

    public List<SplitRequest> getSplits() {
        return splits;
    }

    public void setSplits(List<SplitRequest> splits) {
        this.splits = splits;
    }
}
