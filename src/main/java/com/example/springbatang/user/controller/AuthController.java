package com.example.springbatang.user.controller;

import com.example.springbatang.user.dto.request.EmailVerificationRequest;
import com.example.springbatang.user.dto.request.LoginRequest;
import com.example.springbatang.user.dto.response.TokenResponse;
import com.example.springbatang.user.service.EmailService;
import com.example.springbatang.user.service.UserService;
import com.example.springbatang.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtUtil.generateAccessToken(authentication);
            String refreshToken = jwtUtil.generateRefreshToken(authentication);

            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setStatus("success");
            tokenResponse.setAccessToken(accessToken);
            tokenResponse.setRefreshToken(refreshToken);
            tokenResponse.setUsername(authentication.getName());
            tokenResponse.setRememberMe(loginRequest.isRememberMe());

            return ResponseEntity.ok(tokenResponse);
        } catch (AuthenticationException e) {
            log.error("Authentication failed: ", e);
            TokenResponse errorResponse = new TokenResponse();
            errorResponse.setStatus("error");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        try {
            // 리프레시 토큰 검증
            if (!jwtUtil.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
            }

            // 사용자 정보 추출
            String username = jwtUtil.extractUsername(refreshToken);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 새로운 액세스 토큰 발급
            String newAccessToken = jwtUtil.generateAccessToken(authentication);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);

            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            log.error("Token refresh failed: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token refresh failed");
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String accessToken) {
        try {
            // 토큰 검증
            if (accessToken != null && accessToken.startsWith("Bearer ")) {
                String token = accessToken.substring(7);
                // 토큰 블랙리스트 처리 또는 무효화
                jwtUtil.invalidateToken(token);
            }

            SecurityContextHolder.clearContext();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Logout failed: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/email-verification-send")
    public ResponseEntity<Boolean> sendVerificationEmail(@RequestParam String email , Model model) {
        String code = emailService.sendVerificationEmail(email);
        log.info("code: {}", code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email-verification")
    public ResponseEntity<Boolean> verifyEmail(@RequestBody EmailVerificationRequest request)
    {
        boolean verification =emailService.verifyCode(request.getEmail(),request.getCode());
        return ResponseEntity.ok(verification);
    }

    @PostMapping("/nickname-verification")
    public ResponseEntity<Boolean> verifyNickname (@RequestParam String nickname){
        boolean isAvailable = userService.verifyNickname(nickname);
        return ResponseEntity.ok(isAvailable);
    }

}
