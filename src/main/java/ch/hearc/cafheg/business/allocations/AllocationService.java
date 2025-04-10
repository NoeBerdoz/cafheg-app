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
      if (info.isParent1Actif() && !info.isParent2Actif()) {
        return "PARENT_1";
      } else if (!info.isParent1Actif() && info.isParent2Actif()) {
        return "PARENT_2";
      }

      if (info.getParent1Salaire().compareTo(info.getParent2Salaire()) > 0) {
        return "PARENT_1";
      } else {
        return "PARENT_2";
      }
    }

}



