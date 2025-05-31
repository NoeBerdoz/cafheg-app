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
  private DroitsAllocations droitsAllocations;

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
void givenParent1ActiveAndParent2Inactive_ShouldReturnParent1() {
  droitsAllocations = new DroitsAllocations(true, false, true, true, true,
          "Neuchâtel", true, false, "Neuchâtel", "Neuchâtel", Canton.NE, Canton.NE, Canton.NE,
          false, false, new BigDecimal(5000), new BigDecimal(0)
  );
  String result = allocationService.getParentDroitAllocation(droitsAllocations);
  assertThat(result).isEqualTo("Parent1");
}

  //1 - 4 OK
  @Test
  void givenParent1InactiveAndParent2Active_ShouldReturnParent2() {
    droitsAllocations = new DroitsAllocations(false, true, true, true, true,
            "Neuchâtel", false, true, "Neuchâtel", "Bienne", Canton.NE, Canton.NE, Canton.NE,
            false, false, new BigDecimal(0), new BigDecimal(5000)
    );
    String result = allocationService.getParentDroitAllocation(droitsAllocations);
    assertThat(result).isEqualTo("Parent2");
  }

  @Test
  void givenParent1HasAuthority_ShouldReturnParent1() {
    droitsAllocations = new DroitsAllocations(true, true, true, false, false,
            "Neuchâtel", true, false, "Neuchâtel", "Bienne", Canton.NE, Canton.NE, Canton.BE,
            false, false, new BigDecimal(5000), new BigDecimal(4000));
    String result = allocationService.getParentDroitAllocation(droitsAllocations);
    assertThat(result).isEqualTo("Parent1");
  }

  @Test
  void givenParent2HasAuthority_ShouldReturnParent2() {
    droitsAllocations = new DroitsAllocations(true, true, false, true, true,
            "Bienne", false, true, "Neuchâtel", "Bienne", Canton.BE, Canton.NE, Canton.BE,
            false, false, new BigDecimal(4000), new BigDecimal(5000));
    String result = allocationService.getParentDroitAllocation(droitsAllocations);
    assertThat(result).isEqualTo("Parent2");
  }

  @Test
  void givenBothParentHaveAuthorityAndAreSeparated_ShouldReturnParent1WhoLiveWithChild() {
    droitsAllocations = new DroitsAllocations(true, true, true, true, false,
            "Neuchâtel", true, false, "Neuchâtel", "Bienne", Canton.NE, Canton.NE, Canton.BE,
            false, false, new BigDecimal(5000), new BigDecimal(4000));
    String result = allocationService.getParentDroitAllocation(droitsAllocations);
    assertThat(result).isEqualTo("Parent1");
  }

  @Test
    void givenBothParentsHaveAuthorityAndAreSeparated_ShouldReturnParent2WhoLiveWithChild() {
        droitsAllocations = new DroitsAllocations(true, true, true, true, false,
                "Bienne", false, true, "Neuchâtel", "Bienne", Canton.BE, Canton.NE, Canton.BE,
                false, false, new BigDecimal(4000), new BigDecimal(5000));
        String result = allocationService.getParentDroitAllocation(droitsAllocations);
        assertThat(result).isEqualTo("Parent2");
    }

  @Test
  void givenBothParentsLiveTogether_ShouldReturnParent1WhoWorkInLivingCanton() {
    droitsAllocations = new DroitsAllocations(true, true, true, true, true,
            "Neuchâtel", true, true, "Neuchâtel", "Neuchâtel", Canton.NE, Canton.NE, Canton.BE,
            false, false, new BigDecimal(4000), new BigDecimal(5000));
    String result = allocationService.getParentDroitAllocation(droitsAllocations);
    assertThat(result).isEqualTo("Parent1");
  }

  @Test
  void givenBothParentsLiveTogether_ShouldReturnParent2WhoWorkInLivingCanton() {
    droitsAllocations = new DroitsAllocations(true, true, true, true, true,
            "Neuchâtel", true, true, "Neuchâtel", "Neuchâtel", Canton.NE, Canton.BE, Canton.NE,
            false, false, new BigDecimal(4000), new BigDecimal(5000));
    String result = allocationService.getParentDroitAllocation(droitsAllocations);
    assertThat(result).isEqualTo("Parent2");
  }

  @Test
    void givenBothParentsActiveAndParent2IsIndependant_ShouldReturnParent1() {
        droitsAllocations = new DroitsAllocations(true, true, true, true, true,
                "Neuchâtel", true, true, "Neuchâtel", "Neuchâtel", Canton.NE, Canton.BE, Canton.BE,
                false, true, new BigDecimal(4000), new BigDecimal(5000));
        String result = allocationService.getParentDroitAllocation(droitsAllocations);
        assertThat(result).isEqualTo("Parent1");
    }

  @Test
    void givenBothParentsActiveAndParent1IsIndependant_ShouldReturnParent2() {
        droitsAllocations = new DroitsAllocations(true, true, true, true, true,
                "Neuchâtel", true, true, "Neuchâtel", "Neuchâtel", Canton.NE, Canton.BE, Canton.BE,
                true, false, new BigDecimal(4000), new BigDecimal(5000));
        String result = allocationService.getParentDroitAllocation(droitsAllocations);
        assertThat(result).isEqualTo("Parent2");
    }

  @Test
  void givenBothParentsEmployed_AndParent1EarnMore_ShouldReturnParent1() {
    droitsAllocations = new DroitsAllocations(true, true, true, true, true,
            "Neuchâtel", true, true, "Neuchâtel", "Neuchâtel", Canton.NE, Canton.BE, Canton.BE,
            false, false, new BigDecimal(5000), new BigDecimal(4000));
    String result = allocationService.getParentDroitAllocation(droitsAllocations);
    assertThat(result).isEqualTo("Parent1");
  }

  @Test
  void givenBothParentsEmployed_AndParent2EarnMore_ShouldReturnParent2() {
    droitsAllocations = new DroitsAllocations(true, true, true, true, true,
            "Neuchâtel", true, true, "Neuchâtel", "Neuchâtel", Canton.NE, Canton.BE, Canton.BE,
            false, false, new BigDecimal(4000), new BigDecimal(5000));
    String result = allocationService.getParentDroitAllocation(droitsAllocations);
    assertThat(result).isEqualTo("Parent2");
  }

  @Test
  void givenBothParentsIndependant_AndParent1EarnMore_ShouldReturnParent1() {
    droitsAllocations = new DroitsAllocations(true, true, true, true, true,
            "Neuchâtel", true, true, "Neuchâtel", "Neuchâtel", Canton.NE, Canton.BE, Canton.BE,
            true, true, new BigDecimal(5000), new BigDecimal(4000));
    String result = allocationService.getParentDroitAllocation(droitsAllocations);
    assertThat(result).isEqualTo("Parent1");
  }

  @Test
  void givenBothParentsIndependant_AndParent2EarnMore_ShouldReturnParent2() {
    droitsAllocations = new DroitsAllocations(true, true, true, true, true,
            "Neuchâtel", true, true, "Neuchâtel", "Neuchâtel", Canton.NE, Canton.BE, Canton.BE,
            true, true, new BigDecimal(4000), new BigDecimal(5000));
    String result = allocationService.getParentDroitAllocation(droitsAllocations);
    assertThat(result).isEqualTo("Parent2");
  }

}