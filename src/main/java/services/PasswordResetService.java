package services;

import tools.myConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Random;

public class PasswordResetService {




    /**
     * Réinitialise le mot de passe
     */


    /**
     * Vérifie si l'email existe dans la base
     */
    private static boolean emailExists(String email) {
        String query = "SELECT id_user FROM users WHERE email = ?";

        try (PreparedStatement pst = myConnection.getInstance().getCnx().prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Met à jour le mot de passe
     */


}