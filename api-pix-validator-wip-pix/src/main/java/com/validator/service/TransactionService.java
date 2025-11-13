package com.validator.service;

import com.validator.controller.requests.TransactionRequest;
import com.validator.controller.responses.TransactionResponse;
import com.validator.model.Transaction;
import com.validator.repository.TransactionRepositoryImpl;
import com.validator.service.validators.TransactionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepositoryImpl transactionRepository;
    private final TransactionValidator validator;

    public List<TransactionResponse> findAll() {
        log.info("Fetching all transactions");
        return transactionRepository.findAll().stream()
                .map(Transaction::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<TransactionResponse> findById(Long id) {
        log.info("Fetching transaction with id={}", id);
        return transactionRepository.findById(id).map(Transaction::toResponse);
    }

    public TransactionResponse save(TransactionRequest request) {
        Transaction transaction = validator.validate(request.toEntity());
        Transaction saved = transactionRepository.save(transaction);

        log.info("Transaction created successfully with id={}", saved.getId());

        return saved.toResponse();
    }

    public TransactionResponse update(Long id, TransactionRequest request) {
        log.info("Updating transaction with id={}", id);

        Transaction existing =
                transactionRepository
                        .findById(id)
                        .orElseThrow(
                                () -> {
                                    log.warn("Update failed: transaction not found for id={}", id);
                                    return new IllegalArgumentException("Transaction not found: " + id);
                                });

        Transaction updated = request.toEntity();
        updated.setId(existing.getId());

        Transaction saved = transactionRepository.update(updated);
        log.info("Transaction updated successfully with id={}", id);

        return saved.toResponse();
    }

    public void delete(Long id) {
        log.info("Deleting transaction with id={}", id);

        if (transactionRepository.findById(id).isEmpty()) {
            log.warn("Delete failed: transaction not found for id={}", id);
            throw new IllegalArgumentException("Transaction not found: " + id);
        }

        transactionRepository.delete(id);
        log.info("Transaction deleted successfully with id={}", id);
    }

    public List<TransactionResponse> findByStatus(String status) {
        log.info("Fetching transactions with status={}", status);
        return transactionRepository.findByStatus(status).stream()
                .map(Transaction::toResponse)
                .collect(Collectors.toList());
    }

}
