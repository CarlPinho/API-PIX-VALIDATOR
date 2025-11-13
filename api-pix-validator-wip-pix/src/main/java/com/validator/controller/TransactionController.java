package com.validator.controller;

import com.validator.controller.requests.TransactionRequest;
import com.validator.controller.responses.ApiResponse;
import com.validator.controller.responses.TransactionResponse;
import com.validator.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> listAll() {
        return ResponseEntity.ok(transactionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return transactionService
                .findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(
                        () ->
                                ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.error("Transaction not found: " + id)));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@RequestBody TransactionRequest transaction) {
        TransactionResponse created = transactionService.save(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id, @RequestBody TransactionRequest transaction) {
        return transactionService
                .findById(id)
                .<ResponseEntity<?>>map(
                        existing -> {
                            transaction.setId(id);
                            return ResponseEntity.ok(transactionService.update(id, transaction));
                        })
                .orElseGet(
                        () ->
                                ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.error("Transaction not found: " + id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        return transactionService
                .findById(id)
                .map(
                        existing -> {
                            transactionService.delete(id);
                            return ResponseEntity.status(HttpStatus.OK)
                                    .body(ApiResponse.success("Transaction deleted successfully"));
                        })
                .orElseGet(
                        () ->
                                ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.error("Transaction not found: " + id)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionResponse>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(transactionService.findByStatus(status));
    }
}
