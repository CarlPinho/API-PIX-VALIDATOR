package com.validator.repository;

import com.validator.model.BlackList;
import com.validator.model.User;

import java.util.List;
import java.util.Optional;

public interface BlackListRepository {

    List<BlackList> getALL();

    Optional<User> getUser(Long id);

    void save(BlackList blackList);
}
