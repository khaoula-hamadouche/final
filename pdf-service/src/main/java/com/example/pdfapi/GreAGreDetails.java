package com.example.pdfapi;

public class GreAGreDetails {

    private Double montantEstime;
    private Double budgetEstime;
    private Integer dureeContrat;  // Durée du contrat en mois/années
    private Integer delaiRealisation;  // Durée de la réalisation en mois/années

    // Getters et Setters

    public Double getMontantEstime() {
        return montantEstime;
    }

    public void setMontantEstime(Double montantEstime) {
        this.montantEstime = montantEstime;
    }

    public Double getBudgetEstime() {
        return budgetEstime;
    }

    public void setBudgetEstime(Double budgetEstime) {
        this.budgetEstime = budgetEstime;
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
}
