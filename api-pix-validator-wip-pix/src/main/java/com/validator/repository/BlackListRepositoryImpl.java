package com.validator.repository;

import com.validator.model.BlackList;
import com.validator.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BlackListRepositoryImpl implements BlackListRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<BlackList> getALL() {
        return entityManager.createQuery("SELECT b FROM BlackList b ORDER BY b.id", BlackList.class).getResultList();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<User> getUser(Long id) {
        BlackList blackList = entityManager.find(BlackList.class, id);
        return Optional.ofNullable(blackList != null ? blackList.getUser() : null);
    }

    @Override
    @Transactional
    public void save(BlackList blackList) {
        Optional.ofNullable(blackList.getId()).ifPresentOrElse(entityManager::merge, () -> entityManager.persist(blackList));
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<BlackList> findByUserId(Long userId) {
        return entityManager.createQuery(
                    // Esta query busca na entidade BlackList onde o 'user' (que é um objeto) 
                    // tenha um 'id' igual ao parâmetro
                    "SELECT b FROM BlackList b WHERE b.user.id = :userId", BlackList.class)
            .setParameter("userId", userId)
            .getResultStream()
            .findFirst();
}
}

