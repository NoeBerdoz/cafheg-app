package ch.hearc.cafheg.business.allocations;
import java.math.BigDecimal;

public class DroitsAllocations {
    private Boolean parent1ActiviteLucrative;
    private Boolean parent2ActiviteLucrative;
    private Boolean parent1AutoriteParentale;
    private Boolean parent2AutoriteParentale;
    private Boolean parentsEnsemble;
    private String enfantResidence;
    private Boolean enfantVitAvecParent1;
    private Boolean enfantVitAvecParent2;
    private String parent1Residence;
    private String parent2Residence;
    private Canton enfantCantonResidence;
    private Canton parent1CantonTravail;
    private Canton parent2CantonTravail;
    private Boolean parent1Independant;
    private Boolean parent2Independant;
    private BigDecimal parent1Salaire;
    private BigDecimal parent2Salaire;

    public DroitsAllocations() {
    }

    public DroitsAllocations (Boolean parent1ActiviteLucrative,
                              Boolean parent2ActiviteLucrative,
                              Boolean parent1AutoriteParentale,
                              Boolean parent2AutoriteParentale,
                              Boolean parentsEnsemble,
                              String enfantResidence,
                              Boolean enfantVitAvecParent1,
                              Boolean enfantVitAvecParent2,
                              String parent1Residence,
                              String parent2Residence,
                              Canton enfantCantonResidence,
                              Canton parent1CantonTravail,
                              Canton parent2CantonTravail,
                              Boolean parent1Independant,
                              Boolean parent2Independant,
                              BigDecimal parent1Salaire,
                              BigDecimal parent2Salaire) {
        this.parent1ActiviteLucrative = parent1ActiviteLucrative;
        this.parent2ActiviteLucrative = parent2ActiviteLucrative;
        this.parent1AutoriteParentale = parent1AutoriteParentale;
        this.parent2AutoriteParentale = parent2AutoriteParentale;
        this.parentsEnsemble = parentsEnsemble;
        this.enfantResidence = enfantResidence;
        this.enfantVitAvecParent1 = enfantVitAvecParent1;
        this.enfantVitAvecParent2 = enfantVitAvecParent2;
        this.parent1Residence = parent1Residence;
        this.parent2Residence = parent2Residence;
        this.enfantCantonResidence = enfantCantonResidence;
        this.parent1CantonTravail = parent1CantonTravail;
        this.parent2CantonTravail = parent2CantonTravail;
        this.parent1Independant = parent1Independant;
        this.parent2Independant = parent2Independant;
        this.parent1Salaire = parent1Salaire;
        this.parent2Salaire = parent2Salaire;
    }

    public String getEnfantResidence() {
        return enfantResidence;
    }

    public Canton getEnfantCantonResidence() {
        return enfantCantonResidence;
    }

    public String getParent1Residence() {
        return parent1Residence;
    }

    public Boolean getParent1ActiviteLucrative() {
        return parent1ActiviteLucrative;
    }

    public String getParent2Residence() {
        return parent2Residence;
    }

    public Boolean getParent2ActiviteLucrative() {
        return parent2ActiviteLucrative;
    }

    public Boolean getParentsEnsemble() {
        return parentsEnsemble;
    }

    public BigDecimal getParent1Salaire() {
        return parent1Salaire;
    }

    public BigDecimal getParent2Salaire() {
        return parent2Salaire;
    }

    public Boolean getParent1AutoriteParentale() {
        return parent1AutoriteParentale;
    }

    public Boolean getParent2AutoriteParentale() {
        return parent2AutoriteParentale;
    }

    public Boolean getEnfantVitAvecParent1() {
        return enfantVitAvecParent1;
    }

    public Boolean getEnfantVitAvecParent2() {
        return enfantVitAvecParent2;
    }

    public Canton getParent1CantonTravail() {
        return parent1CantonTravail;
    }

    public Canton getParent2CantonTravail() {
        return parent2CantonTravail;
    }

    public Boolean getParent1Independant() {
        return parent1Independant;
    }

    public Boolean getParent2Independant() {
        return parent2Independant;
    }

}
