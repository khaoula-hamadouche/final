package com.example.blacklist_service;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Setter
@Getter
public class BlacklistDTO {
    private String denomination;
    private String activite;
    private String structureDemandeExclusion;
    private LocalDate dateExclusion;
    private String motifs;
    private Integer dureeExclusion;

    // Getters et setters
}
