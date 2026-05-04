package com.cpt202.service;

import com.cpt202.model.entity.User;

public interface EmailLoginOtpMailService {

    void sendLoginOtpMail(User user, String otp);
}
