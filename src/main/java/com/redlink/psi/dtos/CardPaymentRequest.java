package com.redlink.psi.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CardPaymentRequest {
    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("card_id")
    private String cardId;

    @JsonProperty("user_id")
    private String userId;

    private BigDecimal amount;
    private String currency;
    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    private Merchant merchant;

    @JsonProperty("mcc_code")
    private Integer mccCode;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public Integer getMccCode() {
        return mccCode;
    }

    public void setMccCode(Integer mccCode) {
        this.mccCode = mccCode;
    }
}