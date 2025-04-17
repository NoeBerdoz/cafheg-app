package ch.hearc.cafheg.business.allocations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import ch.hearc.cafheg.business.common.Montant;
import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.AllocationMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AllocationServiceTest {

  private AllocationService allocationService;

  private AllocataireMapper allocataireMapper;
  private AllocationMapper allocationMapper;

  // Service pour faciliter les tests
  private ParentsInfo createParentsInfo(
          boolean parent1ActiviteLucrative,
          boolean parent2ActiviteLucrative,
          BigDecimal parent1Salaire,
          BigDecimal parent2Salaire
  ) {
    return new ParentsInfo(
            parent1ActiviteLucrative,
            parent2ActiviteLucrative,
            parent1Salaire,
            parent2Salaire,
            true,   // autoriteParentalePartagee
            true,   // parentsEnsemble
            true,   // parent1VitAvecEnfant
            true,   // parent2VitAvecEnfant
            true,   // parent1TravailleDansCantonEnfant
            true    // parent2TravailleDansCantonEnfant
    );
  }

  @BeforeEach
  void setUp() {
    allocataireMapper = Mockito.mock(AllocataireMapper.class);
    allocationMapper = Mockito.mock(AllocationMapper.class);

    allocationService = new AllocationService(allocataireMapper, allocationMapper);
  }

  @Test
  void findAllAllocataires_GivenEmptyAllocataires_ShouldBeEmpty() {
    Mockito.when(allocataireMapper.findAll("Geiser")).thenReturn(Collections.emptyList());
    List<Allocataire> all = allocationService.findAllAllocataires("Geiser");
    assertThat(all).isEmpty();
  }

  @Test
  void findAllAllocataires_Given2Geiser_ShouldBe2() {
    Mockito.when(allocataireMapper.findAll("Geiser"))
        .thenReturn(Arrays.asList(new Allocataire(new NoAVS("1000-2000"), "Geiser", "Arnaud"),
            new Allocataire(new NoAVS("1000-2001"), "Geiser", "Aurélie")));
    List<Allocataire> all = allocationService.findAllAllocataires("Geiser");
    assertAll(() -> assertThat(all.size()).isEqualTo(2),
        () -> assertThat(all.get(0).getNoAVS()).isEqualTo(new NoAVS("1000-2000")),
        () -> assertThat(all.get(0).getNom()).isEqualTo("Geiser"),
        () -> assertThat(all.get(0).getPrenom()).isEqualTo("Arnaud"),
        () -> assertThat(all.get(1).getNoAVS()).isEqualTo(new NoAVS("1000-2001")),
        () -> assertThat(all.get(1).getNom()).isEqualTo("Geiser"),
        () -> assertThat(all.get(1).getPrenom()).isEqualTo("Aurélie"));
  }

  @Test
  void findAllocationsActuelles() {
    Mockito.when(allocationMapper.findAll())
        .thenReturn(Arrays.asList(new Allocation(new Montant(new BigDecimal(1000)), Canton.NE,
            LocalDate.now(), null), new Allocation(new Montant(new BigDecimal(2000)), Canton.FR,
            LocalDate.now(), null)));
    List<Allocation> all = allocationService.findAllocationsActuelles();
    assertAll(() -> assertThat(all.size()).isEqualTo(2),
        () -> assertThat(all.get(0).getMontant()).isEqualTo(new Montant(new BigDecimal(1000))),
        () -> assertThat(all.get(0).getCanton()).isEqualTo(Canton.NE),
        () -> assertThat(all.get(0).getDebut()).isEqualTo(LocalDate.now()),
        () -> assertThat(all.get(0).getFin()).isNull(),
        () -> assertThat(all.get(1).getMontant()).isEqualTo(new Montant(new BigDecimal(2000))),
        () -> assertThat(all.get(1).getCanton()).isEqualTo(Canton.FR),
        () -> assertThat(all.get(1).getDebut()).isEqualTo(LocalDate.now()),
        () -> assertThat(all.get(1).getFin()).isNull());
  }

  @Test
  void getParentDroitAllocation_Parent1ActifParent2Inactif_ReturnsParent1() {
    ParentsInfo info = createParentsInfo(
            true,   // parent1ActiviteLucrative
            false,   // parent2ActiviteLucrative
            BigDecimal.ZERO, // parent1Salaire
            BigDecimal.ZERO  // parent2Salaire
    );
    String result = allocationService.getParentDroitAllocation(info);
    assertThat(result).isEqualTo("PARENT_1");
  }

  @Test
  void getParentDroitAllocation_Parent2ActifParent1Inactif_ReturnsParent2() {
    ParentsInfo info = createParentsInfo(
            false,   // parent1ActiviteLucrative
            true,   // parent2ActiviteLucrative
            BigDecimal.ZERO, // parent1Salaire
            BigDecimal.ZERO  // parent2Salaire
    );
    String result = allocationService.getParentDroitAllocation(info);
    assertThat(result).isEqualTo("PARENT_2");
  }

  @Test
  void getParentDroitAllocation_BothInactive_SalaireParent1PlusHaut_ReturnsParent1() {
    ParentsInfo info = createParentsInfo(
            false,   // parent1ActiviteLucrative
            false,   // parent2ActiviteLucrative
            new BigDecimal("5000"), // parent1Salaire
            new BigDecimal("3000")  // parent2Salaire
    );
    String result = allocationService.getParentDroitAllocation(info);
    assertThat(result).isEqualTo("PARENT_1");
  }

  @Test
  void getParentDroitAllocation_BothInactive_SalaireParent2PlusHaut_ReturnsParent2() {
    ParentsInfo info = createParentsInfo(
            false,   // parent1ActiviteLucrative
            false,   // parent2ActiviteLucrative
            new BigDecimal("3000"), // parent1Salaire
            new BigDecimal("5000")  // parent2Salaire
    );
    String result = allocationService.getParentDroitAllocation(info);
    assertThat(result).isEqualTo("PARENT_2");
  }

  @Test
  void getParentDroitAllocation_BothActif_SalaireParent1PlusHaut_ReturnsParent1() {
    ParentsInfo info = createParentsInfo(
            true,   // parent1ActiviteLucrative
            true,   // parent2ActiviteLucrative
            new BigDecimal("6000"), // parent1Salaire
            new BigDecimal("4000")  // parent2Salaire
    );
    String result = allocationService.getParentDroitAllocation(info);
    assertThat(result).isEqualTo("PARENT_1");
  }

  @Test
  void getParentDroitAllocation_BothActif_SalaireParent2PlusHaut_ReturnsParent2() {
    ParentsInfo info = new ParentsInfo(
            true,   // parent1ActiviteLucrative
            true,   // parent2ActiviteLucrative
            new BigDecimal("4500"), // parent1Salaire
            new BigDecimal("7500"), // parent2Salaire
            true,   // autoriteParentalePartagee
            false,  // parentsEnsemble
            true,   // parent1VitAvecEnfant
            false,  // parent2VitAvecEnfant
            true,   // parent1TravailleDansCantonEnfant
            false   // parent2TravailleDansCantonEnfant
    );
    String result = allocationService.getParentDroitAllocation(info);
    assertThat(result).isEqualTo("PARENT_1");
  }

  @Test
  void getParentDroitAllocation_BothActif_SalairesEgaux_ReturnsParent2() {
    ParentsInfo info = createParentsInfo(
            true,   // parent1ActiviteLucrative
            true,   // parent2ActiviteLucrative
            new BigDecimal("5000"), // parent1Salaire
            new BigDecimal("5000")  // parent2Salaire
    );
    String result = allocationService.getParentDroitAllocation(info);
    assertThat(result).isEqualTo("PARENT_2");
  }

  @Test
  void getParentDroitAllocation_NullSalaireHandled_ReturnsParent2() {
    ParentsInfo info = createParentsInfo(
            false,   // parent1ActiviteLucrative
            false,   // parent2ActiviteLucrative
            null, // parent1Salaire
            null  // parent2Salaire
    );
    String result = allocationService.getParentDroitAllocation(info);
    assertThat(result).isEqualTo("PARENT_2");
  }

  @Test
  void getParentDroitAllocation_SalaireParent1NullParent2NonNull_ReturnsParent2() {
    ParentsInfo info = createParentsInfo(
            false,   // parent1ActiviteLucrative
            false,   // parent2ActiviteLucrative
            null, // parent1Salaire
            new BigDecimal("4000")  // parent2Salaire
    );
    String result = allocationService.getParentDroitAllocation(info);
    assertThat(result).isEqualTo("PARENT_2");
  }

  @Test
  void getParentDroitAllocation_SalaireParent2NullParent1NonNull_ReturnsParent1() {
    ParentsInfo info = createParentsInfo(
            false,   // parent1ActiviteLucrative
            false,   // parent2ActiviteLucrative
            new BigDecimal("4000"), // parent1Salaire
            null  // parent2Salaire
    );
    String result = allocationService.getParentDroitAllocation(info);
    assertThat(result).isEqualTo("PARENT_1");
  }


}