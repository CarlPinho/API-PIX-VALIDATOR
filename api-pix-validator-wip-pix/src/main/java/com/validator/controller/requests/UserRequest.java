package com.validator.controller.requests;

import com.validator.model.User;
import com.validator.model.enums.PixType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String pixKey;
    private PixType pixKeyType;

    public User toEntity() {
        User user = new User();

        user.setPixKey(this.pixKey);
        user.setPixKeyType(this.pixKeyType);

        return user;
    }

}
