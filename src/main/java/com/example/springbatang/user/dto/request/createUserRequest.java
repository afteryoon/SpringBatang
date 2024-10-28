package com.example.springbatang.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class createUserRequest {
    private String email;
    private String password;
    private String nickname;
}
