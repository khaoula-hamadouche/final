package com.coderdot.controllers;

import com.coderdot.dto.AuthentificationRequest;
import com.coderdot.dto.AuthentificationResponse;
import com.coderdot.entities.User;
import com.coderdot.repository.UserRepository;
import com.coderdot.services.OtpService;
import com.coderdot.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.coderdot.dto.ForgotPasswordRequest;
import com.coderdot.dto.ResetPasswordRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import com.coderdot.services.EmailService;

import java.util.*;
import java.util.stream.Collectors;




import java.util.*;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")

public class AuthentificationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OtpService otpService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    private static final int OTP_EXPIRY_MINUTES = 10;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/authenticate")
    public AuthentificationResponse createAuthentificationToken(
            @RequestBody AuthentificationRequest authentificationRequest, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authentificationRequest.getEmail(),
                            authentificationRequest.getPassword()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(authentificationRequest.getEmail());
            User user = userRepository.findFirstByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

            String jwt = jwtUtil.generateToken(user);
            ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                    .httpOnly(true)
                    .secure(false) // ⚠️ Mettre false pour HTTP
                    .path("/")
                    .sameSite("Lax") // ou "None" si cross-origin
                    .maxAge(3600)
                    .build();

            response.setHeader("Set-Cookie", cookie.toString());

            Set<String> roles = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());
            Set<String> permissions = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(permission -> permission.getName())
                    .collect(Collectors.toSet());

            return new AuthentificationResponse(
                    "Authenticated",
                    user.getId(),
                    user.getEmail(),
                    null,
                    roles,
                    permissions,
                    user.getName()

            );

        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte désactivé.");
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect.");
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("jwt".equals(c.getName())) {
                    c.setValue("");
                    c.setPath("/");
                    c.setMaxAge(0);
                    response.addCookie(c);
                    break;
                }
            }
        }

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Déconnexion réussie");

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        Optional<User> optionalUser = userRepository.findFirstByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalUser.get();
        String otp = emailService.generateOtp();
        otpService.saveOtp(user.getEmail(), otp); // ✅ Enregistre l'OTP dans OtpService
        emailService.sendOtpEmail(user.getEmail(), otp);

        System.out.println("OTP envoyé pour " + user.getEmail() + " : " + otp); // ✅ Log OTP
        return ResponseEntity.ok("OTP sent to email.");
    }
    private final Map<String, String> verifiedUsers = new HashMap<>(); // Stocke les emails vérifiés temporairement

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        System.out.println("Vérification de l'OTP pour : " + email + " avec code : " + otp);

        boolean isValid = otpService.verifyOtp(email, otp);

        Map<String, String> response = new HashMap<>();
        if (isValid) {
            verifiedUsers.put(email, "VERIFIED"); // Stocke l'utilisateur vérifié

            response.put("message", "OTP verified successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "OTP incorrect");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        for (String email : verifiedUsers.keySet()) { // Vérifie s'il y a un utilisateur validé
            Optional<User> optionalUser = userRepository.findFirstByEmail(email);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                // Vérifier que les mots de passe correspondent
                if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                    return ResponseEntity.badRequest()
                            .body(Collections.singletonMap("message", "Les mots de passe ne correspondent pas !"));
                }

                // Hacher et enregistrer le nouveau mot de passe
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);

                verifiedUsers.remove(email); // Supprimer après réinitialisation pour éviter les abus

                return ResponseEntity.ok(Collections.singletonMap("message", "Mot de passe réinitialisé avec succès !"));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Collections.singletonMap("message", "Aucune vérification OTP trouvée !"));
    }


}
