package com.example.dossiers_service;


import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DecisionDossierRepository extends JpaRepository<DecisionDossier, Long> {
    Optional<DecisionDossier> findByDossier_IdAndChargeDossierId(Long dossierId, Long chargeDossierId);
    List<DecisionDossier> findByDossier_Id(Long dossierId);
    List<DecisionDossier> findByChargeDossierId(Long chargeDossierId);
    @Query("SELECT dd.dossier FROM DecisionDossier dd WHERE LOWER(dd.decision) = LOWER(:decision)")
    List<DossierCME> findDossiersByDecision(@Param("decision") String decision);

    @Query("SELECT d FROM DossierCME d WHERE d.id NOT IN (SELECT dd.dossier.id FROM DecisionDossier dd)")
    List<DossierCME> findDossiersSansDecision();
    @Query("SELECT dd.decision, COUNT(dd) FROM DecisionDossier dd GROUP BY dd.decision")
    List<Object[]> countDecisionsGroupedByDecisionType();

}
