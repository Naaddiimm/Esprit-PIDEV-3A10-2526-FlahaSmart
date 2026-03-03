package com.example.flahasmarty;

import java.time.LocalDateTime;

public class Article {
    private int id;
    private String nom;
    private String description;
    private String categorie;
    private double prix;
    private int stock;
    private double poids;
    private String unite;
    private String imageUrl;
    private int idUser;
    private LocalDateTime dateAjout;

    public Article(String nom, String description, String categorie, double prix, int stock) {
        this.nom = nom;
        this.description = description;
        this.categorie = categorie;
        this.prix = prix;
        this.stock = stock;
    }

    public Article(String nom, String description, String categorie, double prix, int stock, double poids, String unite, String imageUrl, int idUser) {
        this.nom = nom;
        this.description = description;
        this.categorie = categorie;
        this.prix = prix;
        this.stock = stock;
        this.poids = poids;
        this.unite = unite;
        this.imageUrl = imageUrl;
        this.idUser = idUser;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public double getPoids() { return poids; }
    public void setPoids(double poids) { this.poids = poids; }

    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }
}