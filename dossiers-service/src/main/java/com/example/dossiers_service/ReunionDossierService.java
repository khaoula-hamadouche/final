// ReunionDossierService.java
package com.example.dossiers_service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReunionDossierService {

    private final ReunionDossierRepository reunionRepo;
    private final DossierRepository dossierRepo;
    private final JwtUtil jwtUtil; // Assumé correctement implémenté
    private final UserServiceClient userServiceClient; // Assumé correctement implémenté

    public ReunionDossier ajouterReunion(Long dossierId, LocalDateTime dateHeureReunion, HttpServletRequest request) {
        // 1. Extraire le token JWT
        String jwtToken = JwtUtil.extractTokenFromCookie(request);
        if (jwtToken == null) {
            throw new RuntimeException("Token JWT non trouvé dans les cookies.");
        }
        if (!jwtUtil.validateToken(jwtToken)) {
            throw new RuntimeException("Token JWT invalide ou expiré.");
        }
        Long userId = jwtUtil.extractUserId(jwtToken);
        if (userId == null) {
            throw new RuntimeException("Impossible d'extraire l'ID utilisateur depuis le token JWT.");
        }

        // Ajouter l'en-tête de cookie pour le client du service utilisateur si nécessaire pour les appels internes
        String cookieHeader = "jwt=" + jwtToken;

        // 2. Vérifier utilisateur via UserServiceClient
        UserDTO utilisateur = userServiceClient.getUserById(userId, cookieHeader);
        if (utilisateur == null) {
            throw new RuntimeException("Utilisateur non trouvé via le service utilisateur pour l'ID: " + userId + ".");
        }
        Long chargeDossierId = utilisateur.getId();

        // 3. Vérifier dossier
        DossierCME dossier = dossierRepo.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé pour l'ID: " + dossierId + "."));

        // 4. Trouver la réunion existante ou en créer une nouvelle
        Optional<ReunionDossier> existingReunionOptional = reunionRepo.findByDossier_IdAndChargeDossierId(dossierId, chargeDossierId);

        ReunionDossier reunion;
        if (existingReunionOptional.isPresent()) {
            // Si une réunion existe, mettre à jour sa date et heure
            reunion = existingReunionOptional.get();
            reunion.setDateHeureReunion(dateHeureReunion);
            System.out.println("✅ Réunion existante mise à jour pour le dossier " + dossierId + " par l'utilisateur " + chargeDossierId);
        } else {
            // Si aucune réunion n'existe, en créer une nouvelle
            reunion = ReunionDossier.builder()
                    .dossier(dossier)
                    .chargeDossierId(chargeDossierId)
                    .dateHeureReunion(dateHeureReunion)
                    .build();
            System.out.println("✅ Nouvelle réunion créée pour le dossier " + dossierId + " par l'utilisateur " + chargeDossierId);
        }

        // 5. Sauvegarder la réunion (nouvelle ou mise à jour)
        return reunionRepo.save(reunion);
    }

    public List<DossierCME> getAllDossiers() {
        return dossierRepo.findAll();
    }
}