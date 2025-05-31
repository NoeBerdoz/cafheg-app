package ch.hearc.cafheg.business.allocations;

import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.AllocationMapper;
import java.math.BigDecimal;
import java.util.List;

public class AllocationService {

  private static final String PARENT_1 = "Parent1";
  private static final String PARENT_2 = "Parent2";

  private final AllocataireMapper allocataireMapper;
  private final AllocationMapper allocationMapper;

  public AllocationService(
      AllocataireMapper allocataireMapper,
      AllocationMapper allocationMapper) {
    this.allocataireMapper = allocataireMapper;
    this.allocationMapper = allocationMapper;
  }

  public List<Allocataire> findAllAllocataires(String likeNom) {
    return allocataireMapper.findAll(likeNom);
  }

  public List<Allocation> findAllocationsActuelles() {
    return allocationMapper.findAll();
  }

    /**
     * Méthode qui détermine quel parent a le droit aux allocations
     * @param droitsAllocations Paramètres de la requête
     * @return Le parent qui a le droit aux allocations
    */
  public String getParentDroitAllocation(DroitsAllocations droitsAllocations) {
    System.out.println("Déterminer quel parent a le droit aux allocations");
    Boolean p1AL = droitsAllocations.getParent1ActiviteLucrative();
    Boolean p2AL = droitsAllocations.getParent2ActiviteLucrative();
    Boolean p1AP = droitsAllocations.getParent1AutoriteParentale();
    Boolean p2AP = droitsAllocations.getParent2AutoriteParentale();
    Boolean parentsEnsemble = droitsAllocations.getParentsEnsemble();
    String enfantResidence = droitsAllocations.getEnfantResidence();
    Boolean enfantVitAvecP1 = droitsAllocations.getEnfantVitAvecParent1();
    Boolean enfantVitAvecP2 = droitsAllocations.getEnfantVitAvecParent2();
    String p1Residence = droitsAllocations.getParent1Residence();
    String p2Residence = droitsAllocations.getParent2Residence();
    Canton enfantCantonResidence = droitsAllocations.getEnfantCantonResidence();
    Canton p1CantonTravail = droitsAllocations.getParent1CantonTravail();
    Canton p2CantonTravail = droitsAllocations.getParent2CantonTravail();
    Boolean p1Independant = droitsAllocations.getParent1Independant();
    Boolean p2Independant = droitsAllocations.getParent2Independant();
    BigDecimal salaireP1 = droitsAllocations.getParent1Salaire();
    BigDecimal salaireP2 = droitsAllocations.getParent2Salaire();

    if (p1AL && !p2AL) { return PARENT_1; }
    if (!p1AL && p2AL) { return PARENT_2; }
    if (p1AP && !p2AP) { return PARENT_1; }
    if (!p1AP && p2AP) { return PARENT_2; }
    if (!parentsEnsemble) {
      // Si les parents ne sont pas ensemble, on regarde où vit l'enfant
      if (enfantResidence.equals(p1Residence) && enfantVitAvecP1) {
        return PARENT_1;
      }
      if (enfantResidence.equals(p2Residence) && enfantVitAvecP2) {
        return PARENT_2;
      }
    }
    // Si l'enfant vit avec les deux parents, on regarde le canton de travail
    if (p1CantonTravail == enfantCantonResidence) {
      return PARENT_1;
    } else if (p2CantonTravail == enfantCantonResidence) {
      return PARENT_2;
    }
    if (p1Independant && !p2Independant) {
      return PARENT_2;
    }
    if (!p1Independant && p2Independant) {
        return PARENT_1;
    }
    // Si les deux parents sont salariées, on compare les salaires
    if (!p1Independant && !p2Independant) {
      return salaireP1.doubleValue() > salaireP2.doubleValue() ? PARENT_1 : PARENT_2;
    }

    // Si les parents vivent ensemble et les deux sont indépendants, on compare les salaires
    return salaireP1.doubleValue() > salaireP2.doubleValue() ? PARENT_1 : PARENT_2;
  }
}
