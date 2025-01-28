package com.redlink.psi.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "bank_transfers")
public class BankTransfer {

    @Id
    private Long transactionID;

    @OneToOne
    @MapsId
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    private String bankCode;
    private String recipientAccount;

    public Long getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(Long id) {
        this.transactionID = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getRecipientAccount() {
        return recipientAccount;
    }

    public void setRecipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
    }
}
