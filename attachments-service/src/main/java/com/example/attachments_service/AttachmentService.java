package com.example.attachments_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class AttachmentService {

    private static final String BASE_UPLOAD_DIR = System.getProperty("user.home") + "/uploads/";
    private final AttachmentRepository attachmentRepository;

    @Autowired
    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }
    public Long uploadFile(MultipartFile file, String apiName, Long entityId) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Le fichier est vide !");
        }

        if (apiName == null || apiName.isBlank()) {
            throw new IOException("Le nom de l'API ne peut pas être vide !");
        }

        // 🔹 Définition du dossier spécifique à l'API
        String uploadDir = BASE_UPLOAD_DIR + apiName + "/";
        File directory = new File(uploadDir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Impossible de créer le dossier d'upload !");
        }

        // 🔹 Nettoyage et validation du nom du fichier
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IOException("Nom de fichier invalide !");
        }
        originalFilename = sanitizeFilename(originalFilename);

        // 🔹 Vérification de l'extension
        String fileExtension = getFileExtension(originalFilename);
        if (fileExtension.isEmpty()) {
            throw new IOException("Fichier sans extension invalide !");
        }

        // 🔹 Génération du nom final : "id_email_nomDuFichier.ext"
        String newFilename = entityId + "_" + originalFilename;
        String filePath = uploadDir + newFilename;

        // 🔹 Enregistrement du fichier
        Path path = Paths.get(filePath);
        Files.copy(file.getInputStream(), path);

        // 🔹 Sauvegarde en base de données
        Attachment attachment = new Attachment(
                newFilename,
                filePath,
                file.getContentType(),
                file.getSize(),
                apiName,
                entityId
        );
        attachment = attachmentRepository.save(attachment);

        return attachment.getId(); // 🔹 Retourner l'ID du fichier
    }


    public Attachment getFile(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fichier introuvable !"));
    }

    public List<Attachment> getAllFiles() {
        return attachmentRepository.findAll();
    }

    @Transactional
    public boolean deleteFile(Long id) {
        Attachment attachment = attachmentRepository.findById(id).orElse(null);
        if (attachment == null) {
            return false;
        }

        Path filePath = Paths.get(attachment.getFilePath());

        try {
            // Supprime le fichier uniquement s'il existe
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier sur le disque : " + e.getMessage(), e);
        }

        // Supprime l'entrée de la base de données même si le fichier n'existe pas
        attachmentRepository.deleteById(id);
        return true;
    }


    // 🔹 Fonction pour nettoyer le nom du fichier
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    // 🔹 Fonction pour récupérer l'extension d'un fichier
    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf(".");
        return (lastIndex == -1) ? "" : filename.substring(lastIndex);
    }
}
