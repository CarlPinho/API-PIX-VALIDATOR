package com.validator.service;

import com.validator.model.User;
import com.validator.model.enums.PixType;
import com.validator.repository.UserRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepositoryImpl userRepository;


    public User getOrCreateUser(User user) {
        return userRepository.findByPixKey(user.getPixKey()).orElseGet(() -> userRepository.save(getNewUser(user)));
    }


    private User getNewUser(User user) {
        User newUser = new User();

        if (PixType.CPF.equals(user.getPixKeyType())) {
            newUser.setCpf(user.getPixKey());
        } else {
            newUser.setCpf("CPF não localizado");
        }

        newUser.setName("Usuário não localizado");
        newUser.setPixKey(user.getPixKey());
        newUser.setPixKeyType(user.getPixKeyType());

        return newUser;
    }
}
