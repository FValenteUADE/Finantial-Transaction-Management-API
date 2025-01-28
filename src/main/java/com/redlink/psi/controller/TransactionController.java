package com.redlink.psi.controller;

import com.redlink.psi.service.TransactionService;
import com.redlink.psi.utils.CustomException;
import com.redlink.psi.utils.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@Validated
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody Map<String, Object> requestMap) {
        try {
            Object response = transactionService.createTransaction(requestMap);
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransactionStatus(@PathVariable String transactionId) {
        try {
            Object response = transactionService.getTransactionStatus(transactionId);
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @GetMapping
    public ResponseEntity<?> listUserTransactions(
            @RequestParam String userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String order,
            Pageable pageable) {
        try {
            return ResponseEntity.ok(transactionService.listUserTransactions(userId, status, sortBy, order, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Error interno del servidor"));
        }
    }
}