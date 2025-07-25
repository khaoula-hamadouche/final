package com.example.pdfapi;

import java.time.LocalDate;
import java.util.List;

public class DossierDto {

    private String numeroDossier;
    private String intitule;
    private LocalDate dateSoumission;
    private String typePassation; // nom variable corrigé en camelCase

    private List<String> fileNames;

    private LancementDetails lancementDetails;
    private AttributionDetails attributionDetails;
    private AvenantDetails avenantDetails;
    private GreAGreDetails greAGreDetails;
    private List<DecisionDto> decisions;

    // Getters & setters

    public String getNumeroDossier() {
        return numeroDossier;
    }

    public void setNumeroDossier(String numeroDossier) {
        this.numeroDossier = numeroDossier;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public LocalDate getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(LocalDate dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    public String getTypePassation() {
        return typePassation;
    }

    public void setTypePassation(String typePassation) {
        this.typePassation = typePassation;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public LancementDetails getLancementDetails() {
        return lancementDetails;
    }

    public void setLancementDetails(LancementDetails lancementDetails) {
        this.lancementDetails = lancementDetails;
    }

    public AttributionDetails getAttributionDetails() {
        return attributionDetails;
    }

    public void setAttributionDetails(AttributionDetails attributionDetails) {
        this.attributionDetails = attributionDetails;
    }

    public AvenantDetails getAvenantDetails() {
        return avenantDetails;
    }

    public void setAvenantDetails(AvenantDetails avenantDetails) {
        this.avenantDetails = avenantDetails;
    }

    public GreAGreDetails getGreAGreDetails() {
        return greAGreDetails;
    }

    public void setGreAGreDetails(GreAGreDetails greAGreDetails) {
        this.greAGreDetails = greAGreDetails;
    }
    public List<DecisionDto> getDecisions() {
        return decisions;
    }

    public void setDecisions(List<DecisionDto> decisions) {
        this.decisions = decisions;
    }
}
