package com.validator.service;

import com.validator.model.User;
import com.validator.model.enums.PixType;
import com.validator.repository.UserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepositoryImpl userRepository;

    @InjectMocks
    private UserService userService;

    private User existingUser;
    private User newUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Alice");
        existingUser.setPixKey("11111111111");
        existingUser.setPixKeyType(PixType.CPF);
        existingUser.setCpf("11111111111");

        newUser = new User();
        newUser.setPixKey("22222222222");
        newUser.setPixKeyType(PixType.CPF);
    }

    @Test
    void testGetOrCreateUser_UserExists() {
        when(userRepository.findByPixKey("11111111111")).thenReturn(Optional.of(existingUser));

        User result = userService.getOrCreateUser(existingUser);

        assertNotNull(result);
        assertEquals(existingUser.getPixKey(), result.getPixKey());
        assertEquals("Alice", result.getName());
        verify(userRepository, times(1)).findByPixKey("11111111111");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetOrCreateUser_UserDoesNotExist_CPFType() {
        when(userRepository.findByPixKey("22222222222")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.getOrCreateUser(newUser);

        assertNotNull(result);
        assertEquals("22222222222", result.getPixKey());
        assertEquals(PixType.CPF, result.getPixKeyType());
        assertEquals("22222222222", result.getCpf());
        assertEquals("Usuário não localizado", result.getName());

        verify(userRepository, times(1)).findByPixKey("22222222222");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetOrCreateUser_UserDoesNotExist_NonCPFType() {
        User emailUser = new User();
        emailUser.setPixKey("bob@example.com");
        emailUser.setPixKeyType(PixType.EMAIL);

        when(userRepository.findByPixKey("bob@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.getOrCreateUser(emailUser);

        assertNotNull(result);
        assertEquals("bob@example.com", result.getPixKey());
        assertEquals(PixType.EMAIL, result.getPixKeyType());
        assertEquals("CPF não localizado", result.getCpf());
        assertEquals("Usuário não localizado", result.getName());

        verify(userRepository, times(1)).findByPixKey("bob@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }
}
