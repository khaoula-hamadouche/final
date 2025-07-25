package com.example.attachments_service;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Setter
    @Getter
    private String filename;
    @Setter
    @Getter
    private String filePath;
    @Setter
    @Getter
    private String fileType;
    @Setter
    @Getter
    private Long fileSize;
    @Setter
    @Getter
    private String serviceName;
    @Setter
    @Getter
    private Long entityId;
    @Setter
    @Getter// Ex: email_id ou dossier_id
    private LocalDateTime createdAt = LocalDateTime.now();

    public Attachment() {}

    public Attachment(String filename, String filePath, String fileType, Long fileSize, String serviceName, Long entityId) {
        this.filename = filename;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.serviceName = serviceName;
        this.entityId = entityId;
    }

    // Getters & Setters...
}
