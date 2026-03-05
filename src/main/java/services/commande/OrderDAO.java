
package services.commande;

import entities.commande.Article;
import entities.commande.Order;
import tools.myConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public void insertOrder(Order order) {
        String sql = "INSERT INTO commandes (reference, date_commande, statut, mode_paiement, " +
                "adresse_livraison, montant_total, frais_livraison, id_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = myConnection.getInstance().getCnx();
            if (conn == null) return;

            ps = conn.prepareStatement(sql);
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
                System.out.println("[OrderDAO] ✅ Commande ajoutée avec succès");
            }
        } catch (SQLException e) {
            System.out.println("[OrderDAO] ❌ Erreur insertion commande");
            e.printStackTrace();
        } finally {
            closeResources(ps, conn);
        }
    }

    public void updateOrder(Order order) {
        String sql = "UPDATE commandes SET reference=?, date_commande=?, statut=?, mode_paiement=?, " +
                "adresse_livraison=?, montant_total=?, frais_livraison=?, id_user=? WHERE id_commande=?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = myConnection.getInstance().getCnx();
            if (conn == null) return;

            ps = conn.prepareStatement(sql);
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
                System.out.println("[OrderDAO] ✅ Commande mise à jour avec succès. ID: " + order.getId());
            } else {
                System.out.println("[OrderDAO] ⚠ Aucune commande trouvée avec ID: " + order.getId());
            }
        } catch (SQLException e) {
            System.out.println("[OrderDAO] ❌ Erreur mise à jour commande");
            e.printStackTrace();
        } finally {
            closeResources(ps, conn);
        }
    }

    public void deleteOrder(int id) {
        String sql = "DELETE FROM commandes WHERE id_commande=?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = myConnection.getInstance().getCnx();
            if (conn == null) return;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("[OrderDAO] ✅ Commande supprimée avec succès. ID: " + id);
            } else {
                System.out.println("[OrderDAO] ⚠ Aucune commande trouvée avec ID: " + id);
            }
        } catch (SQLException e) {
            System.out.println("[OrderDAO] ❌ Erreur suppression commande");
            e.printStackTrace();
        } finally {
            closeResources(ps, conn);
        }
    }

    public List<Order> getAllOrders() {
        String sql = "SELECT id_commande, reference, date_commande, statut, mode_paiement, " +
                "adresse_livraison, montant_total, frais_livraison, id_user " +
                "FROM commandes ORDER BY id_commande DESC";
        List<Order> orders = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = myConnection.getInstance().getCnx();
            if (conn == null) return orders;

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

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
            System.out.println("[OrderDAO] ✅ " + orders.size() + " commandes récupérées");
        } catch (SQLException e) {
            System.out.println("[OrderDAO] ❌ Erreur récupération commandes");
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return orders;
    }

    public List<Order> searchOrdersByReference(String searchTerm) {
        String sql = "SELECT id_commande, reference, date_commande, statut, mode_paiement, " +
                "adresse_livraison, montant_total, frais_livraison, id_user " +
                "FROM commandes WHERE LOWER(reference) LIKE LOWER(?) ORDER BY id_commande DESC";
        List<Order> orders = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = myConnection.getInstance().getCnx();
            if (conn == null) return orders;

            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + searchTerm + "%");
            rs = ps.executeQuery();

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
            System.out.println("[OrderDAO] ✅ " + orders.size() + " commandes trouvées pour : " + searchTerm);
        } catch (SQLException e) {
            System.out.println("[OrderDAO] ❌ Erreur recherche commandes");
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return orders;
    }

    public Order getOrderById(int id) {
        String sql = "SELECT id_commande, reference, date_commande, statut, mode_paiement, " +
                "adresse_livraison, montant_total, frais_livraison, id_user " +
                "FROM commandes WHERE id_commande = ?";
        Order order = null;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = myConnection.getInstance().getCnx();
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

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
                System.out.println("[OrderDAO] ✅ Commande trouvée : " + order.getReference());
            } else {
                System.out.println("[OrderDAO] ⚠ Aucune commande trouvée avec ID: " + id);
            }
        } catch (SQLException e) {
            System.out.println("[OrderDAO] ❌ Erreur recherche commande par ID");
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return order;
    }

    private void closeResources(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                    // Ignorer
                }
            }
        }
    }
}