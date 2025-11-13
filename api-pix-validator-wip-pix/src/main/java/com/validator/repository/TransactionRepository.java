package com.validator.repository;

import com.validator.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    List<Transaction> findAll();

    Optional<Transaction> findById(Long id);

    Transaction save(Transaction transaction);

    Transaction update(Transaction transaction);

    void delete(Long id);

    List<Transaction> findReceiverLast15Transactions(Long userId);

    List<Transaction> findByStatus(String status);
}
