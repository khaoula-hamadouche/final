package com.example.dossiers_service;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
public class DecisionDossierController {

    private final DecisionDossierService decisionService;

    @PostMapping("/dossiers/{id}/ajouter")
    public ResponseEntity<DecisionDossier> ajouterDecision(
            @PathVariable Long id,
            @RequestParam String decision,
            @RequestParam(required = false) String compteRendu, // nouveau paramètre facultatif
            HttpServletRequest request) {

        DecisionDossier res = decisionService.ajouterDecision(id, decision, compteRendu, request);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/decision/{decision}")
    public ResponseEntity<List<DossierCME>> getDossiersParDecision(@PathVariable String decision) {
        List<DossierCME> dossiers = decisionService.getDossiersByDecisionDirect(decision);
        if (dossiers.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(dossiers); // 200 OK avec JSON
    }




    @GetMapping("/dossiers/{id}")
    public ResponseEntity<List<DecisionDossier>> getDecisionsByDossier(@PathVariable Long id) {
        List<DecisionDossier> decisions = decisionService.getAllByDossier(id);
        if (decisions.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(decisions);
    }

    @GetMapping("/mes-decisions")
    public ResponseEntity<List<DecisionDossier>> getMesDecisions(HttpServletRequest request) {
        List<DecisionDossier> decisions = decisionService.getMesDecisions(request);
        return ResponseEntity.ok(decisions);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDecision(@PathVariable Long id, HttpServletRequest request) {
        decisionService.deleteDecision(id, request);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/dossiers-sans-decision")
    public ResponseEntity<List<DossierCME>> getDossiersSansDecision() {
        List<DossierCME> dossiers = decisionService.getDossiersSansDecision();
        if (dossiers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dossiers);
    }
    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getDecisionCounts() {
        Map<String, Long> counts = decisionService.getDecisionCounts();
        if (counts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(counts);
    }

}
