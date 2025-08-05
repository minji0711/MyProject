package com.example.mealkit.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	//Cors 설정
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("http://localhost:3000")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
				.allowCredentials(true);
		
	}			
		
	//외부설정 파일(application.properties 파일등)의 값을 자바 필드에 주입하는 방법
	@Value("${upload.path}")
	private String uploadPath;
	
	//정적 자원 핸들링
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		
	//파일객체 변환 및 절대경로 반환
	String absolutePath = new File(uploadPath).getAbsolutePath()+File.separator;
		registry.addResourceHandler("/images/**")
				.addResourceLocations("file:" + absolutePath);
		
	}
	
}
