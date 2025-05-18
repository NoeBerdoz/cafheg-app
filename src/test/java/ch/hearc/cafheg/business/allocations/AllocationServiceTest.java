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
    private ParentAllocRequest request;

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
    void givenParent1HasActivityAndParent2DoesNot_whenGetParentDroitAllocation_thenReturnParent1() {
        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", false, "Bienne", true,
                BigDecimal.ZERO, BigDecimal.ZERO, true, true, false, false, "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent1");
    }

    @Test
    void givenParent2HasActivityAndParent1DoesNot_whenGetParentDroitAllocation_thenReturnParent2() {
        request = new ParentAllocRequest(
                "Neuchâtel", false, "Neuchâtel", true, "Bienne", true,
                BigDecimal.ZERO, BigDecimal.ZERO, true, true, false, false, "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent2");
    }

    @Test
    void givenBothParentsHaveActivityAndParent1HasHigherSalary_whenGetParentDroitAllocation_thenReturnParent1() {
        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", true, "Bienne", true,
                new BigDecimal(3500), new BigDecimal(3000), true, true, false, false, "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent1");
    }

    @Test
    void givenBothParentsHaveActivityAndParent2HasHigherSalary_whenGetParentDroitAllocation_thenReturnParent2() {

        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", true, "Bienne", true,
                new BigDecimal(3000), new BigDecimal(3500), true, true, false, false, "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent2");
    }

    @Test
    void givenBothParentsHaveActivityAndEqualSalaries_whenGetParentDroitAllocation_thenReturnParent2() {
        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", true, "Bienne", true,
                new BigDecimal(3000), new BigDecimal(3000), true, true, false, false, "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent2");
    }

    @Test
    void givenNoParentHasActivity_whenGetParentDroitAllocation_thenReturnParent2() {

        request = new ParentAllocRequest(
                "Neuchâtel", false, "Neuchâtel", false, "Bienne", true,
                BigDecimal.ZERO, BigDecimal.ZERO, true, true, false, false, "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent2");
    }

    @Test
    void givenBothParentsActiveAndOnlyParent1HasAuthority_whenGetParentDroitAllocation_thenReturnParent1() {
        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", true, "Bienne", true,
                new BigDecimal(3000), new BigDecimal(4000), // Salaire Parent2 > Parent1
                true, false, false, false,
                "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent1");
    }

    @Test
    void givenOnlyParent1WorksInChildsCanton_whenGetParentDroitAllocation_thenReturnParent1() {
        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", true, "Bienne", true,
                new BigDecimal(3000), new BigDecimal(4000),
                true, true, false, false,
                "Neuchâtel", "Berne"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent1");
    }

    @Test
    void givenBothParentsIndependentAndParent2HasHigherSalary_whenGetParentDroitAllocation_thenReturnParent2() {
        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", true, "Neuchâtel", true,
                new BigDecimal(3000), new BigDecimal(4000),
                true, true, true, true,
                "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent2");
    }

}