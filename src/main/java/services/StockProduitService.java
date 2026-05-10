package services;



import entities.StockProduit;
import utilies.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockProduitService {
    private final Connection cnx = MyDataBase.getInstance().getConnection();

    public void ajouter(StockProduit s) throws SQLException {
        String sql = "INSERT INTO stock_produit (type_produit, variete, date_debut, date_fin_estimee, statut, id_user) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, s.getTypeProduit());
        ps.setString(2, s.getVariete());
        ps.setDate(3, Date.valueOf(s.getDateDebut()));
        if (s.getDateFinEstimee() != null)
            ps.setDate(4, Date.valueOf(s.getDateFinEstimee()));
        else
            ps.setNull(4, Types.DATE);
        ps.setString(5, s.getStatut());
        ps.setInt(6, s.getIdUser());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            s.setIdProduit(rs.getInt(1));
        }
    }

    public void updateQrCode(int id, String path) throws SQLException {
        String sql = "UPDATE stock_produit SET code_qr = ? WHERE id_produit = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, path);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    public List<StockProduit> afficher() throws SQLException {
        List<StockProduit> list = new ArrayList<>();
        String sql = "SELECT * FROM stock_produit ORDER BY id_produit DESC";
        ResultSet rs = cnx.createStatement().executeQuery(sql);
        while (rs.next()) {
            LocalDate fin = rs.getDate("date_fin_estimee") != null ? rs.getDate("date_fin_estimee").toLocalDate() : null;
            list.add(new StockProduit(
                    rs.getInt("id_produit"),
                    rs.getString("type_produit"),
                    rs.getString("variete"),
                    rs.getDate("date_debut").toLocalDate(),
                    fin,
                    rs.getString("statut"),
                    rs.getInt("id_user"),
                    rs.getString("code_qr")
            ));
        }
        return list;
    }

    public void supprimer(int id) throws SQLException {
        PreparedStatement ps = cnx.prepareStatement("DELETE FROM stock_produit WHERE id_produit = ?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public void modifier(StockProduit s) throws SQLException {
        String sql = "UPDATE stock_produit SET type_produit = ?, variete = ?, date_debut = ?, date_fin_estimee = ?, statut = ?, id_user = ? WHERE id_produit = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, s.getTypeProduit());
        ps.setString(2, s.getVariete());
        ps.setDate(3, Date.valueOf(s.getDateDebut()));
        if (s.getDateFinEstimee() != null)
            ps.setDate(4, Date.valueOf(s.getDateFinEstimee()));
        else
            ps.setNull(4, Types.DATE);
        ps.setString(5, s.getStatut());
        ps.setInt(6, s.getIdUser());
        ps.setInt(7, s.getIdProduit());
        ps.executeUpdate();
    }

    public StockProduit getById(int id) throws SQLException {
        String sql = "SELECT * FROM stock_produit WHERE id_produit = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            LocalDate fin = rs.getDate("date_fin_estimee") != null ? rs.getDate("date_fin_estimee").toLocalDate() : null;
            return new StockProduit(
                    rs.getInt("id_produit"),
                    rs.getString("type_produit"),
                    rs.getString("variete"),
                    rs.getDate("date_debut").toLocalDate(),
                    fin,
                    rs.getString("statut"),
                    rs.getInt("id_user"),
                    rs.getString("code_qr")
            );
        }
        return null;
    }
}