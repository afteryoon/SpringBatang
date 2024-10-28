package com.example.springbatang.user.service;

import com.example.springbatang.user.common.Role;
import com.example.springbatang.user.dto.request.UserSignupRequest;
import com.example.springbatang.user.dto.response.UserResponse;
import com.example.springbatang.user.entity.Users;
import com.example.springbatang.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Logger log = LoggerFactory.getLogger(UserServiceTest.class);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원 가입 성공")
    public void signup_success() throws Exception{
        //given
        UserSignupRequest request = createSignupRequest();
        Users mockUser = createMockUser(request);

        given(userRepository.existsByEmail(any())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("password123");
        given(userRepository.save(any(Users.class))).willReturn(mockUser);
        given(userRepository.findById(any(Long.class))).willReturn(java.util.Optional.of(mockUser));

        //when
        UserResponse response = userService.signup(request);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getNickname()).isEqualTo(request.getNickname());

        Users user = userRepository.findById(response.getId()).orElse(null); // 메서드 이름 일치

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(request.getEmail());
        assertThat(user.getNickname()).isEqualTo(request.getNickname());

    }


    //create method
    private UserSignupRequest createSignupRequest() {
        return UserSignupRequest.builder()
                .email("test@example.com")
                .password("password123")
                .nickname("testUser")
                .build();
    }

    private Users createMockUser(UserSignupRequest request) {
        return Users.builder()
                .email(request.getEmail())
                .password("password123")
                .nickname(request.getNickname())
                .role(Role.ROLE_USER)
                .build();
    }

}