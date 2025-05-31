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
    System.out.println("Rechercher tous les allocataires");
    return allocataireMapper.findAll(likeNom);
  }

  public List<Allocation> findAllocationsActuelles() {
    return allocationMapper.findAll();
  }

    /**
     * Méthode qui détermine quel parent a le droit aux allocations
     * @param droitsAllocations Paramètres de la requête
     * @return Le parent qui a le droit aux allocations
      1 seul parent actif (Soit P1, soit P2)
      Les 2 parents sont actifs --> On compare les salaires
      Les 2 parents sont actifs et ont le même salaire --> On retourne P2 (car pas strictement plus grand que P1)
      Les 2 parents sont inactifs --> On retourne P2 (car pas strictement plus grand que P1)
      Plus valable avec la classe -> La map fournie en paramètre ne contient pas les valeurs nécessaires
        --> getOrDefault renvoie la valeur par défaut et donc P2 doit être retourné
      La résidence de l'enfant et des parents n'a aucun impact sur le droit aux allocations
    */
  public String getParentDroitAllocation(DroitsAllocations droitsAllocations) {
    System.out.println("Déterminer quel parent a le droit aux allocations");
    Boolean p1AL = droitsAllocations.getParent1ActiviteLucrative();
    Boolean p2AL = droitsAllocations.getParent2ActiviteLucrative();
    BigDecimal salaireP1 = droitsAllocations.getParent1Salaire();
    BigDecimal salaireP2 = droitsAllocations.getParent2Salaire();
    String enfantResidence = droitsAllocations.getEnfantResidence();
    String parent1Residence = droitsAllocations.getParent1Residence();
    String parent2Residence = droitsAllocations.getParent2Residence();
    Boolean parentsEnsemble = droitsAllocations.getParentsEnsemble();

    if (!parentsEnsemble) {
      if (enfantResidence.equals(parent1Residence)) {
        return PARENT_1;
      } else if (enfantResidence.equals(parent2Residence)) {
        return PARENT_2;
      }
    } else {
      if(p1AL && !p2AL) {
        return PARENT_1;
      } else if(!p1AL && p2AL) {
        return PARENT_2;
      } else if (!p1AL && !p2AL) {
        // Si les deux parents sont inactifs, on retourne PARENT_2
        return PARENT_2;
      }
    }

    //Si les parents sont ensemble et les deux actifs, on compare les salaires
    return salaireP1.doubleValue() > salaireP2.doubleValue() ? PARENT_1 : PARENT_2;
  }
}
