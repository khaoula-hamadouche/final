package com.example.dossiers_service;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
@Entity
@Table(name = "reunion_dossier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReunionDossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateHeureReunion;

    @Column(nullable = false)
    private Long chargeDossierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    @JsonBackReference
    private DossierCME dossier;

}
