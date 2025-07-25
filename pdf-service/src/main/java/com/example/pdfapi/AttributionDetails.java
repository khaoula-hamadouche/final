package com.example.pdfapi;
public class AttributionDetails {

    private String nomFournisseur;
    private Double montantContrat;
    private Integer dureeContrat;
    private Integer delaiRealisation;
    private String typologidemarche;
    private String garantie;

    private Integer experiencefournisseur;
    private Integer nombredeprojetssimilaires;
    private Integer notationinterne;
    private Integer chiffreaffaire;
    private String situationfiscale;
    private String fournisseurblacklist;
    private String typefournisseur;
    private boolean fournisseurEtrangerInstallationPermanente;
    private boolean originePaysNonDoubleImposition;

    // Getters and Setters

    public String getNomFournisseur() {
        return nomFournisseur;
    }

    public void setNomFournisseur(String nomFournisseur) {
        this.nomFournisseur = nomFournisseur;
    }

    public Double getMontantContrat() {
        return montantContrat;
    }

    public void setMontantContrat(Double montantContrat) {
        this.montantContrat = montantContrat;
    }

    public Integer getDureeContrat() {
        return dureeContrat;
    }

    public void setDureeContrat(Integer dureeContrat) {
        this.dureeContrat = dureeContrat;
    }

    public Integer getDelaiRealisation() {
        return delaiRealisation;
    }

    public void setDelaiRealisation(Integer delaiRealisation) {
        this.delaiRealisation = delaiRealisation;
    }

    public String gettypologidemarche() {
        return typologidemarche;
    }

    public void settypologidemarche(String typologidemarche) {
        this.typologidemarche = typologidemarche;
    }

    public String getGarantie() {
        return garantie;
    }

    public void setGarantie(String garantie) {
        this.garantie = garantie;
    }

    public Integer getExperiencefournisseur() {
        return experiencefournisseur;
    }

    public void setExperiencefournisseur(Integer experiencefournisseur) {
        this.experiencefournisseur = experiencefournisseur;
    }

    public Integer getNombredeprojetssimilaires() {
        return nombredeprojetssimilaires;
    }

    public void setNombredeprojetssimilaires(Integer nombredeprojetssimilaires) {
        this.nombredeprojetssimilaires = nombredeprojetssimilaires;
    }

    public Integer getNotationinterne() {
        return notationinterne;
    }

    public void setNotationinterne(Integer notationinterne) {
        this.notationinterne = notationinterne;
    }

    public Integer getChiffreaffaire() {
        return chiffreaffaire;
    }

    public void setChiffreaffaire(Integer chiffreaffaire) {
        this.chiffreaffaire = chiffreaffaire;
    }

    public String getSituationfiscale() {
        return situationfiscale;
    }

    public void setSituationfiscale(String situationfiscale) {
        this.situationfiscale = situationfiscale;
    }

    public String getFournisseurblacklist() {
        return fournisseurblacklist;
    }

    public void setFournisseurblacklist(String fournisseurblacklist) {
        this.fournisseurblacklist = fournisseurblacklist;
    }

    public String getTypefournisseur() {
        return typefournisseur;
    }

    public void setTypefournisseur(String typefournisseur) {
        this.typefournisseur = typefournisseur;
    }

    public boolean isFournisseurEtrangerInstallationPermanente() {
        return fournisseurEtrangerInstallationPermanente;
    }

    public void setFournisseurEtrangerInstallationPermanente(boolean fournisseurEtrangerInstallationPermanente) {
        this.fournisseurEtrangerInstallationPermanente = fournisseurEtrangerInstallationPermanente;
    }

    public boolean isOriginePaysNonDoubleImposition() {
        return originePaysNonDoubleImposition;
    }

    public void setOriginePaysNonDoubleImposition(boolean originePaysNonDoubleImposition) {
        this.originePaysNonDoubleImposition = originePaysNonDoubleImposition;
    }
}
