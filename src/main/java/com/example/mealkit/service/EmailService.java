package com.example.mealkit.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTempPassword(String toEmail, String tempPassword) {
    	SimpleMailMessage message = new SimpleMailMessage();
    	message.setTo(toEmail);
    	message.setFrom("ahrud12@naver.com");  // ✅ 로그인 계정과 동일해야 함
    	message.setSubject("임시 비밀번호 발송 안내");
    	message.setText("임시 비밀번호: " + tempPassword);
    	mailSender.send(message);

    }
}

