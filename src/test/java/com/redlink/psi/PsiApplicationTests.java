package com.redlink.psi;

import com.redlink.psi.dtos.BankTransferRequest;
import com.redlink.psi.dtos.CardPaymentRequest;
import com.redlink.psi.dtos.P2PTransferRequest;
import com.redlink.psi.entities.BankTransfer;
import com.redlink.psi.entities.CardPayment;
import com.redlink.psi.entities.P2PTransfer;
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
import org.springframework.dao.DataIntegrityViolationException;
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
import static org.mockito.Mockito.*;

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
    void createTransaction_BankTransfer_Success() {
        Map<String, Object> requestMap = createBankTransferRequestMap();
        BigDecimal convertedAmount = new BigDecimal("123000");

        when(currencyConversionServiceImpl.convertToARS(any(), any()))
                .thenReturn(convertedAmount);
        when(transactionRepository.findByRequestId(any()))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Object result = transactionService.createTransaction(requestMap);

        assertInstanceOf(BankTransferRequest.class, result);
        BankTransferRequest bankRequest = (BankTransferRequest) result;
        assertEquals("ARS", bankRequest.getCurrency());
        assertEquals(convertedAmount, bankRequest.getAmount());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_P2PTransfer_Success() {
        Map<String, Object> requestMap = createP2PRequestMap();
        BigDecimal convertedAmount = new BigDecimal("123000");

        when(currencyConversionServiceImpl.convertToARS(any(), any()))
                .thenReturn(convertedAmount);
        when(transactionRepository.findByRequestId(any()))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Object result = transactionService.createTransaction(requestMap);

        assertInstanceOf(P2PTransferRequest.class, result);
        P2PTransferRequest p2pRequest = (P2PTransferRequest) result;
        assertEquals("ARS", p2pRequest.getCurrency());
        assertEquals(convertedAmount, p2pRequest.getAmount());
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
        assertEquals("Duplicated transaction: duplicate_id", exception.getMessage());
    }

    @Test
    void createTransaction_DataIntegrityViolation_ThrowsCustomException() {
        Map<String, Object> requestMap = createCardRequestMap();

        when(currencyConversionServiceImpl.convertToARS(any(), any()))
                .thenReturn(new BigDecimal("123000"));
        when(transactionRepository.findByRequestId(any()))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class)))
                .thenThrow(new DataIntegrityViolationException("Data integrity error."));

        CustomException exception = assertThrows(CustomException.class,
                () -> transactionService.createTransaction(requestMap));
        assertEquals("Data integrity error.", exception.getMessage());
    }

    @Test
    void createTransaction_GenericException_ThrowsCustomException() {
        Map<String, Object> requestMap = createCardRequestMap();

        when(currencyConversionServiceImpl.convertToARS(any(), any()))
                .thenReturn(new BigDecimal("123000"));
        when(transactionRepository.findByRequestId(any()))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class)))
                .thenThrow(new RuntimeException("Transaction processing failed."));

        CustomException exception = assertThrows(CustomException.class,
                () -> transactionService.createTransaction(requestMap));
        assertEquals("Transaction processing failed.", exception.getMessage());
    }

    @Test
    void createTransaction_InvalidTransactionType_ThrowsException() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("invalid_field", "value");

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(requestMap));
    }
    @Test
    void createTransaction_EmptyRequestMap_ThrowsCustomException() {
        Map<String, Object> requestMap = new HashMap<>();

        CustomException exception = assertThrows(CustomException.class,
                () -> transactionService.createTransaction(requestMap));
        assertEquals("Request body cannot be empty.", exception.getMessage());
    }

    @Test
    void createTransaction_MissingRequestId_ThrowsCustomException() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("user_id", "user1");
        requestMap.put("amount", new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(requestMap));
    }

    @Test
    void createTransaction_CustomException_ThrowsCustomException() {
        Map<String, Object> requestMap = createCardRequestMap();

        when(currencyConversionServiceImpl.convertToARS(any(), any()))
                .thenThrow(new CustomException("Error de conversión de moneda"));

        CustomException exception = assertThrows(CustomException.class,
                () -> transactionService.createTransaction(requestMap));
        assertEquals("Error de conversión de moneda", exception.getMessage());
    }

    @Test
    void createTransaction_GenericException_VerifiesRollback() {
        Map<String, Object> requestMap = createCardRequestMap();
        String paymentId = "pay123";

        when(currencyConversionServiceImpl.convertToARS(any(), any()))
                .thenReturn(new BigDecimal("123000"));
        when(transactionRepository.findByRequestId(paymentId))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class)))
                .thenThrow(new RuntimeException("Found an error whilst trying to create a Transaction."));

        CustomException exception = assertThrows(CustomException.class,
                () -> transactionService.createTransaction(requestMap));
        assertEquals("Transaction processing failed.", exception.getMessage());

        verify(transactionRepository).deleteByRequestId(paymentId);
    }

    @Test
    void createTransaction_RollbackFailure_LogsError() {
        Map<String, Object> requestMap = createCardRequestMap();
        String paymentId = "pay123";

        when(currencyConversionServiceImpl.convertToARS(any(), any()))
                .thenReturn(new BigDecimal("123000"));
        when(transactionRepository.findByRequestId(paymentId))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class)))
                .thenThrow(new RuntimeException("Found an error whilst trying to create a Transaction."));
        doThrow(new RuntimeException("Rolled back transaction with ID: {}.")).when(transactionRepository).deleteByRequestId(paymentId);

        CustomException exception = assertThrows(CustomException.class,
                () -> transactionService.createTransaction(requestMap));
        assertEquals("Transaction processing failed.", exception.getMessage());

        verify(transactionRepository).deleteByRequestId(paymentId);
    }

    @Test
    void getTransactionStatus_NotFound_ThrowsCustomException() {
        when(transactionRepository.findByRequestId("unknown_id"))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> transactionService.getTransactionStatus("unknown_id"));
        assertEquals("Transaction not found.", exception.getMessage());
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
    void getTransactionStatus_BankTransfer_ReturnsResponse() {
        Transaction transaction = createBankTransferTransaction();
        when(transactionRepository.findByRequestId("bank123"))
                .thenReturn(Optional.of(transaction));

        Object result = transactionService.getTransactionStatus("bank123");

        assertInstanceOf(BankTransferRequest.class, result);
        BankTransferRequest response = (BankTransferRequest) result;
        assertEquals("bank123", response.getTransactionId());
        assertEquals("BANK001", response.getBankCode());
    }

    @Test
    void getTransactionStatus_P2PTransfer_ReturnsResponse() {
        Transaction transaction = createP2PTransaction();
        when(transactionRepository.findByRequestId("p2p123"))
                .thenReturn(Optional.of(transaction));

        Object result = transactionService.getTransactionStatus("p2p123");

        assertInstanceOf(P2PTransferRequest.class, result);
        P2PTransferRequest response = (P2PTransferRequest) result;
        assertEquals("p2p123", response.getTransferId());
        assertEquals("user2", response.getRecipientId());
    }

    @Test
    void listUserTransactions_NoStatusFilter_ReturnsAllTransactions() {
        String userId = "user1";
        Pageable pageable = PageRequest.of(0, 10);
        Transaction transaction = createCardTransaction();
        Page<Transaction> mockPage = new PageImpl<>(List.of(transaction));

        when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<Object> result = transactionService.listUserTransactions(
                userId, null, "createdAt", "ASC", pageable);

        assertEquals(1, result.getContent().size());
        assertInstanceOf(CardPaymentRequest.class, result.getContent().get(0));

        ArgumentCaptor<Specification<Transaction>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionRepository).findAll(specCaptor.capture(), pageableCaptor.capture());

        Specification<Transaction> capturedSpec = specCaptor.getValue();
        assertNotNull(capturedSpec);
    }

    @Test
    void listUserTransactions_InvalidStatus_ThrowsCustomException() {
        String userId = "user1";
        Pageable pageable = PageRequest.of(0, 10);

        CustomException exception = assertThrows(CustomException.class,
                () -> transactionService.listUserTransactions(userId, "INVALID_STATUS", "createdAt", "ASC", pageable));
        assertEquals("Invalid transaction status.", exception.getMessage());
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

    private Map<String, Object> createBankTransferRequestMap() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("transaction_id", "bank123");
        requestMap.put("user_id", "user1");
        requestMap.put("amount", new BigDecimal("100"));
        requestMap.put("currency", "USD");
        requestMap.put("status", "SUCCESS");
        requestMap.put("created_at", LocalDateTime.now());
        requestMap.put("bank_code", "BANK001");
        requestMap.put("recipient_account", "ACC123");
        return requestMap;
    }

    private Map<String, Object> createP2PRequestMap() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("transfer_id", "p2p123");
        requestMap.put("sender_id", "user1");
        requestMap.put("amount", new BigDecimal("100"));
        requestMap.put("currency", "USD");
        requestMap.put("status", "SUCCESS");
        requestMap.put("created_at", LocalDateTime.now());
        requestMap.put("recipient_id", "user2");
        requestMap.put("note", "P2P Transfer");
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

    private Transaction createBankTransferTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionType("BANK_TRANSFER");
        transaction.setRequestId("bank123");
        transaction.setUserId("user1");
        transaction.setAmount(new BigDecimal("123000"));
        transaction.setCurrency("ARS");
        transaction.setStatus("SUCCESS");
        transaction.setCreatedAt(LocalDateTime.now());

        BankTransfer bankTransfer = new BankTransfer();
        bankTransfer.setBankCode("BANK001");
        bankTransfer.setRecipientAccount("ACC123");
        transaction.setBankTransfer(bankTransfer);

        return transaction;
    }

    private Transaction createP2PTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionType("P2P");
        transaction.setRequestId("p2p123");
        transaction.setSenderId("user1");
        transaction.setAmount(new BigDecimal("123000"));
        transaction.setCurrency("ARS");
        transaction.setStatus("SUCCESS");
        transaction.setCreatedAt(LocalDateTime.now());

        P2PTransfer p2pTransfer = new P2PTransfer();
        p2pTransfer.setRecipientId("user2");
        p2pTransfer.setNote("P2P Transfer");
        transaction.setP2pTransfer(p2pTransfer);

        return transaction;
    }
}