package com.example.flahasmarty;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // Insert a new order
    public void insertOrder(Order order) {
        String sql = "INSERT INTO commandes (reference, date_commande, statut, mode_paiement, " +
                "adresse_livraison, montant_total, frais_livraison, id_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getReference());
            ps.setDate(2, Date.valueOf(order.getDateCommande()));
            ps.setString(3, order.getStatut());
            ps.setString(4, order.getModePaiement());
            ps.setString(5, order.getAdresseLivraison());
            ps.setDouble(6, order.getMontantTotal());
            ps.setDouble(7, order.getFraisLivraison());
            ps.setInt(8, order.getIdUser());

            int result = ps.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Commande ajoutée avec succès");
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur insertion commande");
            e.printStackTrace();
        }
    }

    // Update an existing order
    public void updateOrder(Order order) {
        String sql = "UPDATE commandes SET reference=?, date_commande=?, statut=?, mode_paiement=?, " +
                "adresse_livraison=?, montant_total=?, frais_livraison=?, id_user=? WHERE id_commande=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getReference());
            ps.setDate(2, Date.valueOf(order.getDateCommande()));
            ps.setString(3, order.getStatut());
            ps.setString(4, order.getModePaiement());
            ps.setString(5, order.getAdresseLivraison());
            ps.setDouble(6, order.getMontantTotal());
            ps.setDouble(7, order.getFraisLivraison());
            ps.setInt(8, order.getIdUser());
            ps.setInt(9, order.getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Commande mise à jour avec succès. ID: " + order.getId());
            } else {
                System.out.println("⚠ Aucune commande trouvée avec ID: " + order.getId());
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur mise à jour commande");
            e.printStackTrace();
        }
    }

    // Delete an order by ID
    public void deleteOrder(int id) {
        String sql = "DELETE FROM commandes WHERE id_commande=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Commande supprimée avec succès. ID: " + id);
            } else {
                System.out.println("⚠ Aucune commande trouvée avec ID: " + id);
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur suppression commande");
            e.printStackTrace();
        }
    }

    // Get all orders
    public List<Order> getAllOrders() {
        String sql = "SELECT id_commande, reference, date_commande, statut, mode_paiement, " +
                "adresse_livraison, montant_total, frais_livraison, id_user " +
                "FROM commandes ORDER BY id_commande DESC";
        List<Order> orders = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getString("reference"),
                        rs.getDate("date_commande").toLocalDate(),
                        rs.getString("statut"),
                        rs.getString("mode_paiement"),
                        rs.getString("adresse_livraison"),
                        rs.getDouble("montant_total"),
                        rs.getDouble("frais_livraison"),
                        rs.getInt("id_user")
                );
                order.setId(rs.getInt("id_commande"));
                orders.add(order);
            }

            System.out.println("✅ " + orders.size() + " commandes récupérées");

        } catch (Exception e) {
            System.out.println("❌ Erreur récupération commandes");
            e.printStackTrace();
        }

        return orders;
    }

    // NEW: Search orders by reference
    public List<Order> searchOrdersByReference(String searchTerm) {
        String sql = "SELECT id_commande, reference, date_commande, statut, mode_paiement, " +
                "adresse_livraison, montant_total, frais_livraison, id_user " +
                "FROM commandes WHERE LOWER(reference) LIKE LOWER(?) ORDER BY id_commande DESC";
        List<Order> orders = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + searchTerm + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getString("reference"),
                        rs.getDate("date_commande").toLocalDate(),
                        rs.getString("statut"),
                        rs.getString("mode_paiement"),
                        rs.getString("adresse_livraison"),
                        rs.getDouble("montant_total"),
                        rs.getDouble("frais_livraison"),
                        rs.getInt("id_user")
                );
                order.setId(rs.getInt("id_commande"));
                orders.add(order);
            }

            rs.close();
            System.out.println("✅ " + orders.size() + " commandes trouvées pour: " + searchTerm);

        } catch (Exception e) {
            System.out.println("❌ Erreur recherche commandes");
            e.printStackTrace();
        }

        return orders;
    }

    // Get order by ID
    public Order getOrderById(int id) {
        String sql = "SELECT id_commande, reference, date_commande, statut, mode_paiement, " +
                "adresse_livraison, montant_total, frais_livraison, id_user " +
                "FROM commandes WHERE id_commande = ?";
        Order order = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                order = new Order(
                        rs.getString("reference"),
                        rs.getDate("date_commande").toLocalDate(),
                        rs.getString("statut"),
                        rs.getString("mode_paiement"),
                        rs.getString("adresse_livraison"),
                        rs.getDouble("montant_total"),
                        rs.getDouble("frais_livraison"),
                        rs.getInt("id_user")
                );
                order.setId(rs.getInt("id_commande"));
                System.out.println("✅ Commande trouvée: " + order.getReference());
            }

            rs.close();

        } catch (Exception e) {
            System.out.println("❌ Erreur recherche commande par ID");
            e.printStackTrace();
        }

        return order;
    }
}