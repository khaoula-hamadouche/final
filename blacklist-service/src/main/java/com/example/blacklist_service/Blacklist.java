package com.example.blacklist_service;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@Getter
public class Blacklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String denomination;

    private String activite;
    private String structureDemandeExclusion;
    private LocalDate dateExclusion;
    private String motifs;
    private Integer dureeExclusion; // durée en jours

    // Getters et setters
}
