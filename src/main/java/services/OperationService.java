package services;

import controllers.Auth.Session;
import entities.Operation;
import utilies.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OperationService implements Iservice<Operation> {

    private Connection connection;
    EquipementService es = new EquipementService();
    public OperationService() {
        connection = MyDataBase.getInstance().getConnection();
    }
    @Override
    public void ajouter(Operation operation) throws SQLException {
        // Récupérer l'ID de l'utilisateur connecté via Session
        int userId = Session.isLoggedIn() ? Session.getCurrentUser().getId_user() : -1;
        if (userId == -1) {
            throw new SQLException("Aucun utilisateur connecté. Impossible d'ajouter l'opération.");
        }

        String sql = "INSERT INTO operation (id_equipement, id_user, type_operation, date_debut, date_fin) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, operation.getId_equipement());
        ps.setInt(2, userId);
        ps.setString(3, operation.getType_operation());
        ps.setDate(4, operation.getDate_debut());
        ps.setDate(5, operation.getDate_fin());


        ps.executeUpdate();
        System.out.println("ID envoyé = " + operation.getId_equipement());

        es.changerEtat(operation.getId_equipement(), "réservé");

        System.out.println("Operation ajoutée !");
    }

    @Override
    public void modifier(Operation operation) throws SQLException {

        String sql = "UPDATE operation SET type_operation = ?, date_debut = ?, date_fin = ?, statut = ?, id_equipement = ?, id_user = ? WHERE id_operation = ?";

        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, operation.getType_operation());
        ps.setDate(2, operation.getDate_debut());
        ps.setDate(3, operation.getDate_fin());
        ps.setString(4, operation.getStatut());
        ps.setInt(5, operation.getId_equipement());
        ps.setInt(6, operation.getId_user());
        ps.setInt(7, operation.getId_operation());

        int r = ps.executeUpdate();

        if (r > 0) {
            System.out.println("Operation modifiée avec succès !");
        } else {
            System.out.println("Aucune opération trouvée avec l'ID: " + operation.getId_operation());
        }
    }

    public void modifier(Operation operation, int ancienEquipementId) throws SQLException {

        String sql = "UPDATE operation SET type_operation = ?, date_debut = ?, date_fin = ?, statut = ?, id_equipement = ?, id_user = ? WHERE id_operation = ?";

        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, operation.getType_operation());
        ps.setDate(2, operation.getDate_debut());
        ps.setDate(3, operation.getDate_fin());
        ps.setString(4, operation.getStatut());
        ps.setInt(5, operation.getId_equipement());
        ps.setInt(6, operation.getId_user());
        ps.setInt(7, operation.getId_operation());

        ps.executeUpdate();

        EquipementService es = new EquipementService();

        int nouveau = operation.getId_equipement();

        if (ancienEquipementId != nouveau) {
            es.changerEtat(ancienEquipementId, "libre");
            es.changerEtat(nouveau, "réservé");
        }

        System.out.println("Modification OK + états synchronisés");
    }


    @Override
    public void supprimer(Operation operation) throws SQLException {
        String sql = "DELETE FROM operation WHERE id_operation = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, operation.getId_operation());
        es.changerEtat(operation.getId_equipement(), "libre");
        int r = ps.executeUpdate();

        if (r > 0) {
            System.out.println("Operation supprimée avec succès !");
        } else {
            System.out.println("Aucune opération trouvée avec l'ID: " + operation.getId_operation());
        }
    }

    @Override
    public List<Operation> afficher() throws SQLException {
        List<Operation> operations = new ArrayList<>();

        // Récupérer l'ID de l'utilisateur connecté
        int userId = Session.isLoggedIn() ? Session.getCurrentUser().getId_user() : -1;
        if (userId == -1) {
            System.out.println("⚠️ Aucun utilisateur connecté. Liste vide.");
            return operations;
        }

        String sql = "SELECT o.*, e.nom AS nom_equipement " +
                "FROM operation o " +
                "JOIN equipement e ON o.id_equipement = e.id_equipement " +
                "WHERE o.id_user = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int id_operation = rs.getInt("id_operation");
            int idEquipement = rs.getInt("id_equipement");
            int idUser = rs.getInt("id_user");
            String type_operation = rs.getString("type_operation");
            String nomEquipement = rs.getString("nom_equipement");
            Date date_debut = Date.valueOf(rs.getString("date_debut"));
            Date date_fin = Date.valueOf(rs.getString("date_fin"));
            String statut = rs.getString("statut");

            Operation operation = new Operation(type_operation, date_debut, date_fin, statut);
            operation.setId_operation(id_operation);
            operation.setId_equipement(idEquipement);
            operation.setId_user(idUser);
            operation.setNomEquipement(nomEquipement);
            operations.add(operation);
        }

        return operations;
    }

    // Recherche
    public List<Operation> rechercherParType(String nom) throws SQLException {
        return afficher().stream()
                .filter(op -> op.getType_operation() != null && op.getType_operation()
                        .toLowerCase()
                        .contains(nom.toLowerCase()))
                        .toList();
    }

    // Tri par nom
    public List<Operation> trierParNom() throws SQLException {
        return afficher().stream()
                .sorted((op1, op2) -> op1.getType_operation().compareToIgnoreCase(op2.getType_operation()))
                .toList();
    }

    public void terminer(Operation operation) throws SQLException {
        String sql = "UPDATE operation SET statut = ? WHERE id_operation = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, "terminé");
        ps.setInt(2, operation.getId_operation());

        int r = ps.executeUpdate();
        if (r > 0) {
            es.changerEtat(operation.getId_equipement(), "libre");
            System.out.println("Operation terminée avec succès !");
        } else {
            System.out.println("Aucune opération trouvée avec l'ID: " + operation.getId_operation());
        }
    }
}
