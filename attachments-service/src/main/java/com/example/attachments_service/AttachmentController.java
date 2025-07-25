package com.example.attachments_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    // Liste des extensions autorisées
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".png", ".pdf", ".docx");

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("serviceName") String serviceName,
            @RequestParam(value = "entityId", required = false) Long entityId) {

        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le fichier ne peut pas être vide !");
        }

        // Vérification du type de fichier autorisé
        String filename = file.getOriginalFilename();
        if (filename == null || ALLOWED_EXTENSIONS.stream().noneMatch(filename.toLowerCase()::endsWith)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Type de fichier non autorisé !");
        }

        try {
            Long fileId = attachmentService.uploadFile(file, serviceName, entityId);
            return ResponseEntity.ok().body(Map.of("fileId", fileId)); // 🔹 Retourne un objet JSON avec l'ID
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du téléchargement : " + e.getMessage());
        }
    }


    @GetMapping("/view/{id}")
    public ResponseEntity<?> viewFile(@PathVariable Long id) {
        try {
            Attachment attachment = attachmentService.getFile(id);
            if (attachment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fichier introuvable !");
            }

            Path filePath = Paths.get(attachment.getFilePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fichier non accessible !");
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération du fichier : " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Attachment>> getAllFiles() {
        return ResponseEntity.ok(attachmentService.getAllFiles());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        try {
            boolean deleted = attachmentService.deleteFile(id);
            if (deleted) {
                return ResponseEntity.ok("Fichier supprimé avec succès.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fichier introuvable en base.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression du fichier : " + e.getMessage());
        }
    }

}
