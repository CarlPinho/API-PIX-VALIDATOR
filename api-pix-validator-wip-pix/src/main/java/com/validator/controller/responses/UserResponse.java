package com.validator.controller.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.validator.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String cpf;
    private String name;
    private String pixKey;
    private String pixKeyType;

}
