package com.redlink.psi.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "p2p_transfers")
public class P2PTransfer {

    @Id
    private Long transferID;

    @OneToOne
    @MapsId
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    private String recipientId;
    private String note;

    public Long gettransferID() {
        return transferID;
    }

    public void settransferID(Long id) {
        this.transferID = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}