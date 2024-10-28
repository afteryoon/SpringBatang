package com.example.springbatang.user.service;


import com.example.springbatang.config.globalExeptionHandler.custom.DuplicateEmailException;
import com.example.springbatang.user.common.Role;
import com.example.springbatang.user.dto.request.UserSignupRequest;
import com.example.springbatang.user.dto.response.UserResponse;
import com.example.springbatang.user.entity.Users;
import com.example.springbatang.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse signup(UserSignupRequest request) {
        validateDuplicateEmail(request.getEmail());

        Users signupUser = userSignupRequestToUser(request);
        signupUser.updateRole(Role.USER);
        signupUser.updateActivated(true);

        signupUser =  userRepository.save(signupUser);
        log.info("signUser : {}", signupUser);

        return convertUserResponse(signupUser);
    }

    //convert
    private Users userSignupRequestToUser(UserSignupRequest request) {
        return Users.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();
    }

    private UserResponse convertUserResponse(Users user){
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    //이메일 중복 검사
    private void validateDuplicateEmail(String email) {
        if(userRepository.existsByEmail(email))
            throw new DuplicateEmailException("이미 가입된 이메일입니다");
    }

    public boolean verifyNickname(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }
}
