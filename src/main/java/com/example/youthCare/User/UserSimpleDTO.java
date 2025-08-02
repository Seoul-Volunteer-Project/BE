package com.example.youthCare.User;

import lombok.Getter;

@Getter
public class UserSimpleDTO {
    private Long id;
    private String name;

    public UserSimpleDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
    }
}
