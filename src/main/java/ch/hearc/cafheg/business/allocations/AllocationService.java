package ch.hearc.cafheg.business.allocations;

import ch.hearc.cafheg.business.exceptions.AllocataireHasVersementsException;
import ch.hearc.cafheg.business.exceptions.AllocataireNotFoundException;
import ch.hearc.cafheg.business.exceptions.NoChangeToUpdateException;
import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.AllocationMapper;
import ch.hearc.cafheg.infrastructure.persistance.VersementMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class AllocationService {

    private static final Logger logger = LoggerFactory.getLogger(AllocationService.class);

    private static final String PARENT_1 = "Parent1";
    private static final String PARENT_2 = "Parent2";

    private final AllocataireMapper allocataireMapper;
    private final AllocationMapper allocationMapper;
    private final VersementMapper versementMapper;

    public AllocationService(
            AllocataireMapper allocataireMapper,
            AllocationMapper allocationMapper,
            VersementMapper versementMapper) {
        this.allocataireMapper = allocataireMapper;
        this.allocationMapper = allocationMapper;
        this.versementMapper = versementMapper;
    }

    public List<Allocataire> findAllAllocataires(String likeNom) {
        logger.info("Service: Recherche des allocataires", likeNom);
        return allocataireMapper.findAll(likeNom);
    }

    public List<Allocation> findAllocationsActuelles() {
        return allocationMapper.findAll();
    }

    public String getParentDroitAllocation(ParentAllocRequest request) {
        logger.info("Service: Détermination du parent ayant droit à l’allocation");

        if (hasOnlyParent1Activity(request)) return PARENT_1;
        if (hasOnlyParent2Activity(request)) return PARENT_2;

        if (hasOnlyParent1Autority(request)) return PARENT_1;
        if (hasOnlyParent2Autority(request)) return PARENT_2;

        if (!request.isParentsEnsemble()) {
            if (parent1LivesWithChild(request)) return PARENT_1;
            if (parent2LivesWithChild(request)) return PARENT_2;
        }

        if (worksOnlyParent1InChildCanton(request)) return PARENT_1;
        if (worksOnlyParent2InChildCanton(request)) return PARENT_2;

        return compareSalaryAccordingToIndependence(request);
    }

    // -------Méthodes privées pour vérifier les conditions

    // Parents avec activité lucrative
    private boolean hasOnlyParent1Activity(ParentAllocRequest r) {
        return r.isParent1ActiviteLucrative() && !r.isParent2ActiviteLucrative();
    }

    private boolean hasOnlyParent2Activity(ParentAllocRequest r) {
        return r.isParent2ActiviteLucrative() && !r.isParent1ActiviteLucrative();
    }

    // Autorité parentale
    private boolean hasOnlyParent1Autority(ParentAllocRequest r) {
        return r.isParent1AutoriteParentale() && !r.isParent2AutoriteParentale();
    }

    private boolean hasOnlyParent2Autority(ParentAllocRequest r) {
        return r.isParent2AutoriteParentale() && !r.isParent1AutoriteParentale();
    }

    // Parents qui vivent séparément
    private boolean parent1LivesWithChild(ParentAllocRequest r) {
        return r.getParent1Residence().equalsIgnoreCase(r.getEnfantResidence());
    }

    private boolean parent2LivesWithChild(ParentAllocRequest r) {
        return r.getParent2Residence().equalsIgnoreCase(r.getEnfantResidence());
    }

    // Parents qui travaillent dans le canton de l'enfant
    private boolean worksOnlyParent1InChildCanton(ParentAllocRequest r) {
        return r.getParent1CantonTravail().equalsIgnoreCase(r.getEnfantResidence()) &&
                !r.getParent2CantonTravail().equalsIgnoreCase(r.getEnfantResidence());
    }

    private boolean worksOnlyParent2InChildCanton(ParentAllocRequest r) {
        return r.getParent2CantonTravail().equalsIgnoreCase(r.getEnfantResidence()) &&
                !r.getParent1CantonTravail().equalsIgnoreCase(r.getEnfantResidence());
    }

    // Comparaison des salaires
    private String compareSalaryAccordingToIndependence(ParentAllocRequest r) {
        boolean p1Indep = r.isParent1EstIndependant();
        boolean p2Indep = r.isParent2EstIndependant();

        if (!p1Indep && p2Indep) return PARENT_1;
        if (p1Indep && !p2Indep) return PARENT_2;

        return r.getParent1Salaire().compareTo(r.getParent2Salaire()) > 0 ? PARENT_1 : PARENT_2;
    }

    /**
     * Deletes an allocataire after verifying business rules.
     * <p>
     * The allocataire must exist and must not have any associated versements.
     *
     * @param allocataireId the ID of the allocataire to delete.
     * @throws AllocataireNotFoundException      if the allocataire with the given ID is not found.
     * @throws AllocataireHasVersementsException if the allocataire has existing versements.
     * @throws RuntimeException                  if the deletion fails in the database for other reasons
     *                                           after initial checks have passed.
     */
    public void deleteAllocataire(long allocataireId) {
        logger.info("Service: Tentative de suppression de l'allocataire avec ID: " + allocataireId);

        Allocataire allocataire = allocataireMapper.findById(allocataireId);
        if (allocataire == null) {
            logger.warn("Allocataire non trouvé avec ID: " + allocataireId);
            throw new AllocataireNotFoundException("L'allocataire avec ID: " + allocataireId + " n'a pas été trouvé.");
        }

        if (versementMapper.countVersementsByAllocataireId(allocataireId) > 0) {
            logger.warn("Suppression refusée : l'allocataire ID {} a des versements", allocataireId);
            throw new AllocataireHasVersementsException("L'allocataire avec ID: " + allocataireId + " a des versements.");
        }

        try {
            boolean deleted = allocataireMapper.deleteById(allocataireId);
            if (!deleted) {
                logger.error("La suppression a échoué pour l'allocataire ID: {}", allocataireId);
                throw new RuntimeException("La suppression a échoué.");
            }
            logger.info("Service: Allocataire ID {} supprimé avec succès", allocataireId);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la suppression de l’allocataire ID {}", allocataireId, e);
            throw e;
        }
    }

    /**
     * Updates the last name and first name of an existing allocataire.
     * <p>
     * The update is performed only if the provided new last name or new first name
     * is different from the existing ones. The AVS number remains unchanged.
     *
     * @param allocataireId the ID of the allocataire to update.
     * @param newNom        the new last name for the allocataire; cannot be null or empty.
     * @param newPrenom     the new first name for the allocataire; cannot be null or empty.
     * @return the updated {@link Allocataire} object.
     * @throws IllegalArgumentException     if {@code newNom} or {@code newPrenom} is null or empty.
     * @throws AllocataireNotFoundException if the allocataire with the given ID is not found.
     * @throws NoChangeToUpdateException    if neither the last name nor the first name has changed.
     * @throws RuntimeException             if the update fails in the database for other reasons.
     */
    public Allocataire updateAllocataire(long allocataireId, String newNom, String newPrenom) {
        logger.info("Service: Tentative de mise à jour du nom/prénom de l'allocataire avec ID: {}", allocataireId);

        // Validate input
        if (newNom == null || newNom.trim().isEmpty() || newPrenom == null || newPrenom.trim().isEmpty()) {
            logger.warn("Le nom ou le prénom fourni est vide pour l'allocataire ID: {}", allocataireId);
            throw new IllegalArgumentException("Le nom et le prénom de l'allocataire ne peuvent pas être vides.");
        }

        Allocataire existingAllocataire = allocataireMapper.findById(allocataireId);
        if (existingAllocataire == null) {
            logger.error("Allocataire non trouvé avec ID: {}", allocataireId);
            throw new AllocataireNotFoundException("L'allocataire avec ID: " + allocataireId + "n'a pas été trouvé");
        }

        boolean nameChanged = !existingAllocataire.getNom().equals(newNom.trim());
        boolean firstnameChanged = !existingAllocataire.getPrenom().equals(newPrenom.trim());

        // Update should be done only if a changed is detected
        if (!nameChanged && !firstnameChanged) {
            logger.info("Aucun changement détecté pour l'allocataire ID: {}", allocataireId);
            throw new NoChangeToUpdateException("Aucune modification détectée pour l'allocataire ID: " + allocataireId);
        }

        boolean updatedInDb = allocataireMapper.updateNameAndFirstname(allocataireId, newNom.trim(), newPrenom.trim());

        if (!updatedInDb) {
            logger.error("La mise à jour de l'allocataire ID: {} a échoué en base de données.", allocataireId);
            throw new RuntimeException("La mise à jour de l'allocataire ID: " + allocataireId + " a échoué en base de données.");
        }

        logger.info("Service: Allocataire ID {} mis à jour avec succès", allocataireId);
        return new Allocataire(existingAllocataire.getNoAVS(), newNom.trim(), newPrenom.trim());
    }
}

