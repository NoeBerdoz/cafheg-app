package ch.hearc.cafheg.infrastructure.persistance;

import ch.hearc.cafheg.business.allocations.Allocataire;
import ch.hearc.cafheg.business.allocations.NoAVS;
import ch.hearc.cafheg.business.exceptions.AllocataireNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AllocataireMapper extends Mapper {

  private static final String QUERY_FIND_ALL = "SELECT NOM,PRENOM,NO_AVS FROM ALLOCATAIRES";
  private static final String QUERY_FIND_WHERE_NOM_LIKE = "SELECT NOM,PRENOM,NO_AVS FROM ALLOCATAIRES WHERE NOM LIKE ?";
  private static final String QUERY_FIND_WHERE_NUMERO = "SELECT NO_AVS, NOM, PRENOM FROM ALLOCATAIRES WHERE NUMERO=?";

  public List<Allocataire> findAll(String likeNom) {
    System.out.println("findAll() " + likeNom);
    Connection connection = activeJDBCConnection();
    try {
      PreparedStatement preparedStatement;
      if (likeNom == null) {
        System.out.println("SQL: " + QUERY_FIND_ALL);
        preparedStatement = connection
            .prepareStatement(QUERY_FIND_ALL);
      } else {

        System.out.println("SQL: " + QUERY_FIND_WHERE_NOM_LIKE);
        preparedStatement = connection
            .prepareStatement(QUERY_FIND_WHERE_NOM_LIKE);
        preparedStatement.setString(1, likeNom + "%");
      }
      System.out.println("Allocation d'un nouveau tableau");
      List<Allocataire> allocataires = new ArrayList<>();

      System.out.println("Exécution de la requête");
      try (ResultSet resultSet = preparedStatement.executeQuery()) {

        System.out.println("Allocataire mapping");
        while (resultSet.next()) {
          System.out.println("ResultSet#next");
          allocataires
              .add(new Allocataire(new NoAVS(resultSet.getString(3)), resultSet.getString(2),
                  resultSet.getString(1)));
        }
      }
      System.out.println("Allocataires trouvés " + allocataires.size());
      return allocataires;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Allocataire findById(long id) {
    System.out.println("findById() " + id);
    Connection connection = activeJDBCConnection();
    try {
      System.out.println("SQL:" + QUERY_FIND_WHERE_NUMERO);
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_FIND_WHERE_NUMERO);
      preparedStatement.setLong(1, id);
      ResultSet resultSet = preparedStatement.executeQuery();
      System.out.println("ResultSet#next");
      if (resultSet.next()) {
        System.out.println("Allocataire mapping");
        return new Allocataire(new NoAVS(resultSet.getString(1)),
                resultSet.getString(2), resultSet.getString(3));
      } else {
        throw new AllocataireNotFoundException("Allocataire non trouvé avec l'ID: " + id);
      }
    } catch (SQLException e) {
      System.err.println("SQL: Erreur: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public boolean deleteById(long id) {
    Connection connection = activeJDBCConnection();
    String query = "DELETE FROM ALLOCATAIRES WHERE NUMERO = ?";
    try {
      System.out.println("SQL: Suppression d'un allocataire avec ID: " + id);
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setLong(1, id);
      int rowAffected = preparedStatement.executeUpdate();
      return rowAffected > 0;
    } catch (SQLException e) {
      System.out.println("SQL: Erreur: " + e.getMessage());
      throw new RuntimeException("Erreur lors de la suppression de l'allocataire avec ID: " + id, e);
    }
  }

  public boolean updateNameAndFirstname(long id, String nom, String prenom) {
    Connection connection = activeJDBCConnection();
    String query = "UPDATE ALLOCATAIRES SET NOM = ?, PRENOM = ? WHERE NUMERO = ?";
    try {
      System.out.println("SQL: Mise à jour d'un allocataire avec ID: " + id);
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setString(1, nom);
      preparedStatement.setString(2, prenom);
      preparedStatement.setLong(3, id);
      int rowsAffected = preparedStatement.executeUpdate();
      return rowsAffected > 0;
    } catch (SQLException e) {
      System.out.println("SQL: Erreur: " + e.getMessage());
      throw new RuntimeException("Erreur lors de la mise à jour de l'allocataire ID: " + id, e);
    }
  }
}
