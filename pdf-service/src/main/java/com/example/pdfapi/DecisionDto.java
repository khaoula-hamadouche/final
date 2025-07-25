package com.example.pdfapi;


import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty; // Import if your backend uses a different field name for the ID

public class DecisionDto {
    private Long id;
    private String decision;
    private LocalDateTime dateAjout;

    // This field will capture the ID directly from the /api/decisions response
    // If your backend JSON for decision has "chargeDossierId", this will map automatically.
    // If it's something else like "userId", you'd use @JsonProperty("userId")
    private Long chargeDossierId;

    // This field will hold the full UserDto object after we fetch it
    private UserDto chargeDossier;
    private String compteRendu;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }

    // Getter and Setter for the initial ID
    public Long getChargeDossierId() {
        return chargeDossierId;
    }

    public void setChargeDossierId(Long chargeDossierId) {
        this.chargeDossierId = chargeDossierId;
    }

    // Getter and Setter for the full UserDto
    public UserDto getChargeDossier() {
        return chargeDossier;
    }

    public void setChargeDossier(UserDto chargeDossier) {
        this.chargeDossier = chargeDossier;
    }

    public String getCompteRendu() {
        return compteRendu;
    }

    public void setCompteRendu(String compteRendu) {
        this.compteRendu = compteRendu;
    }
}