package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.BusinessException;
import com.cpt202.model.entity.User;
import com.cpt202.service.EmailLoginOtpMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Sends email OTPs for passwordless login.
 */
@Service
@RequiredArgsConstructor
public class EmailLoginOtpMailServiceImpl implements EmailLoginOtpMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.email-login-otp-expiration-minutes:5}")
    private long expirationMinutes;

    @Override
    public void sendLoginOtpMail(User user, String otp) {
        if (!StringUtils.hasText(mailFrom)) {
            throw new BusinessException(MessageConstants.EMAIL_OTP_MAIL_NOT_CONFIGURED);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("CPT202 Login Verification Code");
        message.setText(buildMailContent(user, otp));
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new BusinessException(MessageConstants.EMAIL_OTP_MAIL_SEND_FAILED);
        }
    }

    private String buildMailContent(User user, String otp) {
        String displayName = StringUtils.hasText(user.getFullName()) ? user.getFullName() : user.getUsername();
        return """
                Hello %s,

                Your CPT202 Project Selection System login verification code is:

                %s

                This code will expire in %d minutes.
                If you did not request this login, you can safely ignore this email.
                """.formatted(displayName, otp, expirationMinutes);
    }
}
