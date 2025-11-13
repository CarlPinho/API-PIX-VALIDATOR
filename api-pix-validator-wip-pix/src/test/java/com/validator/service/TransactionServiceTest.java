package com.validator.service;

import com.validator.controller.requests.TransactionRequest;
import com.validator.controller.requests.UserRequest;
import com.validator.controller.responses.TransactionResponse;
import com.validator.model.Transaction;
import com.validator.model.User;
import com.validator.model.enums.PixType;
import com.validator.model.enums.TransactionStatus;
import com.validator.repository.TransactionRepositoryImpl;
import com.validator.service.validators.TransactionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private TransactionRepositoryImpl transactionRepository;

    @Mock
    private TransactionValidator validator;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;
    private TransactionRequest transactionRequest;
    private User sender;
    private User receiver;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        sender = new User(1L, "11111111111", "Alice", PixType.CPF, "11111111111");
        receiver = new User(2L, "22222222222", "Bob", PixType.EMAIL, "bob@example.com");

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setValue(200.0);
        transaction.setDescription("Payment test");
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedDate(LocalDateTime.now());

        UserRequest senderReq = new UserRequest();
        senderReq.setPixKey("11111111111");
        senderReq.setPixKeyType(PixType.CPF);

        UserRequest receiverReq = new UserRequest();
        receiverReq.setPixKey("bob@example.com");
        receiverReq.setPixKeyType(PixType.EMAIL);

        transactionRequest = new TransactionRequest();
        transactionRequest.setId(1L);
        transactionRequest.setSender(senderReq);
        transactionRequest.setReceiver(receiverReq);
        transactionRequest.setValue(200.0);
        transactionRequest.setDescription("Payment test");
    }

    @Test
    void testFindAll() {
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));
        List<TransactionResponse> result = transactionService.findAll();

        assertEquals(1, result.size());
        assertEquals(transaction.getId(), result.get(0).getId());
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Optional<TransactionResponse> result = transactionService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(transaction.getId(), result.get().getId());
        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    void testSave() {
        when(validator.validate(any(Transaction.class))).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse result = transactionService.save(transactionRequest);

        assertNotNull(result);
        assertEquals(transaction.getId(), result.getId());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testUpdate_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.update(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse result = transactionService.update(1L, transactionRequest);

        assertNotNull(result);
        assertEquals(transaction.getId(), result.getId());
        verify(transactionRepository, times(1)).update(any(Transaction.class));
    }

    @Test
    void testUpdate_NotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.update(99L, transactionRequest));

        verify(transactionRepository, times(1)).findById(99L);
    }

    @Test
    void testDelete_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        transactionService.delete(1L);

        verify(transactionRepository, times(1)).delete(1L);
    }

    @Test
    void testDelete_NotFound() {
        when(transactionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> transactionService.delete(2L));
        verify(transactionRepository, never()).delete(anyLong());
    }

    @Test
    void testFindByStatus() {
        when(transactionRepository.findByStatus("SUCCESS")).thenReturn(List.of(transaction));

        List<TransactionResponse> result = transactionService.findByStatus("SUCCESS");

        assertEquals(1, result.size());
        assertEquals(transaction.getId(), result.get(0).getId());
        verify(transactionRepository, times(1)).findByStatus("SUCCESS");
    }
}
