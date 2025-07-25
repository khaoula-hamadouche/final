package com.example.email_service;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/email")
@CrossOrigin(origins = "http://10.16.100.36", allowCredentials = "true")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailArchiveRepository emailArchiveRepository;

    private static final String COOKIE_NAME = "jwt";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private boolean hasPermission(String token, String requiredPermission) {
        try {
            return jwtUtil.extractPermissions(token).contains(requiredPermission);
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/send-email-attachment")
    public ResponseEntity<Map<String, String>> sendEmailWithAttachment(
            HttpServletRequest request,
            @RequestParam("to") List<String> toList,
            @RequestParam("subject") String subject,
            @RequestParam("text") String text,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Map<String, String> response = new HashMap<>();
        String token = extractTokenFromCookie(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token JWT invalide ou expiré !"));
        }

        String userEmail = jwtUtil.extractUsername(token);
        if (!hasPermission(token, "SENDEMAIL")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Permission refusée : SENDEMAIL requise !"));
        }

        if (toList.isEmpty() || toList.stream().anyMatch(email -> !EMAIL_PATTERN.matcher(email).matches())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Adresse e-mail invalide ou liste vide !"));
        }

        try {
            if (file != null && !file.isEmpty()) {
                emailService.sendEmailWithAttachment(userEmail, toList, subject, text, file);
                response.put("message", "E-mail avec pièce jointe envoyé avec succès.");
            } else {
                emailService.sendEmailWithoutAttachment(userEmail, toList, subject, text);
                response.put("message", "E-mail sans pièce jointe envoyé avec succès.");
            }
            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erreur lors de l'envoi de l'e-mail : " + e.getMessage()));
        }
    }

    @GetMapping("/archive")
    public ResponseEntity<?> getArchivedEmails(HttpServletRequest request) {
        String token = extractTokenFromCookie(request);
        if (token == null || !hasPermission(token, "SEEALLEMAIL")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé !");
        }

        List<EmailDTO> emails = emailArchiveRepository.findAll().stream()
                .map(this::mapToEmailDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(emails);
    }

    @GetMapping("/sent")
    public ResponseEntity<List<EmailDTO>> getSentEmails(HttpServletRequest request) {
        return getEmails(request, "SEEEMAIL", emailArchiveRepository::findBySender);
    }

    @GetMapping("/received")
    public ResponseEntity<List<EmailDTO>> getReceivedEmails(HttpServletRequest request) {
        return getEmails(request, "SEEEMAIL", emailArchiveRepository::findByRecipient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmailById(HttpServletRequest request, @PathVariable Long id) {
        String token = extractTokenFromCookie(request);
        if (token == null || !hasPermission(token, "SEEEMAIL")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé !");
        }

        Optional<EmailArchive> emailOptional = emailArchiveRepository.findById(id);
        if (emailOptional.isPresent()) {
            // Marquer l'email comme lu lors de la récupération des détails
            EmailArchive email = emailOptional.get();
            email.setRead(true); // Assurez-vous d'avoir cette propriété et le setter dans votre entité EmailArchive
            emailArchiveRepository.save(email);
            return ResponseEntity.ok(mapToEmailDTO(email));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "E-mail non trouvé avec l'ID : " + id));
        }
    }
    private ResponseEntity<List<EmailDTO>> getEmails(HttpServletRequest request, String permission, java.util.function.Function<String, List<EmailArchive>> finder) {
        String token = extractTokenFromCookie(request);
        if (token == null || !hasPermission(token, permission)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String userEmail = jwtUtil.extractUsername(token);
        List<EmailDTO> emails = finder.apply(userEmail).stream()
                .map(this::mapToEmailDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(emails);
    }

    private EmailDTO mapToEmailDTO(EmailArchive email) {
        String attachmentUrl = (email.getFileId() != null) ?
                "http://localhost:8083/api/attachments/view/" + email.getFileId() : null;

        return new EmailDTO(
                email.getId(),
                email.getSubject(),
                email.getContent(),
                email.getRecipient(),
                email.getSender(),
                email.getSentAt(),
                attachmentUrl
        );
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}