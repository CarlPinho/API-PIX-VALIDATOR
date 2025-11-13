package com.validator.repository;

import com.validator.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByPixKey(String key);

    User save(User user);

}
