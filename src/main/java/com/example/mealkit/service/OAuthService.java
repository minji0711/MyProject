package com.example.mealkit.service;

import com.example.mealkit.domain.Member;
import com.example.mealkit.repository.MemberRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final MemberRepository memberRepository;

    //카카오 설정값
    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.client-secret:}")  // client-secret 없이도 동작 가능
    private String kakaoClientSecret;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;
    
    //네이버 설정값
    @Value("${naver.client-id}")
    private String naverClientId;

    @Value("${naver.client-secret}")
    private String naverClientSecret;

    public Member kakaoLogin(String code) {
        String accessToken = requestAccessToken(code);
        KakaoUser kakaoUser = requestUserInfo(accessToken);

        // 기존 회원 확인 (이메일 기준)
        Member member = memberRepository.findByEmail(kakaoUser.getEmail());
        if (member == null) {
            member = new Member();
            member.setEmail(kakaoUser.getEmail());
            member.setName(kakaoUser.getNickname());
            member.setPhone(kakaoUser.getPhoneNumber());
            member.setGender(kakaoUser.getGender());
            member.setBirthday(kakaoUser.getBirthdate());
            member.setPassword(null); // 소셜로그인은 패스워드 없이 가입
            member.setAdmin(false);
            member.setCreatedAt(LocalDateTime.now());
            memberRepository.save(member);
        }
        return member;
    }

    // 1단계: Access Token 발급 요청
    private String requestAccessToken(String code) {
        String url = "https://kauth.kakao.com/oauth/token";
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);

        if (kakaoClientSecret != null && !kakaoClientSecret.isBlank()) {
            params.add("client_secret", kakaoClientSecret);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<?> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        return (String) response.getBody().get("access_token");
    }

    // 2단계: 사용자 정보 조회
    private KakaoUser requestUserInfo(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        Map<String, Object> kakaoAccount = (Map<String, Object>) response.getBody().get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        KakaoUser user = new KakaoUser();
        user.setEmail((String) kakaoAccount.get("email"));
        user.setNickname((String) profile.get("nickname"));
        user.setPhoneNumber((String) kakaoAccount.get("phone_number"));
        user.setGender((String) kakaoAccount.get("gender"));

        // 생년월일 가공
        String birthyear = (String) kakaoAccount.get("birthyear");
        String birthday = (String) kakaoAccount.get("birthday");
        if (birthyear != null && birthday != null && birthday.length() == 4) {
            user.setBirthdate(birthyear + "-" + birthday.substring(0, 2) + "-" + birthday.substring(2));
        }

        return user;
    }

    public Member naverLogin(String code, String state) {
        String accessToken = requestNaverAccessToken(code, state);
        NaverUser naverUser = requestNaverUserInfo(accessToken);

        Member member = memberRepository.findByEmail(naverUser.getEmail());
        if (member == null) {
            member = new Member();
            member.setEmail(naverUser.getEmail());
            member.setName(naverUser.getName());
            member.setPhone(naverUser.getPhoneNumber());
            member.setGender(naverUser.getGender());
            member.setBirthday(naverUser.getBirthdate());
            member.setPassword(null);
            member.setAdmin(false);
            member.setCreatedAt(LocalDateTime.now());
            memberRepository.save(member);
        }
        return member;
    }

    private String requestNaverAccessToken(String code, String state) {
        String url = "https://nid.naver.com/oauth2.0/token";
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("code", code);
        params.add("state", state);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<?> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        return (String) response.getBody().get("access_token");
    }

    private NaverUser requestNaverUserInfo(String accessToken) {
        String url = "https://openapi.naver.com/v1/nid/me";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("response");

        NaverUser user = new NaverUser();
        user.setEmail((String) responseData.get("email"));
        user.setName((String) responseData.get("name"));
        user.setPhoneNumber((String) responseData.get("mobile"));
        user.setGender((String) responseData.get("gender"));

        String birthyear = (String) responseData.get("birthyear");
        String birthday = (String) responseData.get("birthday");
        if (birthyear != null && birthday != null) {
            user.setBirthdate(birthyear + "-" + birthday);
        }

        return user;
    }
    
    // 내부 전용 DTO
    @Data
    public static class KakaoUser {
        private String email;
        private String nickname;
        private String phoneNumber;
        private String gender;
        private String birthdate;
    }
    
    @Data
    public static class NaverUser {
        private String email;
        private String name;
        private String phoneNumber;
        private String gender;
        private String birthdate;
    }
}
