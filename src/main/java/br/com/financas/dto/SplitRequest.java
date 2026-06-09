package br.com.financas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SplitRequest {
    @JsonProperty("category_id")
    private Long categoryId;

    private String description;
    private Object amount;

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
}
