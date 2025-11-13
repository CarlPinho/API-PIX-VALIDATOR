package com.validator.repository;

import com.validator.model.Transaction;
import com.validator.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<User> findByPixKey(String key) {
        return entityManager.createQuery("SELECT u FROM User u WHERE u.pixKey = :key", User.class)
                .setParameter("key", key)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional
    public User save(User user) {
        if (Objects.isNull(user.getId())) {
            entityManager.persist(user);
            return user;
        }

        return entityManager.merge(user);
    }

}
