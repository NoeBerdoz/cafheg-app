package ch.hearc.cafheg.business.allocations;

import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.AllocationMapper;
import java.util.List;

public class AllocationService {

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


  public String getParentDroitAllocation(ParentsInfo info) {
    // Cas 1 : un seul parent a une activité lucrative
    if (info.isParent1Actif() && !info.isParent2Actif()) {
      return "PARENT_1";
    } else if (!info.isParent1Actif() && info.isParent2Actif()) {
      return "PARENT_2";
    }

    // Cas 2 : les deux parents ont une activité lucrative
    if (!info.isAutoriteParentalePartagee()) {
      // Un seul parent a l’autorité parentale
      if (info.isParent1Actif()) {
        return "PARENT_1";
      } else {
        return "PARENT_2";
      }
    } else {
      // Les deux ont l’autorité parentale
      if (!info.isParentsEnsemble()) {
        // Parents séparés
        if (info.isParent1VitAvecEnfant() && !info.isParent2VitAvecEnfant()) {
          return "PARENT_1";
        } else if (!info.isParent1VitAvecEnfant() && info.isParent2VitAvecEnfant()) {
          return "PARENT_2";
        } else {
          // Les deux vivent avec l'enfant → canton de domicile
          if (info.isParent1TravailleDansCantonEnfant()) {
            return "PARENT_1";
          } else if (info.isParent2TravailleDansCantonEnfant()) {
            return "PARENT_2";
          }
        }
      } else {
        // Parents vivent ensemble
        if (info.getParent1Salaire().compareTo(info.getParent2Salaire()) > 0) {
          return "PARENT_1";
        } else {
          return "PARENT_2";
        }
      }
    }

    // Cas par défaut si aucune règle ne s’applique (optionnel)
    return "INDETERMINE";
  }

}



