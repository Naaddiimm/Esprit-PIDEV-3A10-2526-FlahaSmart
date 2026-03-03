package services;

import entities.Equipement;
import utilies.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EquipementService implements Iservice<Equipement> {

    private Connection connection;
    public EquipementService() {
        connection = MyDataBase.getInstance().getConnection();
    }
    @Override
    public void ajouter(Equipement equipement) throws SQLException {
        String sql = "INSERT INTO equipement (nom, type, etat) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, equipement.getNom());
        ps.setString(2, equipement.getType());
        ps.setString(3, "libre");
        ps.executeUpdate();
        System.out.println("Equipement ajouté !");
    }

    @Override
    public void modifier(Equipement equipement) throws SQLException {
        String sql = "UPDATE equipement SET nom = ?,type = ?, etat = ? WHERE id_equipement = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, equipement.getNom());
        ps.setString(2, equipement.getType());
        ps.setString(3, equipement.getEtat());
        ps.setInt(4, equipement.getId_equipement());
        int r = ps.executeUpdate();
        if (r > 0) {
            System.out.println("Equipement modifié avec succès !");
        } else {
            System.out.println("Aucun équipement trouvé avec l'ID: " + equipement.getId_equipement());
        }
    }

    @Override
    public void supprimer(Equipement equipement) throws SQLException {
        String sql = "DELETE FROM equipement WHERE id_equipement = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, equipement.getId_equipement());
        int r= ps.executeUpdate();
        if (r > 0) {
            System.out.println("Equipement supprimé avec succès !");
        } else {
            System.out.println("Aucun équipement trouvé avec l'ID: " + equipement.getId_equipement());
        }
    }

    @Override
    public List<Equipement> afficher() throws SQLException {
        String sql = "SELECT * FROM equipement";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        List<Equipement> equipements = new ArrayList<>();
        while (rs.next()) {
            Equipement e = new Equipement(
                rs.getInt("id_equipement"),
                rs.getString("type"),
                rs.getString("nom"),
                rs.getString("etat")
            );
            equipements.add(e);
        }
        if (equipements.isEmpty()) {
            System.out.println("Aucun équipement trouvé dans la base de données.");
        }
        return equipements;
    }

    public void changerEtat(int idEquipement, String etat) throws SQLException {

        String sql = "UPDATE equipement SET etat = ? WHERE id_equipement = ?";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, etat);
        ps.setInt(2, idEquipement);

        ps.executeUpdate();
    }
    public List<Equipement> afficherLibres() throws SQLException {

        List<Equipement> list = new ArrayList<>();

        String sql = "SELECT * FROM equipement WHERE etat = 'libre'";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Equipement e = new Equipement();
            e.setId_equipement(rs.getInt("id_equipement"));
            e.setNom(rs.getString("nom"));
            e.setType(rs.getString("type"));
            e.setEtat(rs.getString("etat"));

            list.add(e);
        }

        return list;
    }


    public List<Equipement> rechercherParNom(String nom) throws SQLException {
        return afficher().stream()
                .filter(e -> e.getType().toLowerCase().contains(nom.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Equipement> trierParNom() throws SQLException {
        return afficher().stream()
                .sorted(Comparator.comparing(Equipement::getType, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

}
