package com.example.mealkit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.mealkit.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final CustomOAuth2UserService customOAuth2UserService;

	//비밀번호 암호화 엔코더 등록
	//public 반환데이터형 메서드()
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {	
		return new BCryptPasswordEncoder();
	}
	
	//로그인 기능 명시적 비활성화
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/","/api/**","/oauth2/**").permitAll()
				.anyRequest().permitAll()
				)
				.oauth2Login(oauth2 -> oauth2
				.loginPage("/")
				.userInfoEndpoint(userInfo -> userInfo
						.userService(customOAuth2UserService)
						)
				.defaultSuccessUrl("/", true)
				);
		
		return http.build();	//build->생성 된 작업물을 외부에 넘기는
	}
	
}
