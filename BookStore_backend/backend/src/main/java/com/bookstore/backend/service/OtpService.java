package com.bookstore.backend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final String PREFIX_REGISTRATION = "OTP:REG_EXPIRE:";
    private static final String PREFIX_FORGOT_PASSWORD = "OTP:FORGOT_EXPIRE:";
    private static final int OTP_EXPIRY_MINUTES = 3;

    public OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateAndSaveOtp(String email, boolean isRegistration) {
        String prefix = isRegistration ? PREFIX_REGISTRATION : PREFIX_FORGOT_PASSWORD;
        String key = prefix + email.trim().toLowerCase();
        String otpCode = String.format("%06d", secureRandom.nextInt(1000000));
        redisTemplate.opsForValue().set(key, otpCode, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
        return otpCode;
    }

    public boolean validateOtp(String email, String userInputOtp, boolean isRegistration) {
        String prefix = isRegistration ? PREFIX_REGISTRATION : PREFIX_FORGOT_PASSWORD;
        String key = prefix + email.trim().toLowerCase();
        String cachedOtp = redisTemplate.opsForValue().get(key);

        if (cachedOtp == null) return false;
        boolean isValid = cachedOtp.equals(userInputOtp.trim());
        if (isValid) redisTemplate.delete(key);
        return isValid;
    }
}