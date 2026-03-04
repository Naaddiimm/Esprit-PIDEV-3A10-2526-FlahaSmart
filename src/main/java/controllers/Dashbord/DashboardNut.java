package controllers.Dashbord;

import entities.User;

public class DashboardNut {

    private User loggedInUser;

    // Méthode pour définir l'utilisateur connecté
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        // Vous pouvez maintenant utiliser loggedInUser pour afficher des informations dans l'interface utilisateur
        System.out.println("Utilisateur connecté (DashboardAgriculteur): " + loggedInUser.getId_user());
    }
}