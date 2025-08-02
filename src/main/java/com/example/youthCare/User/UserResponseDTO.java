package com.example.youthCare.User;

import lombok.Getter;

@Getter
public class UserResponseDTO {
    private Long id;
    private String email;
    private String name;
    private String role;

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole().name();
    }
}
