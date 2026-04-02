package com.ganesh.portfolio_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ganesh.portfolio_backend.dto.ContactRequest;
import com.ganesh.portfolio_backend.service.EmailService;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody Map<String, String> payload) {
        emailService.sendOtpEmail(payload.get("email"));
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        boolean valid = emailService.verifyOtp(payload.get("email"), payload.get("otp"));
        if (valid) {
            return ResponseEntity.ok(Map.of("verified", true));
        }
        return ResponseEntity.status(400).body(Map.of("verified", false, "message", "Invalid OTP"));
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ContactRequest request) {
        try {
            emailService.sendContactEmail(request);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false));
        }
    }
}