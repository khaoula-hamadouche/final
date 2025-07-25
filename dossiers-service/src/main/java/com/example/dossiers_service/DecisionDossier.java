package com.example.dossiers_service;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "decision_dossier") // Nom de la table explicite
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class DecisionDossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String decision;

    @Column(updatable = false)
    private LocalDateTime dateAjout;

    @Column(nullable = false)
    private Long chargeDossierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    @JsonBackReference
    private DossierCME dossier;

    // Nouveau champ compte rendu
    @Column(length = 2000) // longueur max optionnelle, adapte selon besoin
    private String compteRendu;

    @PrePersist
    protected void onCreate() {
        this.dateAjout = LocalDateTime.now();
    }
}
