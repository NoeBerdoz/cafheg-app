package ch.hearc.cafheg.business.allocations;

import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.AllocationMapper;

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

    public String getParentDroitAllocation(ParentAllocRequest request) {
        System.out.println("Déterminer le parent ayant droit à l'allocation");

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
}

