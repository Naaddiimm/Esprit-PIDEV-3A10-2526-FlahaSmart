package servise;

import java.sql.SQLException;
import java.util.List;

public interface IService <T>{
    void ajouter (T t) throws SQLException ; // ma3neha raw l méthode hethy 3andha un risque de faire telle et telle
    void supprimer (T t) throws SQLException ;
    void modifier (T t) throws SQLException ;
    List<T> recuperer () throws SQLException ;
  // nefhem chma3neha throw w throws w chnoua far9 binethom w win ythatou

    
}
