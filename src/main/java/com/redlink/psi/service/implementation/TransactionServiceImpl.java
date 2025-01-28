package com.redlink.psi.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.redlink.psi.dtos.BankTransferRequest;
import com.redlink.psi.dtos.CardPaymentRequest;
import com.redlink.psi.dtos.Merchant;
import com.redlink.psi.dtos.P2PTransferRequest;
import com.redlink.psi.entities.BankTransfer;
import com.redlink.psi.entities.CardPayment;
import com.redlink.psi.entities.P2PTransfer;
import com.redlink.psi.entities.Transaction;
import com.redlink.psi.repository.TransactionRepository;
import com.redlink.psi.service.TransactionService;
import com.redlink.psi.utils.CustomException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CurrencyConversionServiceImpl currencyConversionServiceImpl;

    /**
     * Creates a new transaction based on the provided request map.
     * The transaction type is determined and the appropriate handler is called.
     * The amount is converted to ARS if necessary.
     *
     * @param requestMap the request map containing transaction details
     * @return the created transaction object
     * @throws CustomException if there is an error creating the transaction
     */
    public Object createTransaction(Map<String, Object> requestMap) {
        String transactionType = determineTransactionType(requestMap);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            String requestId = null;
            if (requestMap.containsKey("payment_id")) {
                requestId = (String) requestMap.get("payment_id");
            } else if (requestMap.containsKey("transfer_id")) {
                requestId = (String) requestMap.get("transfer_id");
            } else if (requestMap.containsKey("transaction_id")) {
                requestId = (String) requestMap.get("transaction_id");
            }

            if (requestId == null) {
                throw new CustomException("No se proporcionó un identificador de transacción válido.");
            }

            Optional<Transaction> existingTransaction = transactionRepository.findByRequestId(requestId);
            if (existingTransaction.isPresent()) {
                throw new CustomException("Transacción duplicada: " + requestId);
            }

            switch (transactionType) {
                case "CARD":
                    CardPaymentRequest cardRequest = objectMapper.convertValue(requestMap, CardPaymentRequest.class);
                    cardRequest.setAmount(currencyConversionServiceImpl.convertToARS(cardRequest.getAmount(), cardRequest.getCurrency()));
                    cardRequest.setCurrency("ARS");
                    return handleCardTransaction(cardRequest);
                case "BANK_TRANSFER":
                    BankTransferRequest bankRequest = objectMapper.convertValue(requestMap, BankTransferRequest.class);
                    bankRequest.setAmount(currencyConversionServiceImpl.convertToARS(bankRequest.getAmount(), bankRequest.getCurrency()));
                    bankRequest.setCurrency("ARS");
                    return handleBankTransfer(bankRequest);
                case "P2P":
                    P2PTransferRequest p2pRequest = objectMapper.convertValue(requestMap, P2PTransferRequest.class);
                    p2pRequest.setAmount(currencyConversionServiceImpl.convertToARS(p2pRequest.getAmount(), p2pRequest.getCurrency()));
                    p2pRequest.setCurrency("ARS");
                    return handleP2PTransaction(p2pRequest);
                default:
                    throw new IllegalArgumentException("Tipo de transacción no soportado");
            }
        } catch (CustomException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            throw new CustomException("Error de integridad de datos", e);
        } catch (Exception e) {
            transactionRepository.deleteByRequestId((String) requestMap.get("request_id"));
            throw new CustomException("Error al crear la transacción", e);
        }
    }

    /**
     * Retrieves the status of a transaction based on the provided transaction ID.
     *
     * @param transactionId the ID of the transaction
     * @return the transaction status object
     * @throws CustomException if the transaction is not found
     */
    public Object getTransactionStatus(String transactionId) {
        Transaction transaction = transactionRepository.findByRequestId(transactionId)
                .orElseThrow(() -> new CustomException("Transacción no encontrada."));
        return mapToSpecificResponse(transaction);
    }

    /**
     * Lists user transactions based on the provided parameters.
     *
     * @param userId the ID of the user
     * @param status the status of the transactions to filter by (optional)
     * @param sortBy the field to sort by (optional, default is "createdAt")
     * @param order the sort order (optional, default is "ASC")
     * @param pageable the pagination information
     * @return a page of user transactions
     */
    public Page<Object> listUserTransactions(String userId, String status, String sortBy, String order, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.fromString(order == null ? "ASC" : order.toUpperCase()),
                sortBy == null ? "createdAt" : sortBy);

        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Specification<Transaction> specification = (root, query, criteriaBuilder) -> {
            Predicate userIdPredicate = criteriaBuilder.equal(root.get("userId"), userId);
            Predicate senderIdPredicate = criteriaBuilder.equal(root.get("senderId"), userId);
            Predicate statusPredicate = status != null ? criteriaBuilder.equal(root.get("status"), status) : criteriaBuilder.conjunction();
            return criteriaBuilder.and(criteriaBuilder.or(userIdPredicate, senderIdPredicate), statusPredicate);
        };

        Page<Transaction> transactions = transactionRepository.findAll(specification, pageable);

        return transactions.map(this::mapToSpecificResponse);
    }

    /**
     * Determines the transaction type based on the provided request map.
     *
     * @param requestMap the request map containing transaction details
     * @return the transaction type as a string
     * @throws IllegalArgumentException if the transaction type is not supported
     */
    private String determineTransactionType(Map<String, Object> requestMap) {
        if (requestMap.containsKey("payment_id")) {
            return "CARD";
        } else if (requestMap.containsKey("transaction_id")) {
            return "BANK_TRANSFER";
        } else if (requestMap.containsKey("transfer_id")) {
            return "P2P";
        } else {
            throw new IllegalArgumentException("Tipo de transacción no soportado");
        }
    }

    /**
     * Handles the creation of a card payment transaction.
     *
     * @param request the card payment request object
     * @return the card payment request object
     */
    private CardPaymentRequest handleCardTransaction(CardPaymentRequest request) {
        Transaction transaction = new Transaction();
        transaction.setTransactionType("CARD");
        transaction.setRequestId(request.getPaymentId());
        transaction.setUserId(request.getUserId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setStatus(request.getStatus());
        transaction.setCreatedAt(request.getCreatedAt());

        CardPayment cardPayment = new CardPayment();
        cardPayment.setTransaction(transaction);
        cardPayment.setCardId(request.getCardId());
        cardPayment.setMerchantName(request.getMerchant().getName());
        cardPayment.setMerchantId(request.getMerchant().getMerchantId());
        cardPayment.setMccCode(request.getMccCode());

        transaction.setCardPayment(cardPayment);
        transactionRepository.save(transaction);

        return request;
    }

    /**
     * Handles the creation of a bank transfer transaction.
     *
     * @param request the bank transfer request object
     * @return the bank transfer request object
     */
    private BankTransferRequest handleBankTransfer(BankTransferRequest request) {
        Transaction transaction = new Transaction();
        transaction.setTransactionType("BANK_TRANSFER");
        transaction.setRequestId(request.getTransactionId());
        transaction.setUserId(request.getUserId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setStatus(request.getStatus());
        transaction.setCreatedAt(request.getCreatedAt());

        BankTransfer bankTransfer = new BankTransfer();
        bankTransfer.setTransaction(transaction);
        bankTransfer.setBankCode(request.getBankCode());
        bankTransfer.setRecipientAccount(request.getRecipientAccount());

        transaction.setBankTransfer(bankTransfer);
        transactionRepository.save(transaction);

        return request;
    }

    /**
     * Handles the creation of a P2P transfer transaction.
     *
     * @param request the P2P transfer request object
     * @return the P2P transfer request object
     */
    private P2PTransferRequest handleP2PTransaction(P2PTransferRequest request) {
        Transaction transaction = new Transaction();
        transaction.setTransactionType("P2P");
        transaction.setRequestId(request.getTransferId());
        transaction.setSenderId(request.getSenderId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setStatus(request.getStatus());
        transaction.setCreatedAt(request.getCreatedAt());

        P2PTransfer p2pTransfer = new P2PTransfer();
        p2pTransfer.setTransaction(transaction);
        p2pTransfer.setRecipientId(request.getRecipientId());
        p2pTransfer.setNote(request.getNote());

        transaction.setP2pTransfer(p2pTransfer);
        transactionRepository.save(transaction);

        return request;
    }
    /**
     * Maps a transaction entity to a specific response object based on the transaction type.
     *
     * @param transaction the transaction entity
     * @return the specific response object
     * @throws IllegalArgumentException if the transaction type is not supported
     */
    private Object mapToSpecificResponse(Transaction transaction) {
        return switch (transaction.getTransactionType()) {
            case "CARD" -> getCardPaymentRequest(transaction);
            case "BANK_TRANSFER" -> getBankTransferRequest(transaction);
            case "P2P" -> getP2PTransferRequest(transaction);
            default -> throw new IllegalArgumentException("Tipo de transacción no soportado");
        };
    }

    /**
     * Converts a transaction entity to a P2P transfer request object.
     *
     * @param transaction the transaction entity
     * @return the P2P transfer request object
     */
    private static P2PTransferRequest getP2PTransferRequest(Transaction transaction) {
        P2PTransferRequest p2pResponse = new P2PTransferRequest();
        p2pResponse.setTransferId(transaction.getRequestId());
        p2pResponse.setSenderId(transaction.getSenderId());
        p2pResponse.setAmount(transaction.getAmount());
        p2pResponse.setCurrency(transaction.getCurrency());
        p2pResponse.setStatus(transaction.getStatus());
        p2pResponse.setCreatedAt(transaction.getCreatedAt());
        p2pResponse.setRecipientId(transaction.getP2pTransfer().getRecipientId());
        p2pResponse.setNote(transaction.getP2pTransfer().getNote());
        return p2pResponse;
    }

    /**
     * Converts a transaction entity to a bank transfer request object.
     *
     * @param transaction the transaction entity
     * @return the bank transfer request object
     */
    private static BankTransferRequest getBankTransferRequest(Transaction transaction) {
        BankTransferRequest bankResponse = new BankTransferRequest();
        bankResponse.setTransactionId(transaction.getRequestId());
        bankResponse.setUserId(transaction.getUserId());
        bankResponse.setAmount(transaction.getAmount());
        bankResponse.setCurrency(transaction.getCurrency());
        bankResponse.setStatus(transaction.getStatus());
        bankResponse.setCreatedAt(transaction.getCreatedAt());
        bankResponse.setBankCode(transaction.getBankTransfer().getBankCode());
        bankResponse.setRecipientAccount(transaction.getBankTransfer().getRecipientAccount());
        return bankResponse;
    }

    /**
     * Converts a transaction entity to a card payment request object.
     *
     * @param transaction the transaction entity
     * @return the card payment request object
     */
    private static CardPaymentRequest getCardPaymentRequest(Transaction transaction) {
        CardPaymentRequest cardResponse = new CardPaymentRequest();
        cardResponse.setPaymentId(transaction.getRequestId());
        cardResponse.setUserId(transaction.getUserId());
        cardResponse.setAmount(transaction.getAmount());
        cardResponse.setCurrency(transaction.getCurrency());
        cardResponse.setStatus(transaction.getStatus());
        cardResponse.setCreatedAt(transaction.getCreatedAt());
        cardResponse.setCardId(transaction.getCardPayment().getCardId());
        Merchant merchant = new Merchant();
        merchant.setName(transaction.getCardPayment().getMerchantName());
        merchant.setMerchantId(transaction.getCardPayment().getMerchantId());
        cardResponse.setMerchant(merchant);
        cardResponse.setMccCode(transaction.getCardPayment().getMccCode());
        return cardResponse;
    }
}