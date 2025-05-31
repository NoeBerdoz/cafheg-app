package ch.hearc.cafheg.business.allocations;
import java.math.BigDecimal;

public class DroitsAllocations {
    private String enfantResidence;
    private String parent1Residence;
    private Boolean parent1ActiviteLucrative;
    private String parent2Residence;
    private Boolean parent2ActiviteLucrative;
    private Boolean parentsEnsemble;
    private BigDecimal parent1Salaire;
    private BigDecimal parent2Salaire;

    public DroitsAllocations() {
    }

    public DroitsAllocations(String enfantResidence, String parent1Residence, Boolean parent1ActiviteLucrative, String parent2Residence,
                             Boolean parent2ActiviteLucrative, Boolean parentsEnsemble, BigDecimal parent1Salaire, BigDecimal parent2Salaire)
    {
        this.enfantResidence = enfantResidence;
        this.parent1Residence = parent1Residence;
        this.parent1ActiviteLucrative = parent1ActiviteLucrative;
        this.parent2Residence = parent2Residence;
        this.parent2ActiviteLucrative = parent2ActiviteLucrative;
        this.parentsEnsemble = parentsEnsemble;
        this.parent1Salaire = parent1Salaire;
        this.parent2Salaire = parent2Salaire;
    }

    public String getEnfantResidence() {
        return enfantResidence;
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
}
