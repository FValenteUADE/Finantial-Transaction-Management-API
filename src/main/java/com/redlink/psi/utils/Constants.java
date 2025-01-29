package com.redlink.psi.utils;

public class Constants {
    public static final String TRANSACTION_CREATION_SUCCESS = "Successfully created transaction with ID: {}.";
    public static final String TRANSACTION_STARTING_CREATION = "Starting transaction creation for request map: {}.";
    public static final String TRANSACTION_EMPTY_CREATION_PARAMETERS = "Empty request map received.";
    public static final String TRANSACTION_EMPTY_ID_PARAMETER = "Required parameter ID not found";
    public static final String TRANSACTION_PREEXISTING_ID = "Found pre-existing transaction with ID {}.";
    public static final String TRANSACTION_DETERMINED_TYPE = "Transaction type determined as: {}.";
    public static final String TRANSACTION_INVALID_TYPE = "Couldn't create transaction because of invalid transaction type {}.";
    public static final String TRANSACTION_DATA_INTEGRITY_EXCEPTION = "Data Integrity Violation detected causing an error.";
    public static final String TRANSACTION_GENERIC_EXCEPTION = "Found an error whilst trying to create a Transaction.";
    public static final String TRANSACTION_STATUS_INIT = "Init getTransactionStatus method using parameter {}.";
    public static final String TRANSACTION_LIST_INIT = "Init listUserTransactions method using parameters: {}, {}, {}, {}, {}.";
    public static final String TRANSACTION_REPOSITORY_WITH_PARAMETERS = "Calling Transaction Repository with parameters: {}, {}.";
    public static final String TRANSACTION_REPOSITORY_FOUND = "Call to Transaction Repository found: {}.";
    public static final String ROLLED_BACK_TRANSACTION_WITH_ID = "Rolled back transaction with ID: {}.";
    public static final String FAILED_TO_ROLLBACK_TRANSACTION = "Failed to rollback transaction: {}.";
}
