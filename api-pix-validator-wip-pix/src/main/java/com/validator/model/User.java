package com.validator.model;

import com.validator.controller.responses.UserResponse;
import com.validator.model.enums.PixType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cpf;
    private String name;

    @Enumerated(EnumType.STRING)
    private PixType pixKeyType;
    private String pixKey;


    public UserResponse toResponse() {
        UserResponse userResponse = new UserResponse();

        userResponse.setId(this.id);
        userResponse.setCpf(this.cpf);
        userResponse.setName(this.name);
        userResponse.setPixKey(this.pixKey);
        userResponse.setPixKeyType(this.pixKeyType.name());

        return userResponse;

    }

}
