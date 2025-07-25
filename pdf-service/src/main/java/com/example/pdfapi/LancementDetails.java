package com.example.pdfapi;

public class LancementDetails {

    private Double montantEstime;
    private Double budgetEstime;
    private Integer delaiRealisation;
    private String typologidemarche;
    private String garantie;

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

    public Integer getDelaiRealisation() {
        return delaiRealisation;
    }

    public void setDelaiRealisation(Integer delaiRealisation) {
        this.delaiRealisation = delaiRealisation;
    }

    public String getTypologidemarche() {
        return typologidemarche;
    }

    public void setTypologidemarche(String typologidemarche) {
        this.typologidemarche = typologidemarche;
    }

    public String getGarantie() {
        return garantie;
    }

    public void setGarantie(String garantie) {
        this.garantie = garantie;
    }
}
