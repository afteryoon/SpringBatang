package com.example.springbatang.user.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    //이메일 인증 코드 저장
    public void saveVerificationCode(String email, String verificationCode) {
        String key = email;
        redisTemplate.opsForValue().set(key, verificationCode);
        redisTemplate.opsForValue().set(key, verificationCode);
    }

    //이메일 인증 코드 확인
    public boolean verifyVerificationCode(String email, String verificationCode) {
        String key = email;
        String storedVerificationCode = redisTemplate.opsForValue().get(key);
        return verificationCode.equals(storedVerificationCode);
    }

}
