package services.commande;

import entities.commande.Todo;
import tools.myConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TodoDAO {

    public void insertTodo(Todo todo) {
        String sql = "INSERT INTO todo (NomTache, Tache, Statut) VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = myConnection.getInstance().getCnx();
            if (conn == null) return;

            ps = conn.prepareStatement(sql);
            ps.setString(1, todo.getNomTache());
            ps.setString(2, todo.getTache());
            ps.setString(3, todo.getStatut());

            int result = ps.executeUpdate();
            if (result > 0) {
                System.out.println("[TodoDAO] ✅ Tâche ajoutée avec succès");
            }
        } catch (SQLException e) {
            System.out.println("[TodoDAO] ❌ Erreur insertion tâche");
            e.printStackTrace();
        } finally {
            closeResources(ps, conn);
        }
    }

    public void updateTodo(Todo oldTodo, Todo newTodo) {
        String sql = "UPDATE todo SET NomTache=?, Tache=?, Statut=? WHERE NomTache=? AND Tache=?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = myConnection.getInstance().getCnx();
            if (conn == null) return;

            ps = conn.prepareStatement(sql);
            ps.setString(1, newTodo.getNomTache());
            ps.setString(2, newTodo.getTache());
            ps.setString(3, newTodo.getStatut());
            ps.setString(4, oldTodo.getNomTache());
            ps.setString(5, oldTodo.getTache());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("[TodoDAO] ✅ Tâche mise à jour avec succès");
            } else {
                System.out.println("[TodoDAO] ⚠ Aucune tâche trouvée avec ces critères");
            }
        } catch (SQLException e) {
            System.out.println("[TodoDAO] ❌ Erreur mise à jour tâche");
            e.printStackTrace();
        } finally {
            closeResources(ps, conn);
        }
    }

    public void deleteTodo(String nomTache, String tache) {
        String sql = "DELETE FROM todo WHERE NomTache=? AND Tache=?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = myConnection.getInstance().getCnx();
            if (conn == null) return;

            ps = conn.prepareStatement(sql);
            ps.setString(1, nomTache);
            ps.setString(2, tache);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("[TodoDAO] ✅ Tâche supprimée avec succès");
            } else {
                System.out.println("[TodoDAO] ⚠ Aucune tâche trouvée avec ces critères");
            }
        } catch (SQLException e) {
            System.out.println("[TodoDAO] ❌ Erreur suppression tâche");
            e.printStackTrace();
        } finally {
            closeResources(ps, conn);
        }
    }

    public List<Todo> getAllTodos() {
        String sql = "SELECT NomTache, Tache, Statut FROM todo";
        List<Todo> todos = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = myConnection.getInstance().getCnx();
            if (conn == null) return todos;

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Todo todo = new Todo(
                        rs.getString("NomTache"),
                        rs.getString("Tache"),
                        rs.getString("Statut")
                );
                todos.add(todo);
            }
            System.out.println("[TodoDAO] ✅ " + todos.size() + " tâches récupérées");
        } catch (SQLException e) {
            System.out.println("[TodoDAO] ❌ Erreur récupération tâches");
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return todos;
    }

    public Todo findTodo(String nomTache, String tache) {
        String sql = "SELECT NomTache, Tache, Statut FROM todo WHERE NomTache=? AND Tache=?";
        Todo todo = null;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = myConnection.getInstance().getCnx();
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            ps.setString(1, nomTache);
            ps.setString(2, tache);
            rs = ps.executeQuery();

            if (rs.next()) {
                todo = new Todo(
                        rs.getString("NomTache"),
                        rs.getString("Tache"),
                        rs.getString("Statut")
                );
                System.out.println("[TodoDAO] ✅ Tâche trouvée : " + todo.getTache());
            }
        } catch (SQLException e) {
            System.out.println("[TodoDAO] ❌ Erreur recherche tâche");
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return todo;
    }

    public List<Todo> searchTodos(String searchTerm) {
        String sql = "SELECT NomTache, Tache, Statut FROM todo WHERE LOWER(NomTache) LIKE LOWER(?) OR LOWER(Tache) LIKE LOWER(?)";
        List<Todo> todos = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = myConnection.getInstance().getCnx();
            if (conn == null) return todos;

            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + searchTerm + "%");
            ps.setString(2, "%" + searchTerm + "%");
            rs = ps.executeQuery();

            while (rs.next()) {
                Todo todo = new Todo(
                        rs.getString("NomTache"),
                        rs.getString("Tache"),
                        rs.getString("Statut")
                );
                todos.add(todo);
            }
            System.out.println("[TodoDAO] ✅ " + todos.size() + " tâches trouvées pour : " + searchTerm);
        } catch (SQLException e) {
            System.out.println("[TodoDAO] ❌ Erreur recherche tâches");
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return todos;
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