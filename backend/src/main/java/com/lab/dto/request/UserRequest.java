package com.lab.dto.request;

import com.lab.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Username is required")
    private String username;

    private String password;

    private String realName;

    private String email;

    private User.Role role;

    private String phone;

    private Boolean isActive;
}
