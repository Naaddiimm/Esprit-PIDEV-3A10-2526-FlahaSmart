package entities;

import java.sql.Timestamp;

public class User {
    private int id_user;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String telephone;
    private String adresse;
    private String ville;
    private String photo_profil;
    private Role role;              // Une seule déclaration
    private Boolean actif;
    private Timestamp date_creation;

    // Constructeur par défaut
    public User() {
    }

    // Constructeur avec tous les champs sauf id_user et date_creation
    public User(String nom, String prenom, String email, String password,
                String telephone, String adresse, String ville,
                String photo_profil, Role role, Boolean actif) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
        this.adresse = adresse;
        this.ville = ville;
        this.photo_profil = photo_profil;
        this.role = role;
        this.actif = actif;
    }

    // Constructeur avec tous les champs
    public User(int id_user, String nom, String prenom, String email, String password,
                String telephone, String adresse, String ville, String photo_profil,
                Role role, Boolean actif, Timestamp date_creation) {
        this.id_user = id_user;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
        this.adresse = adresse;
        this.ville = ville;
        this.photo_profil = photo_profil;
        this.role = role;
        this.actif = actif;
        this.date_creation = date_creation;
    }

    // Getters et Setters
    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPhoto_profil() {
        return photo_profil;
    }

    public void setPhoto_profil(String photo_profil) {
        this.photo_profil = photo_profil;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public Timestamp getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(Timestamp date_creation) {
        this.date_creation = date_creation;
    }

    @Override
    public String toString() {
        return "User{" +
                "id_user=" + id_user +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", adresse='" + adresse + '\'' +
                ", ville='" + ville + '\'' +
                ", photo_profil='" + photo_profil + '\'' +
                ", role=" + role +
                ", actif=" + actif +
                ", date_creation=" + date_creation +
                '}';
    }
}