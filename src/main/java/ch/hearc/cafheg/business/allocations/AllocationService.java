package ch.hearc.cafheg.business.allocations;

import ch.hearc.cafheg.business.exceptions.AllocataireHasVersementsException;
import ch.hearc.cafheg.business.exceptions.AllocataireNotFoundException;
import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.AllocationMapper;
import ch.hearc.cafheg.infrastructure.persistance.VersementMapper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class AllocationService {

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
    System.out.println("Rechercher tous les allocataires");
    return allocataireMapper.findAll(likeNom);
  }

  public List<Allocation> findAllocationsActuelles() {
    return allocationMapper.findAll();
  }

  public String getParentDroitAllocation(Map<String, Object> parameters) {
    System.out.println("Déterminer quel parent a le droit aux allocations");
    String eR = (String)parameters.getOrDefault("enfantResidence", "");
    Boolean p1AL = (Boolean)parameters.getOrDefault("parent1ActiviteLucrative", false);
    String p1Residence = (String)parameters.getOrDefault("parent1Residence", "");
    Boolean p2AL = (Boolean)parameters.getOrDefault("parent2ActiviteLucrative", false);
    String p2Residence = (String)parameters.getOrDefault("parent2Residence", "");
    Boolean pEnsemble = (Boolean)parameters.getOrDefault("parentsEnsemble", false);
    Number salaireP1 = (Number) parameters.getOrDefault("parent1Salaire", BigDecimal.ZERO);
    Number salaireP2 = (Number) parameters.getOrDefault("parent2Salaire", BigDecimal.ZERO);

    if(p1AL && !p2AL) {
      return PARENT_1;
    }

    if(p2AL && !p1AL) {
      return PARENT_2;
    }

    return salaireP1.doubleValue() > salaireP2.doubleValue() ? PARENT_1 : PARENT_2;
  }

  public void deleteAllocataire(long allocataireId) {
    System.out.println("Service: Tentative de suppression de l'allocataire avec ID: " + allocataireId);

    Allocataire allocataire = allocataireMapper.findById(allocataireId);
    if (allocataire == null) {
      throw new AllocataireNotFoundException("L'allocataire avec ID: " + allocataireId + " n'a pas été trouvé.");
    }

    if (versementMapper.countVersementsByAllocataireId(allocataireId) > 0) {
      throw new AllocataireHasVersementsException("L'allocataire avec ID: " + allocataireId + " a des versements.");
    }

    boolean deleted = allocataireMapper.deleteById(allocataireId);
    if(!deleted) {
      throw new RuntimeException("La suppression de l'allocataire avec ID: " + allocataireId + " a échoué en base de données bien qu'il ait été trouvé initialement et n'ait pas de versements.");
    }

    System.out.println("Service: Allocataire avec ID " + allocataireId + " supprimé avec succès.");
  }
}
