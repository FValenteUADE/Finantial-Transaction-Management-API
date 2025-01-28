package com.redlink.psi.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface TransactionService {
    Object createTransaction(Map<String, Object> requestMap);

    Object getTransactionStatus(String transactionId);

    Page<Object> listUserTransactions(String userId, String status, String sortBy, String order, Pageable pageable);
}
