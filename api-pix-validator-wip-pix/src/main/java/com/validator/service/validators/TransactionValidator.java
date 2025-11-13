package com.validator.service.validators;

import com.validator.model.BlackList;
import com.validator.model.Transaction;
import com.validator.model.User;
import com.validator.model.enums.FraudReason;
import com.validator.model.enums.TransactionStatus;
import com.validator.repository.BlackListRepositoryImpl;
import com.validator.repository.TransactionRepositoryImpl;
import com.validator.repository.UserRepositoryImpl;
import com.validator.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionValidator {

    private static final List<String> DANGEROUS_TERMS = List.of("golpe", "fraude", "fake", "urgente");

    private final TransactionRepositoryImpl transactionRepository;
    private final BlackListRepositoryImpl blackListRepository;
    private final UserService userService;


    public Transaction validate(Transaction transaction) {
        transaction.setSender(userService.getOrCreateUser(transaction.getSender()));
        transaction.setReceiver(userService.getOrCreateUser(transaction.getReceiver()));

        if (runAndCheck(() -> validateUserBlacklist(transaction), transaction)) return transaction;
        if (runAndCheck(() -> validateTransactionValue(transaction), transaction)) return transaction;

        List<Transaction> lastTransactions = transactionRepository.findReceiverLast15Transactions(transaction.getReceiver().getId());

        if (runAndCheck(() -> validateHighFrequency(transaction, lastTransactions), transaction)) return transaction;
        if (runAndCheck(() -> validateOutOfAverageValue(transaction, lastTransactions), transaction)) return transaction;
        if (runAndCheck(() -> validateDangerousDescription(transaction), transaction)) return transaction;
        if (runAndCheck(() -> validateDangerousKeys(transaction), transaction)) return transaction;

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setFraudReason(null);

        return transaction;
    }


    private void validateUserBlacklist(Transaction transaction) {
        List<User> users = List.of(transaction.getSender(), transaction.getReceiver());

        if (users.stream().anyMatch(this::isBlacklisted)) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFraudReason(FraudReason.USER_IN_BLACKLIST);
        }
    }

    public void validateTransactionValue(Transaction transaction) {
        double TRANSACTION_MAX_VALUE = 10000.0;
        double TRANSACTION_MIN_VALUE = 0.5;

        if (transaction.getValue() > TRANSACTION_MAX_VALUE || transaction.getValue() < TRANSACTION_MIN_VALUE) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFraudReason(FraudReason.STRANGE_VALUE);
            addUserToBlackList(transaction.getReceiver());
        }
    }

    public void validateDangerousDescription(Transaction transaction) {
        String normalizedDescription = transaction.getDescription().toLowerCase(Locale.ROOT);
        List<String> foundTerms = DANGEROUS_TERMS.stream().filter(normalizedDescription::contains).toList();

        if (BooleanUtils.isFalse(foundTerms.isEmpty())) {
            transaction.setStatus(TransactionStatus.PENDING_REVIEW);
            transaction.setFraudReason(FraudReason.SUSPICIOUS_DESCRIPTION);
        }
    }

    public void validateDangerousKeys(Transaction transaction) {
        if (transaction == null || transaction.getSender() == null || transaction.getReceiver() == null) {
            return;
        }

        List<String> dangerousTerms = List.of("golpe", "fraude", "fake", "urgente");

        String senderKey = Optional.ofNullable(transaction.getSender().getPixKey()).orElse("").toLowerCase(Locale.ROOT);
        String receiverKey = Optional.ofNullable(transaction.getReceiver().getPixKey()).orElse("").toLowerCase(Locale.ROOT);

        boolean foundDangerous =
                dangerousTerms.stream().anyMatch(senderKey::contains) ||
                        dangerousTerms.stream().anyMatch(receiverKey::contains);

        if (foundDangerous) {
            transaction.setStatus(TransactionStatus.PENDING_REVIEW);
            transaction.setFraudReason(FraudReason.SUSPICIOUS_PIX_KEY);
        }
    }

    public void validateHighFrequency(Transaction transaction, List<Transaction> recentTransactions) {
        long frequencyInMinutes = 5;

        long foundTransactions = recentTransactions.stream()
                .filter(t -> t.getCreatedDate() != null && Duration.between(t.getCreatedDate(), LocalDateTime.now()).toMinutes() <= frequencyInMinutes)
                .count();

        if (foundTransactions >= 5) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFraudReason(FraudReason.HIGH_FREQUENCY);
            addUserToBlackList(transaction.getReceiver());
        }
    }

    public void validateOutOfAverageValue(Transaction transaction, List<Transaction> lastTransactions) {
        if (lastTransactions == null || lastTransactions.isEmpty() || lastTransactions.size() < 5) {
            return;
        }

        double average = lastTransactions.stream()
                .mapToDouble(Transaction::getValue)
                .average()
                .orElse(0.0);

        double toleranceFactor = 4.0;
        double upperLimit = average * toleranceFactor;
        double lowerLimit = average / toleranceFactor;

        if (transaction.getValue() > upperLimit || transaction.getValue() < lowerLimit) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFraudReason(FraudReason.OUT_OF_AVERAGE_VALUE);
        }
    }

    public boolean isBlacklisted(User user) {
        return Objects.isNull(user.getId()) || blackListRepository.getUser(user.getId()).isPresent();
    }

    public void addUserToBlackList(User user) {
        blackListRepository.save(new BlackList(null, user, LocalDateTime.now()));
    }

    private boolean runAndCheck(Runnable validator, Transaction transaction) {
        validator.run();
        return transaction.getStatus() != null;
    }

}
