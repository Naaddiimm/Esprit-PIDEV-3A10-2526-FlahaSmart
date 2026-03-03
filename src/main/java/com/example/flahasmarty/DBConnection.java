package com.example.flahasmarty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/flahasmart?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Static block to load MySQL driver once
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[v0] MySQL Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.out.println("[v0] ERROR: MySQL Driver not found!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[v0] ✅ Database Connected");
            return conn;
        } catch (Exception e) {
            System.out.println("[v0] ❌ Database NOT Connected - Error: " + e.getMessage());
            System.out.println("[v0] Check if MySQL is running on localhost:3306");
            System.out.println("[v0] Check if database 'flahasmart' exists");
            e.printStackTrace();
            return null;
        }
    }

    public static void insertArticle(
            String nom,
            String description,
            String categorie,
            double prix,
            int stock,
            double poids,
            String unite,
            String imageUrl,
            int idUser) {

        String sql = "INSERT INTO articles " +
                "(nom, description, categorie, prix, stock, poids, unite, image_url, id_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();

            if (conn == null) {
                System.out.println("[v0] ❌ FATAL: Connection is null - cannot proceed with insert");
                return;
            }

            conn.setAutoCommit(true);  // Enable auto-commit

            ps = conn.prepareStatement(sql);

            ps.setString(1, nom);
            ps.setString(2, description);
            ps.setString(3, categorie);
            ps.setDouble(4, prix);
            ps.setInt(5, stock);
            ps.setDouble(6, poids);
            ps.setString(7, unite);
            ps.setString(8, imageUrl);
            ps.setInt(9, idUser);

            System.out.println("[v0] Executing INSERT: " + nom);
            int result = ps.executeUpdate();

            if (result > 0) {
                System.out.println("[v0] ✅ Article ajouté avec succès - Ligne insérée: " + result);
            } else {
                System.out.println("[v0] ❌ Insert failed - no rows affected");
            }

        } catch (Exception e) {
            System.out.println("[v0] ❌ Erreur insertion article: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                System.out.println("[v0] Error closing PreparedStatement: " + e.getMessage());
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                System.out.println("[v0] Error closing Connection: " + e.getMessage());
            }
        }
    }

    public static List<Article> getAllArticles() {
        String sql = "SELECT id_article, nom, description, categorie, prix, stock, poids, unite, image_url, id_user, date_ajout FROM articles ORDER BY id_article DESC";
        List<Article> articles = new ArrayList<>();

        Connection conn = null;
        try {
            conn = getConnection();

            if (conn == null) {
                System.out.println("[v0] ❌ FATAL: Connection is null - cannot retrieve articles");
                return articles;
            }

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

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

                // Set date_ajout field
                java.sql.Timestamp timestamp = rs.getTimestamp("date_ajout");
                if (timestamp != null) {
                    article.setDateAjout(timestamp.toLocalDateTime());
                }

                articles.add(article);
            }

            rs.close();
            ps.close();
            System.out.println("[v0] ✅ Retrieved " + articles.size() + " articles from database");

        } catch (Exception e) {
            System.out.println("[v0] ❌ Erreur récupération articles: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                System.out.println("[v0] Error closing Connection: " + e.getMessage());
            }
        }

        return articles;
    }

    // NEW: Search articles by name method
    public static List<Article> searchArticlesByName(String searchTerm) {
        String sql = "SELECT id_article, nom, description, categorie, prix, stock, poids, unite, image_url, id_user, date_ajout " +
                "FROM articles WHERE LOWER(nom) LIKE LOWER(?) ORDER BY id_article DESC";
        List<Article> articles = new ArrayList<>();

        Connection conn = null;
        try {
            conn = getConnection();

            if (conn == null) {
                System.out.println("[v0] ❌ FATAL: Connection is null - cannot search articles");
                return articles;
            }

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + searchTerm + "%");
            ResultSet rs = ps.executeQuery();

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

                // Set date_ajout field
                java.sql.Timestamp timestamp = rs.getTimestamp("date_ajout");
                if (timestamp != null) {
                    article.setDateAjout(timestamp.toLocalDateTime());
                }

                articles.add(article);
            }

            rs.close();
            ps.close();
            System.out.println("[v0] ✅ Found " + articles.size() + " articles matching: " + searchTerm);

        } catch (Exception e) {
            System.out.println("[v0] ❌ Erreur recherche articles: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                System.out.println("[v0] Error closing Connection: " + e.getMessage());
            }
        }

        return articles;
    }
}