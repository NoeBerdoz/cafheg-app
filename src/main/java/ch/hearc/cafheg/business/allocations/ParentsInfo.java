package ch.hearc.cafheg.business.allocations;

import java.math.BigDecimal;

public class ParentsInfo {
    private final boolean parent1ActiviteLucrative;
    private final boolean parent2ActiviteLucrative;
    private final BigDecimal parent1Salaire;
    private final BigDecimal parent2Salaire;
    private final boolean autoriteParentalePartagee;
    private final boolean parentsEnsemble;
    private final boolean parent1VitAvecEnfant;
    private final boolean parent2VitAvecEnfant;
    private final boolean parent1TravailleDansCantonEnfant;
    private final boolean parent2TravailleDansCantonEnfant;

    public ParentsInfo(
            boolean parent1ActiviteLucrative,
            boolean parent2ActiviteLucrative,
            BigDecimal parent1Salaire,
            BigDecimal parent2Salaire,
            boolean autoriteParentalePartagee,
            boolean parentsEnsemble,
            boolean parent1VitAvecEnfant,
            boolean parent2VitAvecEnfant,
            boolean parent1TravailleDansCantonEnfant,
            boolean parent2TravailleDansCantonEnfant) {

        this.parent1ActiviteLucrative = parent1ActiviteLucrative;
        this.parent2ActiviteLucrative = parent2ActiviteLucrative;
        this.parent1Salaire = parent1Salaire != null ? parent1Salaire : BigDecimal.ZERO;
        this.parent2Salaire = parent2Salaire != null ? parent2Salaire : BigDecimal.ZERO;
        this.autoriteParentalePartagee = autoriteParentalePartagee;
        this.parentsEnsemble = parentsEnsemble;
        this.parent1VitAvecEnfant = parent1VitAvecEnfant;
        this.parent2VitAvecEnfant = parent2VitAvecEnfant;
        this.parent1TravailleDansCantonEnfant = parent1TravailleDansCantonEnfant;
        this.parent2TravailleDansCantonEnfant = parent2TravailleDansCantonEnfant;
    }

    public boolean isParent1Actif() {
        return parent1ActiviteLucrative;
    }

    public boolean isParent2Actif() {
        return parent2ActiviteLucrative;
    }

    public BigDecimal getParent1Salaire() {
        return parent1Salaire;
    }

    public BigDecimal getParent2Salaire() {
        return parent2Salaire;
    }

    public boolean isAutoriteParentalePartagee() {
        return autoriteParentalePartagee;
    }

    public boolean isParentsEnsemble() {
        return parentsEnsemble;
    }

    public boolean isParent1VitAvecEnfant() {
        return parent1VitAvecEnfant;
    }

    public boolean isParent2VitAvecEnfant() {
        return parent2VitAvecEnfant;
    }

    public boolean isParent1TravailleDansCantonEnfant() {
        return parent1TravailleDansCantonEnfant;
    }

    public boolean isParent2TravailleDansCantonEnfant() {
        return parent2TravailleDansCantonEnfant;
    }

}
