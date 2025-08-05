package com.example.mealkit.controller;

import java.io.IOException;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mealkit.domain.Member;
import com.example.mealkit.service.OAuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;
    
    @Value("${naver.client-id}")
    private String naverClientId;

    @Value("${naver.client-secret}")
    private String naverClientSecret;

    @Value("${naver.redirect-uri}")
    private String naverRedirectUri;


    private final OAuthService oAuthService;

    @GetMapping("/kakao/callback")
    public void kakaoCallback(@RequestParam("code") String code, HttpServletResponse response, HttpSession session) throws IOException {
        Member member = oAuthService.kakaoLogin(code);
        session.setAttribute("loginMember", member);

        // 프론트엔드로 redirect
        response.sendRedirect("http://localhost:3000/oauth-success");
    }

    @GetMapping("/naver/callback")
    public void naverCallback(@RequestParam("code") String code,
                              @RequestParam(value = "state", required = false) String state,
                              HttpServletResponse response,
                              HttpSession session) throws IOException {
        Member member = oAuthService.naverLogin(code, state); // 네이버 로그인 처리 로직
        session.setAttribute("loginMember", member);
        response.sendRedirect("http://localhost:3000/oauth-success");
    }

}

