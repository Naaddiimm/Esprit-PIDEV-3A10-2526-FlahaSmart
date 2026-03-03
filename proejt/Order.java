package com.example.flahasmarty;

import java.time.LocalDate;

public class Order {
    private int id;
    private String reference;
    private LocalDate dateCommande;
    private String statut;
    private String modePaiement;
    private String adresseLivraison;
    private double montantTotal;
    private double fraisLivraison;
    private int idUser;

    // Constructors
    public Order() {}

    public Order(String reference, LocalDate dateCommande, String statut,
                 String modePaiement, String adresseLivraison,
                 double montantTotal, double fraisLivraison, int idUser) {
        this.reference = reference;
        this.dateCommande = dateCommande;
        this.statut = statut;
        this.modePaiement = modePaiement;
        this.adresseLivraison = adresseLivraison;
        this.montantTotal = montantTotal;
        this.fraisLivraison = fraisLivraison;
        this.idUser = idUser;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public LocalDate getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDate dateCommande) { this.dateCommande = dateCommande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }

    public String getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(String adresseLivraison) { this.adresseLivraison = adresseLivraison; }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }

    public double getFraisLivraison() { return fraisLivraison; }
    public void setFraisLivraison(double fraisLivraison) { this.fraisLivraison = fraisLivraison; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
}