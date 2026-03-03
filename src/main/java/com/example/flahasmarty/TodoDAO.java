package com.example.flahasmarty;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TodoDAO {

    // Insert a new todo task
    public void insertTodo(Todo todo) {
        String sql = "INSERT INTO todo (NomTache, Tache, Statut) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, todo.getNomTache());
            ps.setString(2, todo.getTache());
            ps.setString(3, todo.getStatut());

            int result = ps.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Tâche ajoutée avec succès");
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur insertion tâche");
            e.printStackTrace();
        }
    }

    // Update an existing todo (by matching NomTache and Tache combination)
    public void updateTodo(Todo oldTodo, Todo newTodo) {
        String sql = "UPDATE todo SET NomTache=?, Tache=?, Statut=? WHERE NomTache=? AND Tache=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newTodo.getNomTache());
            ps.setString(2, newTodo.getTache());
            ps.setString(3, newTodo.getStatut());
            ps.setString(4, oldTodo.getNomTache());
            ps.setString(5, oldTodo.getTache());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Tâche mise à jour avec succès");
            } else {
                System.out.println("⚠ Aucune tâche trouvée avec ces critères");
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur mise à jour tâche");
            e.printStackTrace();
        }
    }

    // Delete a todo by NomTache and Tache
    public void deleteTodo(String nomTache, String tache) {
        String sql = "DELETE FROM todo WHERE NomTache=? AND Tache=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nomTache);
            ps.setString(2, tache);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Tâche supprimée avec succès");
            } else {
                System.out.println("⚠ Aucune tâche trouvée avec ces critères");
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur suppression tâche");
            e.printStackTrace();
        }
    }

    // Get all todos
    public List<Todo> getAllTodos() {
        String sql = "SELECT NomTache, Tache, Statut FROM todo";
        List<Todo> todos = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Todo todo = new Todo(
                        rs.getString("NomTache"),
                        rs.getString("Tache"),
                        rs.getString("Statut")
                );
                todos.add(todo);
            }

            System.out.println("✅ " + todos.size() + " tâches récupérées");

        } catch (Exception e) {
            System.out.println("❌ Erreur récupération tâches");
            e.printStackTrace();
        }

        return todos;
    }

    // Find a specific todo by NomTache and Tache
    public Todo findTodo(String nomTache, String tache) {
        String sql = "SELECT NomTache, Tache, Statut FROM todo WHERE NomTache=? AND Tache=?";
        Todo todo = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nomTache);
            ps.setString(2, tache);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                todo = new Todo(
                        rs.getString("NomTache"),
                        rs.getString("Tache"),
                        rs.getString("Statut")
                );
                System.out.println("✅ Tâche trouvée: " + todo.getTache());
            }

            rs.close();

        } catch (Exception e) {
            System.out.println("❌ Erreur recherche tâche");
            e.printStackTrace();
        }

        return todo;
    }

    // Search todos by consultant name or task description
    public List<Todo> searchTodos(String searchTerm) {
        String sql = "SELECT NomTache, Tache, Statut FROM todo WHERE LOWER(NomTache) LIKE LOWER(?) OR LOWER(Tache) LIKE LOWER(?)";
        List<Todo> todos = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + searchTerm + "%");
            ps.setString(2, "%" + searchTerm + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Todo todo = new Todo(
                        rs.getString("NomTache"),
                        rs.getString("Tache"),
                        rs.getString("Statut")
                );
                todos.add(todo);
            }

            rs.close();
            System.out.println("✅ " + todos.size() + " tâches trouvées pour: " + searchTerm);

        } catch (Exception e) {
            System.out.println("❌ Erreur recherche tâches");
            e.printStackTrace();
        }

        return todos;
    }
}