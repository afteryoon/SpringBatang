package com.example.springbatang.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private SecretKey key;

    private RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, accessTokenExpiration);
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, refreshTokenExpiration);
    }

    private String generateToken(Authentication authentication, long expiration) {
        long now = System.currentTimeMillis();

        // 권한 정보 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Claims 생성
        Claims claims = Jwts.claims()
                .subject(authentication.getName())
                .add("auth", authorities)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .build();

        // 토큰 생성 및 반환
        return Jwts.builder()
                .claims(claims)
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public void invalidateToken(String token) {
        // 토큰의 남은 유효시간 계산
        Claims claims = extractAllClaims(token);
        long expirationTime = claims.getExpiration().getTime();
        long now = System.currentTimeMillis();
        long ttl = expirationTime - now;

        if (ttl > 0) {
            // 토큰을 블랙리스트에 추가 (Redis 사용)
            redisTemplate.opsForValue().set(
                    "blacklist:" + token,
                    "invalidated",
                    ttl,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    // 토큰 검증 시 블랙리스트 확인 추가
    public boolean validateToken(String token) {
        try {
            // 블랙리스트 확인
            Boolean isBlacklisted = redisTemplate.hasKey("blacklist:" + token);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                return false;
            }

            // 기존 검증 로직
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: ", e);
            return false;
        }
    }
}