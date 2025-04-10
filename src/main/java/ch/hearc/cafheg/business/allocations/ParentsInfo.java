package ch.hearc.cafheg.business.allocations;

import java.math.BigDecimal;

public class ParentsInfo {
    private final boolean parent1ActiviteLucrative;
    private final boolean parent2ActiviteLucrative;
    private final BigDecimal parent1Salaire;
    private final BigDecimal parent2Salaire;

    public ParentsInfo(boolean parent1ActiviteLucrative, boolean parent2ActiviteLucrative,
                       BigDecimal parent1Salaire, BigDecimal parent2Salaire) {
        this.parent1ActiviteLucrative = parent1ActiviteLucrative;
        this.parent2ActiviteLucrative = parent2ActiviteLucrative;
        this.parent1Salaire = parent1Salaire != null ? parent1Salaire : BigDecimal.ZERO;
        this.parent2Salaire = parent2Salaire != null ? parent2Salaire : BigDecimal.ZERO;
    }

    public boolean isParent1Actif() { return parent1ActiviteLucrative; }

    public boolean isParent2Actif() { return parent2ActiviteLucrative; }

    public BigDecimal getParent1Salaire() { return parent1Salaire; }

    public BigDecimal getParent2Salaire() { return parent2Salaire; }
}
