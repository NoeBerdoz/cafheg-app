package ch.hearc.cafheg.business.allocations;

import java.math.BigDecimal;

public class ParentAllocRequest {
    private String enfantResidence;
    private boolean parent1ActiviteLucrative;
    private String parent1Residence;
    private boolean parent2ActiviteLucrative;


    private String parent2Residence;
    private boolean parentsEnsemble;
    private BigDecimal parent1Salaire;
    private BigDecimal parent2Salaire;
    private boolean parent1AutoriteParentale;
    private boolean parent2AutoriteParentale;
    private boolean parent1EstIndependant;
    private boolean parent2EstIndependant;
    private String parent1CantonTravail;
    private String parent2CantonTravail;

    /**
     * Constructeur de la classe ParentAllocRequest
     *
     * @param enfantResidence          La résidence de l'enfant
     * @param parent1ActiviteLucrative L'activité lucrative du parent 1
     * @param parent1Residence         La résidence du parent 1
     * @param parent2ActiviteLucrative L'activité lucrative du parent 2
     * @param parent2Residence         La résidence du parent 2
     * @param parentsEnsemble          Indique si les parents vivent ensemble
     * @param parent1Salaire           Le salaire du parent 1
     * @param parent2Salaire           Le salaire du parent 2
     */

    public ParentAllocRequest(String enfantResidence, boolean parent1ActiviteLucrative, String parent1Residence,
                              boolean parent2ActiviteLucrative, String parent2Residence, boolean parentsEnsemble,
                              BigDecimal parent1Salaire, BigDecimal parent2Salaire, boolean parent1AutoriteParentale, boolean parent2AutoriteParentale,
                              boolean parent1EstIndependant, boolean parent2EstIndependant, String parent1CantonTravail, String parent2CantonTravail) {
        this.enfantResidence = enfantResidence;
        this.parent1ActiviteLucrative = parent1ActiviteLucrative;
        this.parent1Residence = parent1Residence;
        this.parent2ActiviteLucrative = parent2ActiviteLucrative;
        this.parent2Residence = parent2Residence;
        this.parentsEnsemble = parentsEnsemble;
        this.parent1Salaire = parent1Salaire;
        this.parent2Salaire = parent2Salaire;
        this.parent1AutoriteParentale = parent1AutoriteParentale;
        this.parent2AutoriteParentale = parent2AutoriteParentale;
        this.parent1EstIndependant = parent1EstIndependant;
        this.parent2EstIndependant = parent2EstIndependant;
        this.parent1CantonTravail = parent1CantonTravail;
        this.parent2CantonTravail = parent2CantonTravail;
    }

    public ParentAllocRequest() {

    }

    public String getEnfantResidence() {
        return enfantResidence;
    }

    public boolean isParent1ActiviteLucrative() {
        return parent1ActiviteLucrative;
    }

    public String getParent1Residence() {
        return parent1Residence;
    }

    public String getParent2Residence() {
        return parent2Residence;
    }

    public boolean isParent2ActiviteLucrative() {
        return parent2ActiviteLucrative;
    }

    public boolean isParentsEnsemble() {
        return parentsEnsemble;
    }

    public BigDecimal getParent1Salaire() {
        return parent1Salaire;
    }

    public BigDecimal getParent2Salaire() {
        return parent2Salaire;
    }

    public boolean isParent1AutoriteParentale() {
        return parent1AutoriteParentale;
    }

    public boolean isParent2AutoriteParentale() {
        return parent2AutoriteParentale;
    }

    public boolean isParent1EstIndependant() {
        return parent1EstIndependant;
    }

    public boolean isParent2EstIndependant() {
        return parent2EstIndependant;
    }

    public String getParent1CantonTravail() {
        return parent1CantonTravail;
    }

    public String getParent2CantonTravail() {
        return parent2CantonTravail;
    }

}
