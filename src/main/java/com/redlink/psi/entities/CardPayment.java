package com.redlink.psi.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "card_payments")
public class CardPayment {

    @Id
    private Long paymentID;

    @OneToOne
    @MapsId
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    private String cardId;
    private String merchantName;
    private String merchantId;
    private Integer mccCode;

    public Long getpaymentID() {
        return paymentID;
    }

    public void setpaymentID(Long id) {
        this.paymentID = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public Integer getMccCode() {
        return mccCode;
    }

    public void setMccCode(Integer mccCode) {
        this.mccCode = mccCode;
    }
}