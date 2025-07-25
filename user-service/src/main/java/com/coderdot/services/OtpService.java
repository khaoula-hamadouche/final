package com.coderdot.services;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class OtpService {
    private final Map<String, String> otpStorage = new HashMap<>();
    private final Map<String, LocalDateTime> otpExpiry = new HashMap<>();
    private static final int OTP_EXPIRY_MINUTES = 10;

    public void saveOtp(String email, String otp) {
        otpStorage.put(email, otp);
        otpExpiry.put(email, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        System.out.println("OTP enregistré pour " + email + " : " + otp);
    }

    public boolean verifyOtp(String email, String otp) {
        if (!otpStorage.containsKey(email)) {
            System.out.println("OTP introuvable pour " + email);
            return false;
        }

        if (otpExpiry.get(email).isBefore(LocalDateTime.now())) {
            System.out.println("OTP expiré pour " + email);
            otpStorage.remove(email);
            otpExpiry.remove(email);
            return false;
        }

        if (otpStorage.get(email).equals(otp)) {
            System.out.println("OTP validé pour " + email);
            otpStorage.remove(email);
            otpExpiry.remove(email);
            return true;
        } else {
            System.out.println("OTP incorrect pour " + email);
            return false;
        }
    }
}
