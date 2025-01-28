package com.redlink.psi;

import com.redlink.psi.dtos.CardPaymentRequest;
import com.redlink.psi.entities.CardPayment;
import com.redlink.psi.entities.Transaction;
import com.redlink.psi.repository.TransactionRepository;
import com.redlink.psi.service.implementation.CurrencyConversionServiceImpl;
import com.redlink.psi.service.implementation.TransactionServiceImpl;
import com.redlink.psi.utils.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PsiApplicationTests {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrencyConversionServiceImpl currencyConversionServiceImpl;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void createTransaction_CardPayment_Success() {
        Map<String, Object> requestMap = createCardRequestMap();
        BigDecimal convertedAmount = new BigDecimal("123000");

        when(currencyConversionServiceImpl.convertToARS(any(), any()))
                .thenReturn(convertedAmount);
        when(transactionRepository.findByRequestId(any()))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Object result = transactionService.createTransaction(requestMap);

        assertInstanceOf(CardPaymentRequest.class, result);
        CardPaymentRequest cardRequest = (CardPaymentRequest) result;
        assertEquals("ARS", cardRequest.getCurrency());
        assertEquals(convertedAmount, cardRequest.getAmount());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_DuplicateRequestId_ThrowsCustomException() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("payment_id", "duplicate_id");
        when(transactionRepository.findByRequestId("duplicate_id"))
                .thenReturn(Optional.of(new Transaction()));

        CustomException exception = assertThrows(CustomException.class,
                () -> transactionService.createTransaction(requestMap));
        assertEquals("Transacción duplicada: duplicate_id", exception.getMessage());
    }

    @Test
    void createTransaction_InvalidTransactionType_ThrowsException() {
        Map<String, Object> requestMap = new HashMap<>();

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(requestMap));
    }

    @Test
    void getTransactionStatus_NotFound_ThrowsCustomException() {
        when(transactionRepository.findByRequestId("unknown_id"))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> transactionService.getTransactionStatus("unknown_id"));
        assertEquals("Transacción no encontrada.", exception.getMessage());
    }

    @Test
    void getTransactionStatus_CardPayment_ReturnsResponse() {
        Transaction transaction = createCardTransaction();
        when(transactionRepository.findByRequestId("pay123"))
                .thenReturn(Optional.of(transaction));

        Object result = transactionService.getTransactionStatus("pay123");

        assertInstanceOf(CardPaymentRequest.class, result);
        CardPaymentRequest response = (CardPaymentRequest) result;
        assertEquals("pay123", response.getPaymentId());
        assertEquals("Merchant", response.getMerchant().getName());
    }

    @Test
    void listUserTransactions_FiltersAndSortsCorrectly() {
        String userId = "user1";
        Pageable pageable = PageRequest.of(0, 10);
        Transaction transaction = createCardTransaction();
        Page<Transaction> mockPage = new PageImpl<>(List.of(transaction));

        when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<Object> result = transactionService.listUserTransactions(
                userId, "SUCCESS", "createdAt", "ASC", pageable);

        assertEquals(1, result.getContent().size());
        assertInstanceOf(CardPaymentRequest.class, result.getContent().get(0));

        ArgumentCaptor<Specification<Transaction>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionRepository).findAll(specCaptor.capture(), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertTrue(capturedPageable.getSort().getOrderFor("createdAt").isAscending());
    }

    private Map<String, Object> createCardRequestMap() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("payment_id", "pay123");
        requestMap.put("user_id", "user1");
        requestMap.put("amount", new BigDecimal("100"));
        requestMap.put("currency", "USD");
        requestMap.put("status", "SUCCESS");
        requestMap.put("created_at", LocalDateTime.now());
        requestMap.put("card_id", "card123");
        requestMap.put("merchant", Map.of("name", "Merchant", "merchant_id", "m123"));
        requestMap.put("mcc_code", 1234);
        return requestMap;
    }

    private Transaction createCardTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionType("CARD");
        transaction.setRequestId("pay123");
        transaction.setUserId("user1");
        transaction.setAmount(new BigDecimal("123000"));
        transaction.setCurrency("ARS");
        transaction.setStatus("SUCCESS");
        transaction.setCreatedAt(LocalDateTime.now());

        CardPayment cardPayment = new CardPayment();
        cardPayment.setMerchantName("Merchant");
        cardPayment.setMerchantId("m123");
        cardPayment.setMccCode(1234);
        transaction.setCardPayment(cardPayment);

        return transaction;
    }
}