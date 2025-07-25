package com.example.pdfapi;

public class AvenantDetails {

    private String numeroContrat;
    private String dateSignatureContrat;
    private Integer dureeContrat;  // Durée du contrat initial en mois/années
    private String dateExpirationContrat;
    private Double montantContrat;  // Montant du contrat initial
    private String objetAvenant;  // Objet de l'avenant
    private Double montantAvenant;  // Montant ajouté par l'avenant
    private Integer dureeAvenant;  // Durée de l'avenant en mois/années

    private Double nouveauMontantContrat;
    private Integer nouvelleDureeContrat;

    // Getters et Setters

    public String getNumeroContrat() {
        return numeroContrat;
    }

    public void setNumeroContrat(String numeroContrat) {
        this.numeroContrat = numeroContrat;
    }

    public String getDateSignatureContrat() {
        return dateSignatureContrat;
    }

    public void setDateSignatureContrat(String dateSignatureContrat) {
        this.dateSignatureContrat = dateSignatureContrat;
    }

    public Integer getDureeContrat() {
        return dureeContrat;
    }

    public void setDureeContrat(Integer dureeContrat) {
        this.dureeContrat = dureeContrat;
    }

    public String getDateExpirationContrat() {
        return dateExpirationContrat;
    }

    public void setDateExpirationContrat(String dateExpirationContrat) {
        this.dateExpirationContrat = dateExpirationContrat;
    }

    public Double getMontantContrat() {
        return montantContrat;
    }

    public void setMontantContrat(Double montantContrat) {
        this.montantContrat = montantContrat;
    }

    public String getObjetAvenant() {
        return objetAvenant;
    }

    public void setObjetAvenant(String objetAvenant) {
        this.objetAvenant = objetAvenant;
    }

    public Double getMontantAvenant() {
        return montantAvenant;
    }

    public void setMontantAvenant(Double montantAvenant) {
        this.montantAvenant = montantAvenant;
    }

    public Integer getDureeAvenant() {
        return dureeAvenant;
    }

    public void setDureeAvenant(Integer dureeAvenant) {
        this.dureeAvenant = dureeAvenant;
    }

    public Double getNouveauMontantContrat() {
        return nouveauMontantContrat;
    }

    public void setNouveauMontantContrat(Double nouveauMontantContrat) {
        this.nouveauMontantContrat = nouveauMontantContrat;
    }

    public Integer getNouvelleDureeContrat() {
        return nouvelleDureeContrat;
    }

    public void setNouvelleDureeContrat(Integer nouvelleDureeContrat) {
        this.nouvelleDureeContrat = nouvelleDureeContrat;
    }
}
