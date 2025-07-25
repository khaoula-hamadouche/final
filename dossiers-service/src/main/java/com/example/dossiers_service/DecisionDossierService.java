package com.example.dossiers_service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DecisionDossierService {

    private final DecisionDossierRepository decisionRepo;
    private final DossierRepository dossierRepo;
    private final JwtUtil jwtUtil;
    private final UserServiceClient userServiceClient;
    private final DossierRepository dossierRepository;


    public DecisionDossier ajouterDecision(Long dossierId, String decision, String compteRendu, HttpServletRequest request) {
        String jwtToken = JwtUtil.extractTokenFromCookie(request);
        if (jwtToken == null) throw new IllegalArgumentException("❌ Token JWT non trouvé !");
        if (!jwtUtil.validateToken(jwtToken)) throw new RuntimeException("❌ Token JWT invalide ou expiré !");
        Long userId = jwtUtil.extractUserId(jwtToken);
        if (userId == null) throw new RuntimeException("❌ Impossible d'extraire l'ID utilisateur depuis le token JWT !");
        String cookieHeader = "jwt=" + jwtToken;

        UserDTO utilisateur = userServiceClient.getUserById(userId, cookieHeader);
        if (utilisateur == null) throw new RuntimeException("❌ Utilisateur non trouvé.");

        Long chargeDossierId = utilisateur.getId();
        DossierCME dossier = dossierRepo.findById(dossierId).orElseThrow(() -> new RuntimeException("❌ Dossier non trouvé."));

        if (decisionRepo.findByDossier_IdAndChargeDossierId(dossierId, chargeDossierId).isPresent()) {
            throw new RuntimeException("⚠️ Vous avez déjà ajouté une décision pour ce dossier.");
        }

        DecisionDossier d = DecisionDossier.builder()
                .dossier(dossier)
                .chargeDossierId(chargeDossierId)
                .decision(decision)
                .compteRendu(compteRendu)  // Ajout du compte rendu ici
                .build();

        return decisionRepo.save(d);
    }


    public List<DecisionDossier> getAllByDossier(Long dossierId) {
        return decisionRepo.findByDossier_Id(dossierId);
    }
    public List<DossierCME> getDossiersByDecisionDirect(String decision) {
        return decisionRepo.findDossiersByDecision(decision);
    }

    public List<DecisionDossier> getMesDecisions(HttpServletRequest request) {
        String jwtToken = JwtUtil.extractTokenFromCookie(request);
        if (jwtToken == null) throw new IllegalArgumentException("❌ Token JWT non trouvé !");
        if (!jwtUtil.validateToken(jwtToken)) throw new RuntimeException("❌ Token JWT invalide ou expiré !");
        Long userId = jwtUtil.extractUserId(jwtToken);
        return decisionRepo.findByChargeDossierId(userId);
    }

    public List<DossierCME> getDossiersSansDecision() {
        return decisionRepo.findDossiersSansDecision();
    }

    public void deleteDecision(Long id, HttpServletRequest request) {
        String jwtToken = JwtUtil.extractTokenFromCookie(request);
        if (jwtToken == null) throw new IllegalArgumentException("❌ Token JWT non trouvé !");
        if (!jwtUtil.validateToken(jwtToken)) throw new RuntimeException("❌ Token JWT invalide ou expiré !");
        Long userId = jwtUtil.extractUserId(jwtToken);

        DecisionDossier d = decisionRepo.findById(id).orElseThrow(() -> new RuntimeException("❌ Décision non trouvée."));
        if (!d.getChargeDossierId().equals(userId)) {
            throw new RuntimeException("❌ Vous n'avez pas le droit de supprimer cette décision.");
        }

        decisionRepo.delete(d);
    }
    public Map<String, Long> getDecisionCounts() {
        List<Object[]> results = decisionRepo.countDecisionsGroupedByDecisionType();
        Map<String, Long> decisionCounts = new HashMap<>();
        for (Object[] result : results) {
            String decisionType = (String) result[0];
            Long count = (Long) result[1];
            decisionCounts.put(decisionType, count);
        }

        // Ensure all 4 standard decisions are present, even if count is 0
        String[] standardDecisions = {
                "Visa sans réserve",
                "Refus de visa",
                "Visa avec réserve suspensive",
                "Visa avec réserve non suspensive"
        };
        for (String stdDecision : standardDecisions) {
            decisionCounts.putIfAbsent(stdDecision, 0L);
        }

        return decisionCounts;
    }
}
