package com.example.email_service;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor // Génère un constructeur par défaut
public class EmailArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String recipient;

    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false) // Stocker un texte long
    private String content;

    private boolean isRead; // Ajouter cette propriété

    @Getter
    @Column(name = "file_id")
    private String fileId; // Identifiant unique du fichier attaché (au lieu du chemin direct)

    private LocalDateTime sentAt = LocalDateTime.now(); // Date d'envoi automatique

    public EmailArchive(String sender, String recipient, String subject, String content, String fileId) {
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.fileId = fileId;
        this.sentAt = LocalDateTime.now();
    }
}
