package com.validator.repository;

import com.validator.model.Transaction;
import com.validator.model.enums.TransactionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class TransactionRepositoryImpl implements TransactionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Transaction> findAll() {
        return entityManager
                .createQuery("SELECT t FROM Transaction t ORDER BY t.id", Transaction.class)
                .getResultList();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Transaction.class, id));
    }

    @Override
    @Transactional
    public Transaction save(Transaction transaction) {
        if (Objects.isNull(transaction.getId())) {
            transaction.setCreatedDate(LocalDateTime.now());
            transaction.setLastUpdatedDate(LocalDateTime.now());
            entityManager.persist(transaction);
            return transaction;
        }

        return entityManager.merge(transaction);
    }

    @Override
    @Transactional
    public Transaction update(Transaction transaction) {
        Transaction existing = getOrThrow(transaction.getId());
        transaction.setCreatedDate(existing.getCreatedDate());
        transaction.setLastUpdatedDate(LocalDateTime.now());
        return entityManager.merge(transaction);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Transaction existing = getOrThrow(id);
        entityManager.remove(existing);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Transaction> findReceiverLast15Transactions(Long userId) {
        return entityManager.createQuery(
                        "SELECT t FROM Transaction t " +
                                "WHERE t.receiver.id = :userId " +
                                "ORDER BY t.createdDate DESC", Transaction.class)
                .setParameter("userId", userId)
                .setMaxResults(15)
                .getResultList();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Transaction> findByStatus(String status) {
        TransactionStatus enumStatus = TransactionStatus.valueOf(status.toUpperCase());

        return entityManager.createQuery(
                        "SELECT t FROM Transaction t " +
                                "WHERE t.status = :status " +
                                "ORDER BY t.createdDate DESC", Transaction.class)
                .setParameter("status", enumStatus)
                .setMaxResults(15)
                .getResultList();
    }

    private Transaction getOrThrow(Long id) {
        Transaction entity = entityManager.find(Transaction.class, id);

        if (Objects.isNull(entity)) {
            throw new IllegalArgumentException("Transaction not found with id=" + id);
        }

        return entity;
    }
}
