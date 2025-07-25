package com.example.dossiers_service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReunionDossierRepository extends JpaRepository<ReunionDossier, Long> {
    Optional<ReunionDossier> findByDossier_IdAndChargeDossierId(Long dossierId, Long chargeDossierId);
}
