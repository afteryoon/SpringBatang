package com.example.springbatang.user.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}
