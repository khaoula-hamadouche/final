// ReunionDossierController.java
package com.example.dossiers_service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reunions")
@RequiredArgsConstructor
public class ReunionDossierController {

    private final ReunionDossierService reunionService;

    @PostMapping("/dossiers/{id}/ajouter")
    public ResponseEntity<?> ajouterReunion( // Use ResponseEntity<?> for more flexibility in error bodies
                                             @PathVariable Long id,
                                             @RequestBody Map<String, String> payload,
                                             HttpServletRequest request) {

        String dateHeureReunionStr = payload.get("dateHeureReunion");

        if (dateHeureReunionStr == null || dateHeureReunionStr.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le champ 'dateHeureReunion' est manquant ou vide."));
        }

        LocalDateTime dateHeureReunion;
        try {
            // Recommandé : Parser l'Instant (point dans le temps UTC)
            // S'attend à un format ISO 8601 comme "2025-06-18T23:36:23.000Z"
            Instant instant = Instant.parse(dateHeureReunionStr);
            // Convertir en LocalDateTime en utilisant l'offset UTC pour le stockage
            dateHeureReunion = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

        } catch (DateTimeParseException e) {
            System.err.println("Erreur de parsing de la date '" + dateHeureReunionStr + "': " + e.getMessage());
            // Retourner une erreur plus spécifique pour le client
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Format de date invalide. Attendu un format ISO 8601 (ex: 'YYYY-MM-DDTHH:mm:ss.SSSZ').",
                            "details", e.getMessage()));
        } catch (Exception e) { // Attraper toute autre erreur inattendue
            System.err.println("Une erreur inattendue est survenue lors de l'ajout de la réunion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Une erreur interne est survenue."));
        }

        try {
            ReunionDossier res = reunionService.ajouterReunion(id, dateHeureReunion, request);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) { // Gérer les exceptions au niveau du service
            System.err.println("Erreur de service lors de l'ajout de la réunion: " + e.getMessage());
            // Affiner ces retours en fonction des types de RuntimeException lancées par le service
            if (e.getMessage().contains("Dossier non trouvé")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            } else if (e.getMessage().contains("Token JWT") || e.getMessage().contains("Utilisateur non trouvé")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erreur lors du traitement de la demande: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<DossierCME>> getAllDossiers() {
        List<DossierCME> dossiers = reunionService.getAllDossiers();
        return ResponseEntity.ok(dossiers);
    }
}