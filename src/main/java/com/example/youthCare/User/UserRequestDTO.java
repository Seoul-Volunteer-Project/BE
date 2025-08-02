package com.example.youthCare.User;

import lombok.Getter;

@Getter
public class UserRequestDTO {
    private String email;
    private String password;
    private String name;
    private String role; // "USER" 또는 "ADMIN"
}
