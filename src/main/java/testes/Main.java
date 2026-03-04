package testes;

import entities.Equipement;
import entities.Operation;
import services.EquipementService;
import services.Iservice;
import services.OperationService;

import java.util.List;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class Main {
    public static void main(String[] args) throws Exception {
        Iservice<Operation> operation = new OperationService();
        Iservice<Equipement> equipement = new EquipementService();
        OperationService Oservice = new OperationService();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date dateDebut = new Date(sdf.parse("21/05/2024").getTime());
        Date dateFin = new Date(sdf.parse("21/05/2024").getTime());

        Operation op1 = new Operation("nadim", dateDebut, dateFin, "en cours");
        Operation op2 = new Operation("Hamma", dateDebut, dateFin, "terminer");
        Operation op3 = new Operation("shimi", dateDebut, dateFin, "en retard");
        Operation op4 = new Operation("Khchini", dateDebut, dateFin, "en retard");

        Equipement eq1 = new Equipement("Drone","Drone", "reserve");
        Equipement eq2 = new Equipement("Tractor","Tractor", "libre");

        operation.ajouter(op1);
        operation.ajouter(op2);
        operation.ajouter(op3);
        operation.ajouter(op4);
        equipement.ajouter(eq1);
        equipement.ajouter(eq2);

        System.out.println(" \n ****Affichage des opérations et équipements**** \n");
        System.out.println(operation.afficher());
        System.out.println("\n ****Affichage des équipements**** \n");
        System.out.println(equipement.afficher());
        System.out.println("\n *********************************************** \n");

        // Appel  de recherche et tri
        List<Operation> resultatRecherche = Oservice.rechercherParType("nadim");
        System.out.println("Résultat de la recherche : ");
        for (Operation op : resultatRecherche) {
            System.out.println(op);
        }

        List<Operation> operationsTriees = Oservice.trierParNom();
        System.out.println("\nListe des opérations triées par nom :");
        for (Operation op : operationsTriees) {
            System.out.println(op);
        }

        //Oservice.supprimer(op7);
        //System.out.println(Oservice.afficher());
    }
}
