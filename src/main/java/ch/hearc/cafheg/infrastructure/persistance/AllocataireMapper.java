package ch.hearc.cafheg.infrastructure.persistance;

import ch.hearc.cafheg.business.allocations.Allocataire;
import ch.hearc.cafheg.business.allocations.NoAVS;
import ch.hearc.cafheg.business.exceptions.AllocataireNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AllocataireMapper extends Mapper {

    private static final Logger log = LoggerFactory.getLogger(AllocataireMapper.class);

    private static final String QUERY_FIND_ALL = "SELECT NOM,PRENOM,NO_AVS FROM ALLOCATAIRES";
    private static final String QUERY_FIND_WHERE_NOM_LIKE = "SELECT NOM,PRENOM,NO_AVS FROM ALLOCATAIRES WHERE NOM LIKE ?";
    private static final String QUERY_FIND_WHERE_NUMERO = "SELECT NO_AVS, NOM, PRENOM FROM ALLOCATAIRES WHERE NUMERO=?";

    public List<Allocataire> findAll(String likeNom) {
        log.debug("findAll() called with likeNom='{}'", likeNom);
        Connection connection = activeJDBCConnection();
        try {
            PreparedStatement preparedStatement;
            if (likeNom == null) {
                log.trace("Executing SQL: {}", QUERY_FIND_ALL);
                preparedStatement = connection
                        .prepareStatement(QUERY_FIND_ALL);
            } else {
                log.trace("Executing SQL: {}", QUERY_FIND_WHERE_NOM_LIKE);
                preparedStatement = connection
                        .prepareStatement(QUERY_FIND_WHERE_NOM_LIKE);
                preparedStatement.setString(1, likeNom + "%");
            }
            log.debug("Allocation d'un nouveau tableau");
            List<Allocataire> allocataires = new ArrayList<>();

            log.debug("Exécution de la requête");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                log.debug("Allocataire mapping");
                while (resultSet.next()) {
                    log.trace("ResultSet#next");
                    allocataires
                            .add(new Allocataire(new NoAVS(resultSet.getString(3)), resultSet.getString(2),
                                    resultSet.getString(1)));
                }
            }
            log.debug("Nombre d'allocataires trouvés: {}", allocataires.size());
            return allocataires;
        } catch (SQLException e) {
            log.error("Erreur lors de l'exécution de findAll", e);
            throw new RuntimeException(e);
        }
    }

    public Allocataire findById(long id) {
        log.debug("findById() called with id={}", id);
        Connection connection = activeJDBCConnection();
        try {
            log.trace("Executing SQL: {}", QUERY_FIND_WHERE_NUMERO);
            PreparedStatement preparedStatement = connection.prepareStatement(QUERY_FIND_WHERE_NUMERO);
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            log.trace("ResultSet#next");
            if (resultSet.next()) {
                log.debug("Allocataire mapping");
                return new Allocataire(new NoAVS(resultSet.getString(1)),
                        resultSet.getString(2), resultSet.getString(3));
            } else {
                log.warn("Aucun allocataire trouvé avec l'ID: {}", id);
                throw new AllocataireNotFoundException("Allocataire non trouvé avec l'ID: " + id);
            }
        } catch (SQLException e) {
            log.error("Erreur SQL dans findById pour id={}", id, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes an allocataire from the database based on its ID.
     *
     * @param id The id of the allocataire to be deleted.
     * @return {@code true} if the recipient was successfully deleted
     * {@code false} if no recipient was found with that ID
     * @throws RuntimeException if a SQL error occurs during the delete operation.
     */
    public boolean deleteById(long id) {
        Connection connection = activeJDBCConnection();
        String query = "DELETE FROM ALLOCATAIRES WHERE NUMERO = ?";
        log.debug("deleteById() appelé pour ID={}", id);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, id);
            int rowAffected = preparedStatement.executeUpdate();
            boolean deleted = rowAffected > 0;
            log.debug("Suppression {} pour l'ID={}", deleted ? "réussie" : "échouée", id);
            return deleted;
        } catch (SQLException e) {
            log.error("Erreur lors de la suppression de l'allocataire ID={}", id, e);
            throw new RuntimeException("Erreur lors de la suppression de l'allocataire avec ID: " + id, e);
        }
    }

    /**
     * Updates the last name and first name of an allocataire.
     *
     * @param id     the ID of the recipient to update.
     * @param nom    the new last name for the recipient.
     * @param prenom the new first name for the recipient.
     * @return {@code true} if the allocataire was updated, {@code false} otherwise.
     * @throws RuntimeException if a database access error occurs.
     */
    public boolean updateNameAndFirstname(long id, String nom, String prenom) {
        Connection connection = activeJDBCConnection();
        String query = "UPDATE ALLOCATAIRES SET NOM = ?, PRENOM = ? WHERE NUMERO = ?";
        log.debug("updateNameAndFirstname() appelé pour ID={}, NOM={}, PRENOM={}", id, nom, prenom);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, nom);
            preparedStatement.setString(2, prenom);
            preparedStatement.setLong(3, id);
            int rowsAffected = preparedStatement.executeUpdate();
            boolean updated = rowsAffected > 0;
            log.debug("Mise à jour {} pour l'ID={}", updated ? "réussie" : "échouée", id);
            return updated;
        } catch (SQLException e) {
            log.error("Erreur lors de la mise à jour de l'allocataire ID={}", id, e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'allocataire ID: " + id, e);
        }
    }
}
