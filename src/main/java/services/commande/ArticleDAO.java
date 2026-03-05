package services.commande;

import entities.commande.Article;
import tools.myConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleDAO {

    public void insertArticle(Article article) {
        String sql = "INSERT INTO articles (nom, description, categorie, prix, stock, poids, unite, image_url, id_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = myConnection.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, article.getNom());
            ps.setString(2, article.getDescription());
            ps.setString(3, article.getCategorie());
            ps.setDouble(4, article.getPrix());
            ps.setInt(5, article.getStock());
            ps.setDouble(6, article.getPoids());
            ps.setString(7, article.getUnite());
            ps.setString(8, article.getImageUrl());
            ps.setInt(9, article.getIdUser());

            int result = ps.executeUpdate();
            if (result > 0) {
                System.out.println("[ArticleDAO] ✅ Article ajouté avec succès");
            }
        } catch (SQLException e) {
            System.out.println("[ArticleDAO] ❌ Erreur insertion article");
            e.printStackTrace();
        }
    }

    public void updateArticle(Article article) {
        String sql = "UPDATE articles SET nom=?, description=?, categorie=?, prix=?, stock=?, poids=?, unite=?, image_url=? WHERE id_article=?";

        try (Connection conn = myConnection.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, article.getNom());
            ps.setString(2, article.getDescription());
            ps.setString(3, article.getCategorie());
            ps.setDouble(4, article.getPrix());
            ps.setInt(5, article.getStock());
            ps.setDouble(6, article.getPoids());
            ps.setString(7, article.getUnite());
            ps.setString(8, article.getImageUrl());
            ps.setInt(9, article.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("[ArticleDAO] ✅ Article mis à jour. ID: " + article.getId());
            } else {
                System.out.println("[ArticleDAO] ⚠ Aucun article avec ID: " + article.getId());
            }
        } catch (SQLException e) {
            System.out.println("[ArticleDAO] ❌ Erreur mise à jour article");
            e.printStackTrace();
        }
    }

    public void deleteArticle(int id) {
        String sql = "DELETE FROM articles WHERE id_article=?";

        try (Connection conn = myConnection.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("[ArticleDAO] ✅ Article supprimé. ID: " + id);
            } else {
                System.out.println("[ArticleDAO] ⚠ Aucun article avec ID: " + id);
            }
        } catch (SQLException e) {
            System.out.println("[ArticleDAO] ❌ Erreur suppression article");
            e.printStackTrace();
        }
    }

    public List<Article> getAllArticles() {
        String sql = "SELECT id_article, nom, description, categorie, prix, stock, poids, unite, image_url, id_user, date_ajout FROM articles ORDER BY id_article DESC";
        List<Article> articles = new ArrayList<>();

        try (Connection conn = myConnection.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Article article = new Article(
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("categorie"),
                        rs.getDouble("prix"),
                        rs.getInt("stock"),
                        rs.getDouble("poids"),
                        rs.getString("unite"),
                        rs.getString("image_url"),
                        rs.getInt("id_user")
                );
                article.setId(rs.getInt("id_article"));
                Timestamp ts = rs.getTimestamp("date_ajout");
                if (ts != null) article.setDateAjout(ts.toLocalDateTime());
                articles.add(article);
            }
            System.out.println("[ArticleDAO] ✅ " + articles.size() + " articles récupérés");
        } catch (SQLException e) {
            System.out.println("[ArticleDAO] ❌ Erreur récupération articles");
            e.printStackTrace();
        }
        return articles;
    }

    public List<Article> searchArticlesByName(String searchTerm) {
        String sql = "SELECT id_article, nom, description, categorie, prix, stock, poids, unite, image_url, id_user, date_ajout " +
                "FROM articles WHERE LOWER(nom) LIKE LOWER(?) ORDER BY id_article DESC";
        List<Article> articles = new ArrayList<>();

        try (Connection conn = myConnection.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + searchTerm + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Article article = new Article(
                            rs.getString("nom"),
                            rs.getString("description"),
                            rs.getString("categorie"),
                            rs.getDouble("prix"),
                            rs.getInt("stock"),
                            rs.getDouble("poids"),
                            rs.getString("unite"),
                            rs.getString("image_url"),
                            rs.getInt("id_user")
                    );
                    article.setId(rs.getInt("id_article"));
                    Timestamp ts = rs.getTimestamp("date_ajout");
                    if (ts != null) article.setDateAjout(ts.toLocalDateTime());
                    articles.add(article);
                }
            }
            System.out.println("[ArticleDAO] ✅ " + articles.size() + " articles trouvés pour : " + searchTerm);
        } catch (SQLException e) {
            System.out.println("[ArticleDAO] ❌ Erreur recherche articles");
            e.printStackTrace();
        }
        return articles;
    }

    public Article getArticleById(int id) {
        String sql = "SELECT id_article, nom, description, categorie, prix, stock, poids, unite, image_url, id_user, date_ajout FROM articles WHERE id_article = ?";
        Article article = null;

        try (Connection conn = myConnection.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    article = new Article(
                            rs.getString("nom"),
                            rs.getString("description"),
                            rs.getString("categorie"),
                            rs.getDouble("prix"),
                            rs.getInt("stock"),
                            rs.getDouble("poids"),
                            rs.getString("unite"),
                            rs.getString("image_url"),
                            rs.getInt("id_user")
                    );
                    article.setId(rs.getInt("id_article"));
                    Timestamp ts = rs.getTimestamp("date_ajout");
                    if (ts != null) article.setDateAjout(ts.toLocalDateTime());
                    System.out.println("[ArticleDAO] ✅ Article trouvé : " + article.getNom());
                } else {
                    System.out.println("[ArticleDAO] ⚠ Aucun article avec ID: " + id);
                }
            }
        } catch (SQLException e) {
            System.out.println("[ArticleDAO] ❌ Erreur recherche article par ID");
            e.printStackTrace();
        }
        return article;
    }
}