package com.validator.service.validators;

import com.validator.model.BlackList;
import com.validator.model.Transaction;
import com.validator.model.User;
import com.validator.model.enums.FraudReason;
import com.validator.model.enums.TransactionStatus;
import com.validator.repository.BlackListRepositoryImpl;
import com.validator.repository.TransactionRepositoryImpl;
import com.validator.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionValidatorTest {

    private TransactionRepositoryImpl transactionRepository;
    private BlackListRepositoryImpl blackListRepository;
    private UserService userService;
    private TransactionValidator validator;

    private User sender;
    private User receiver;
    private Transaction transaction;

    @BeforeEach
    void setup() {
        transactionRepository = mock(TransactionRepositoryImpl.class);
        blackListRepository = mock(BlackListRepositoryImpl.class);
        userService = mock(UserService.class);

        validator = new TransactionValidator(transactionRepository, blackListRepository, userService);

        sender = new User();
        sender.setId(1L);
        sender.setPixKey("sender_key");

        receiver = new User();
        receiver.setId(2L);
        receiver.setPixKey("receiver_key");

        transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setValue(100.0);
        transaction.setDescription("pagamento normal");
        transaction.setCreatedDate(LocalDateTime.now());

        when(userService.getOrCreateUser(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testValidate_UserInBlacklist() {
        when(blackListRepository.getUser(2L)).thenReturn(Optional.of(new User()));

        Transaction result = validator.validate(transaction);

        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertEquals(FraudReason.USER_IN_BLACKLIST, result.getFraudReason());
    }

    @Test
    void testValidate_StrangeValue_High() {
        transaction.setValue(20000.0);

        Transaction result = validator.validate(transaction);

        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertEquals(FraudReason.STRANGE_VALUE, result.getFraudReason());
        verify(blackListRepository).save(any(BlackList.class));
    }

    @Test
    void testValidate_StrangeValue_Low() {
        transaction.setValue(0.2);

        Transaction result = validator.validate(transaction);

        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertEquals(FraudReason.STRANGE_VALUE, result.getFraudReason());
    }

    @Test
    void testValidate_HighFrequency() {
        List<Transaction> recent = List.of(
                createRecentTx(1),
                createRecentTx(2),
                createRecentTx(3),
                createRecentTx(4),
                createRecentTx(5)
        );
        when(transactionRepository.findReceiverLast15Transactions(any())).thenReturn(recent);

        Transaction result = validator.validate(transaction);

        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertEquals(FraudReason.HIGH_FREQUENCY, result.getFraudReason());
        verify(blackListRepository).save(any(BlackList.class));
    }

    @Test
    void testValidate_OutOfAverageValue() {
        List<Transaction> lastTransactions = List.of(
                createTxWithValueAndDate(100, 1),
                createTxWithValueAndDate(120, 2),
                createTxWithValueAndDate(80, 3),
                createTxWithValueAndDate(90, 4),
                createTxWithValueAndDate(110, 5)
        );

        when(transactionRepository.findReceiverLast15Transactions(any())).thenReturn(lastTransactions);

        transaction.setValue(1000.0);

        Transaction result = validator.validate(transaction);

        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertEquals(FraudReason.OUT_OF_AVERAGE_VALUE, result.getFraudReason());
    }

    @Test
    void testValidate_DangerousDescription() {
        transaction.setDescription("URGENTE pagamento golpe");

        when(transactionRepository.findReceiverLast15Transactions(any())).thenReturn(List.of());

        Transaction result = validator.validate(transaction);

        assertEquals(TransactionStatus.PENDING_REVIEW, result.getStatus());
        assertEquals(FraudReason.SUSPICIOUS_DESCRIPTION, result.getFraudReason());
    }

    @Test
    void testValidate_DangerousKeys() {
        transaction.getSender().setPixKey("golpe123");

        when(transactionRepository.findReceiverLast15Transactions(any())).thenReturn(List.of());

        Transaction result = validator.validate(transaction);

        assertEquals(TransactionStatus.PENDING_REVIEW, result.getStatus());
        assertEquals(FraudReason.SUSPICIOUS_PIX_KEY, result.getFraudReason());
    }

    @Test
    void testValidate_Success() {
        when(transactionRepository.findReceiverLast15Transactions(any())).thenReturn(List.of());

        Transaction result = validator.validate(transaction);

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertNull(result.getFraudReason());
    }

    @Test
    void testValidateDangerousKeys_NullSafety() {
        validator.validateDangerousKeys(null);
        Transaction t = new Transaction();
        validator.validateDangerousKeys(t);
    }

    @Test
    void testIsBlacklisted_TrueWhenFound() {
        when(blackListRepository.getUser(1L)).thenReturn(Optional.of(new User()));
        assertTrue(validator.isBlacklisted(sender));
    }

    @Test
    void testIsBlacklisted_FalseWhenNotFound() {
        when(blackListRepository.getUser(1L)).thenReturn(Optional.empty());
        assertFalse(validator.isBlacklisted(sender));
    }

    @Test
    void testIsBlacklisted_NullId() {
        sender.setId(null);
        assertTrue(validator.isBlacklisted(sender));
    }

    @Test
    void testAddUserToBlackList_SavesCorrectly() {
        ArgumentCaptor<BlackList> captor = ArgumentCaptor.forClass(BlackList.class);

        validator.addUserToBlackList(receiver);
        verify(blackListRepository).save(captor.capture());

        assertEquals(receiver, captor.getValue().getUser());
        assertNotNull(captor.getValue().getCreateTime());
    }

    private Transaction createRecentTx(int minutesAgo) {
        Transaction t = new Transaction();
        t.setCreatedDate(LocalDateTime.now().minusMinutes(minutesAgo));
        t.setValue(50.00);
        return t;
    }

    private Transaction createTxWithValue(double value) {
        Transaction t = new Transaction();
        t.setCreatedDate(LocalDateTime.now());
        t.setValue(value);
        return t;
    }

    private Transaction createTxWithValueAndDate(double value, int daysAgo) {
        Transaction t = new Transaction();
        t.setCreatedDate(LocalDateTime.now().minusDays(daysAgo));
        t.setValue(value);
        return t;
    }
}
