package com.example.email_service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailArchiveRepository emailArchiveRepository;

    private final RestTemplate restTemplate;

    private static final String ATTACHMENT_SERVICE_URL = "http://localhost:8083/api/attachments/upload";
    private static final String ATTACHMENT_VIEW_URL = "http://localhost:8083/api/attachments/view/";

    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public void sendEmailWithAttachment(String fromEmail, List<String> toList, String subject, String text, MultipartFile file)
            throws MessagingException {

        for (String to : toList) {
            try {
                // 🔹 1️⃣ Créer et enregistrer l'email en base sans pièce jointe
                EmailArchive archive = new EmailArchive(fromEmail, to, subject, text, null);
                archive = emailArchiveRepository.saveAndFlush(archive); // 💾 Force l'enregistrement en base
                Long emailId = archive.getId();
                LOGGER.info("📩 Email enregistré avec ID : " + emailId);

                // 🔹 2️⃣ Uploader la pièce jointe et récupérer fileId
                String fileId = null;
                if (file != null && !file.isEmpty()) {
                    try {
                        fileId = uploadFileToAttachmentService(file, "emails", emailId);
                        LOGGER.info("📎 fileId récupéré après upload : " + fileId);
                        archive.setFileId(fileId);
                        emailArchiveRepository.saveAndFlush(archive);
                        LOGGER.info("💾 fileId enregistré en base : " + archive.getFileId());
                    } catch (IOException e) {
                        LOGGER.severe("❌ Échec de l'upload de la pièce jointe pour l'email ID: " + emailId + " -> " + e.getMessage());
                    }
                }

                // 🔹 3️⃣ Construire et envoyer l’email

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setFrom(fromEmail);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(text);
                if (fileId != null) {
                    helper.addAttachment(file.getOriginalFilename(), file);
                }



                mailSender.send(message);
                LOGGER.info("✅ Email envoyé avec succès à : " + to);

            } catch (Exception e) {
                LOGGER.severe("❌ Erreur lors de l'envoi de l'email à " + to + " -> " + e.getMessage());
            }
        }
    }

    private String uploadFileToAttachmentService(MultipartFile file, String category, Long emailId) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Le fichier est vide !");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("serviceName", category);
        body.add("entityId", emailId.toString());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(ATTACHMENT_SERVICE_URL, requestEntity, Map.class);
        } catch (Exception e) {
            throw new IOException("❌ Service de pièces jointes inaccessible !");
        }

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Object fileIdObject = response.getBody().get("fileId");

            if (fileIdObject != null) {
                String fileId = fileIdObject.toString();
                LOGGER.info("📎 fileId retourné par le service : " + fileId);
                return fileId;
            } else {
                throw new IOException("❌ Réponse invalide du service d'attachement : fileId manquant !");
            }
        } else {
            throw new IOException("❌ Échec de l'upload du fichier !");
        }
    }

    public void sendEmailWithoutAttachment(String from, List<String> toList, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(from);
        helper.setTo(toList.toArray(new String[0]));
        helper.setSubject(subject);
        helper.setText(text, true);

        mailSender.send(message);

        // 🔴 Archivage
        for (String to : toList) {
            EmailArchive archive = new EmailArchive(from, to, subject, text, null);
            emailArchiveRepository.save(archive);
        }
    }
}
