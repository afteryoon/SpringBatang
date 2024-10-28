package com.example.springbatang.user.controller;

import com.example.springbatang.user.dto.request.UserSignupRequest;
import com.example.springbatang.user.dto.request.createUserRequest;
import com.example.springbatang.user.dto.response.UserResponse;
import com.example.springbatang.user.entity.Users;
import com.example.springbatang.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    //로그인 페이지
    @GetMapping("/login")
    public String showLogin() {
        return "/user/login";  // login.html 반환
    }

    //회원가입 페이지
    @GetMapping("/signup")
    public String showSingup(){
        return "/user/signup";
    }

    @PostMapping("/signup")
    public String createUser(UserSignupRequest request){
        UserResponse createdUser = userService.signup(request);
        return "redirect:/auth/login";
    }
}