package services;

import entities.User;
import entities.Role;
import interfaces.IService;
import tools.myConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private Connection connection;

    public UserService() {
        this.connection = myConnection.getInstance().getCnx();
    }

    @Override
    public void addEntity(User user) {
        String query = "INSERT INTO users (nom, prenom, email, password, telephone, adresse, ville, photo_profil, role, actif, date_creation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            // PLUS DE HACHAGE - mot de passe en clair
            String plainPassword = user.getPassword();  // ← Suppression de BCrypt

            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, plainPassword);  // ← Mot de passe en clair
            pst.setString(5, user.getTelephone());
            pst.setString(6, user.getAdresse());
            pst.setString(7, user.getVille());
            pst.setString(8, user.getPhoto_profil());
            pst.setString(9, user.getRole().name());
            pst.setBoolean(10, user.getActif() != null ? user.getActif() : true);
            pst.setTimestamp(11, new Timestamp(System.currentTimeMillis()));

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    user.setId_user(rs.getInt(1));
                }
                System.out.println("✅ Utilisateur ajouté avec succès ! ID: " + user.getId_user());
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deleteEntity(User user) {
        deleteEntity(user.getId_user());
    }

    public void deleteEntity(int id) {
        String query = "DELETE FROM users WHERE id_user = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Utilisateur supprimé avec succès ! ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
        }
    }

    @Override
    public void updateEntity(User user) {
        String query = "UPDATE users SET nom = ?, prenom = ?, email = ?, password = ?, " +
                "telephone = ?, adresse = ?, ville = ?, photo_profil = ?, role = ?, actif = ? " +
                "WHERE id_user = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {

            // Mot de passe en clair (sans hachage)
            String passwordToSet = user.getPassword();

            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, passwordToSet);  // ← Mot de passe en clair
            pst.setString(5, user.getTelephone());
            pst.setString(6, user.getAdresse());
            pst.setString(7, user.getVille());
            pst.setString(8, user.getPhoto_profil());
            pst.setString(9, user.getRole().name());
            pst.setBoolean(10, user.getActif() != null ? user.getActif() : true);
            pst.setInt(11, user.getId_user());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Utilisateur mis à jour avec succès ! ID: " + user.getId_user());
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<User> getEntities() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY id_user DESC";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            System.out.println("✅ " + users.size() + " utilisateurs récupérés");

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
        }
        return users;
    }

    public List<User> getAllUsers() {
        return getEntities();
    }

    public User getUserById(int id) {
        String query = "SELECT * FROM users WHERE id_user = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
        }
        return null;
    }

    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
        }
        return null;
    }

    public User authenticate(String email, String password) {
        User user = getUserByEmail(email);

        if (user == null) {
            System.out.println("❌ Utilisateur non trouvé: " + email);
            return null;
        }

        // COMPARAISON SIMPLE (mots de passe en clair)
        if (password.equals(user.getPassword())) {
            System.out.println("✅ Authentification réussie pour: " + email);
            return user;
        }

        System.out.println("❌ Échec d'authentification pour: " + email);
        return null;
    }

    public List<User> getRecentUsers(int limit) {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY date_creation DESC LIMIT ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, limit);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
            System.out.println("✅ " + users.size() + " utilisateurs récents récupérés");

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans getRecentUsers: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId_user(rs.getInt("id_user"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setTelephone(rs.getString("telephone"));
        user.setAdresse(rs.getString("adresse"));
        user.setVille(rs.getString("ville"));
        user.setPhoto_profil(rs.getString("photo_profil"));
        user.setDate_creation(rs.getTimestamp("date_creation"));

        String roleStr = rs.getString("role");
        if (roleStr != null) {
            user.setRole(Role.valueOf(roleStr));
        }

        user.setActif(rs.getBoolean("actif"));
        if (rs.wasNull()) {
            user.setActif(null);
        }

        return user;
    }
}