package ch.hearc.cafheg.business.allocations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

import ch.hearc.cafheg.business.common.Montant;
import ch.hearc.cafheg.business.exceptions.AllocataireHasVersementsException;
import ch.hearc.cafheg.business.exceptions.AllocataireNotFoundException;
import ch.hearc.cafheg.business.exceptions.NoChangeToUpdateException;
import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.AllocationMapper;
import ch.hearc.cafheg.infrastructure.persistance.VersementMapper;

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
  private VersementMapper versementMapper;
    private ParentAllocRequest request;

  @BeforeEach
  void setUp() {
    allocataireMapper = Mockito.mock(AllocataireMapper.class);
    allocationMapper = Mockito.mock(AllocationMapper.class);
    versementMapper = Mockito.mock(VersementMapper.class);

    allocationService = new AllocationService(allocataireMapper, allocationMapper, versementMapper);
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
    ///////////////////////////
    // TESTS FROM EXERCISE 1
    ///////////////////////////
    @Test
    void getParenDroitAllocation_GivenParent1HasActivityAndParent2DoesNot_ShouldReturnParent1() {
        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", false, "Bienne", true,
                BigDecimal.ZERO, BigDecimal.ZERO, true, true, false, false, "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent1");
    }

    @Test
    void getParentDroitAllocation_GivenParent2HasActivityAndParent1DoesNot_ShouldReturnParent2 () {
        request = new ParentAllocRequest(
                "Neuchâtel", false, "Neuchâtel", true, "Bienne", true,
                BigDecimal.ZERO, BigDecimal.ZERO, true, true, false, false, "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent2");
    }

    @Test
    void getParentDroitAllocation_GivenBothParentsHaveActivityAndParent1HasHigherSalary_ShouldReturnParent1 () {
        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", true, "Bienne", true,
                new BigDecimal(3500), new BigDecimal(3000), true, true, false, false, "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent1");
    }

    @Test
    void getParentDroitAllocation_GivenBothParentsHaveActivityAndParent2HasHigherSalary_ShouldReturnParent2 () {

        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", true, "Bienne", true,
                new BigDecimal(3000), new BigDecimal(3500), true, true, false, false, "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent2");
    }

    @Test
    void getParentDroitAllocation_GivenBothParentsHaveActivityAndEqualSalaries_ShouldReturnParent2() {
        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", true, "Bienne", true,
                new BigDecimal(3000), new BigDecimal(3000), true, true, false, false, "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent2");
    }

    @Test
    void getParentDroitAllocation_GivenNoParentHasActivity_ShouldReturnParent2() {

        request = new ParentAllocRequest(
                "Neuchâtel", false, "Neuchâtel", false, "Bienne", true,
                BigDecimal.ZERO, BigDecimal.ZERO, true, true, false, false, "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent2");
    }

    @Test
    void getParentDroitAllocation_GivenBothParentsActiveAndOnlyParent1HasAuthority_ShouldReturnParent1() {
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
    void getParentDroitAllocation_GivenOnlyParent2HasAuthority_ShouldReturnParent2() {
        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", true, "Bienne", true,
                new BigDecimal(4000), new BigDecimal(3000),
                false, true, false, false,
                "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent2");
    }

    @Test
    void getParenDroitAllocation_GivenOnlyParent1WorksInChildCanton_ShouldReturnParent1() {
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
    void getParentDroitAllocation_GivenOnlyParent2WorksInChildCanton_ShouldReturnParent2() {
        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", true, "Neuchâtel", true,
                new BigDecimal(3000), new BigDecimal(4000),
                true, true, false, false,
                "Genève", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent2");
    }

    @Test
    void getParentDroitAllocation_GivenBothParentsIndependentAndParent2HasHigherSalary_ShouldReturnParent2() {
        request = new ParentAllocRequest(
                "Neuchâtel", true, "Neuchâtel", true, "Neuchâtel", true,
                new BigDecimal(3000), new BigDecimal(4000),
                true, true, true, true,
                "Neuchâtel", "Neuchâtel"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent2");
    }
    @Test
    void getParentDroitAllocation_GivenParentsSeparatedAndOnlyParent2LivesWithChild_ShouldReturnParent2() {
        request = new ParentAllocRequest(
                "Bienne", true, "Lausanne", true, "Bienne", false,
                new BigDecimal(3000), new BigDecimal(3000),
                true, true, false, false,
                "Berne", "Berne"
        );
        String result = allocationService.getParentDroitAllocation(request);
        assertThat(result).isEqualTo("Parent2");
    }

    ///////////////////////////
    // TESTS FROM EXERCISE 2
    ///////////////////////////

    @Test
    void deleteAllocataire_GivenAllocataireWithoutVersements_ShouldDelete() {
        long allocataireId = 1L;
        // Mock that the allocataire exists
        Mockito.when(allocataireMapper.findById(allocataireId)).thenReturn(new Allocataire(new NoAVS("AVS1"), "Nom", "Prenom"));
        // Mock that the allocataire has no versements
        Mockito.when(versementMapper.countVersementsByAllocataireId(allocataireId)).thenReturn(0L);
        // Mock deletion in mapper
        Mockito.when(allocataireMapper.deleteById(allocataireId)).thenReturn(true);

        assertDoesNotThrow(() -> allocationService.deleteAllocataire(allocataireId));

        Mockito.verify(allocataireMapper, times(1)).findById(allocataireId);
        Mockito.verify(versementMapper, times(1)).countVersementsByAllocataireId(allocataireId);
        Mockito.verify(allocataireMapper, times(1)).deleteById(allocataireId);
    }

    @Test
    void deleteAllocataire_GivenAllocataireWithVersements_ShouldTrowAllocataireHasVersementsException() {
        long allocataireId = 2L;
        // Mock that the allocataire exists
        Mockito.when(allocataireMapper.findById(allocataireId)).thenReturn(new Allocataire(new NoAVS("AVS2"), "Nom2", "Prenom2"));
        // Mock that the allocataire has 10 versements
        Mockito.when(versementMapper.countVersementsByAllocataireId(allocataireId)).thenReturn(10L);

        // Ensure that deleteAllocataire returns AllocataireHasVersementsException error
        assertThrows(
                AllocataireHasVersementsException.class,
                () -> allocationService.deleteAllocataire(allocataireId)
        );

        Mockito.verify(allocataireMapper, times(1)).findById(allocataireId);
        Mockito.verify(versementMapper, times(1)).countVersementsByAllocataireId(allocataireId);
        Mockito.verify(allocataireMapper, never()).deleteById(allocataireId);
    }

    @Test
    void deleteAllocataire_GivenNonExistentAllocataire_ShouldThrowAllocataireNotFoundException() {
        long allocataireId = 3L;
        // Mock a non existent allocataire
        Mockito.when(allocataireMapper.findById(allocataireId)).thenReturn(null);

        assertThrows(
                AllocataireNotFoundException.class,
                () -> allocationService.deleteAllocataire(allocataireId)
        );

        Mockito.verify(allocataireMapper, times(1)).findById(allocataireId);
        Mockito.verify(versementMapper, never()).countVersementsByAllocataireId(allocataireId);
        Mockito.verify(allocataireMapper, never()).deleteById(allocataireId);
    }

    @Test
    void updateAllocataire_GivenNameAndFirstNameChanged_ShouldUpdateAndReturnUpdatedAllocataire() {
        long allocataireId = 10L;
        NoAVS avs = new NoAVS("AVS10");
        Allocataire existingAllocataire = new Allocataire(avs, "OldNom", "OldPrenom");

        String newName = "NewNom";
        String newFirstname = "NewPrenom";

        Mockito.when(allocataireMapper.findById(allocataireId)).thenReturn(existingAllocataire);
        Mockito.when(allocataireMapper.updateNameAndFirstname(allocataireId, newName, newFirstname)).thenReturn(true);

        Allocataire result = allocationService.updateAllocataire(allocataireId, newName, newFirstname);

        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo(newName);
        assertThat(result.getPrenom()).isEqualTo(newFirstname);
        assertThat(result.getNoAVS()).isEqualTo(avs); // Ensure AVS is never changed

        Mockito.verify(allocataireMapper, times(1)).findById(allocataireId);
        Mockito.verify(allocataireMapper, times(1)).updateNameAndFirstname(allocataireId, newName, newFirstname);
    }

    @Test
    void updateAllocataire_GivenNewNameAndExistingFirstname_ShouldUpdateSuccessfullyAndReturnUpdatedAllocataire() {
        long allocataireId = 11L;
        NoAVS avs = new NoAVS("AVS11");
        String existingFirstname = "Prenom";
        Allocataire existingAllocataire = new Allocataire(avs, "OldNom", existingFirstname);

        String newName = "NewNom";

        Mockito.when(allocataireMapper.findById(allocataireId)).thenReturn(existingAllocataire);
        Mockito.when(allocataireMapper.updateNameAndFirstname(allocataireId, newName, existingFirstname)).thenReturn(true);

        Allocataire result = allocationService.updateAllocataire(allocataireId, newName, existingFirstname);

        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo(newName);
        assertThat(result.getPrenom()).isEqualTo(existingFirstname);
        assertThat(result.getNoAVS()).isEqualTo(avs); // Ensure AVS is never changed

        Mockito.verify(allocataireMapper, times(1)).findById(allocataireId);
        Mockito.verify(allocataireMapper, times(1)).updateNameAndFirstname(allocataireId, newName, existingFirstname);
    }

    @Test
    void updateAllocataire_GivenExistingNameAndNewFirstname_ShouldUpdateSuccessfullyAndReturnUpdatedAllocataire() {
        long allocataireId = 11L;
        NoAVS avs = new NoAVS("AVS11");
        String existingName = "Nom";
        Allocataire existingAllocataire = new Allocataire(avs, existingName, "oldFirstname");

        String newFirstname = "newFirstname";

        Mockito.when(allocataireMapper.findById(allocataireId)).thenReturn(existingAllocataire);
        Mockito.when(allocataireMapper.updateNameAndFirstname(allocataireId, existingName, newFirstname)).thenReturn(true);

        Allocataire result = allocationService.updateAllocataire(allocataireId, existingName, newFirstname);

        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo(existingName);
        assertThat(result.getPrenom()).isEqualTo(newFirstname);
        assertThat(result.getNoAVS()).isEqualTo(avs); // Ensure AVS is never changed

        Mockito.verify(allocataireMapper, times(1)).findById(allocataireId);
        Mockito.verify(allocataireMapper, times(1)).updateNameAndFirstname(allocataireId, existingName, newFirstname);
    }

    @Test
    void updateAllocataire_GivenNoChangesInNameAndFirstName_ShouldThrowNoChangeToUpdateException() {
        long allocataireId = 13L;
        String existingName = "sameNom";
        String existingFirstname = "sameFirstname";
        Allocataire existingAllocataire = new Allocataire(new NoAVS("AVS13"), existingName, existingFirstname);

        Mockito.when(allocataireMapper.findById(allocataireId)).thenReturn(existingAllocataire);

        NoChangeToUpdateException exception = assertThrows(NoChangeToUpdateException.class,
                () -> allocationService.updateAllocataire(allocataireId, existingName, existingFirstname));

        Mockito.verify(allocataireMapper, times(1)).findById(allocataireId);
        Mockito.verify(allocataireMapper, never()).updateNameAndFirstname(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void updateAllocataire_GivenNonExistentAllocataire_ShouldThrowAllocataireNotFoundException() {
        long allocataireId = 14L;
        Mockito.when(allocataireMapper.findById(allocataireId)).thenReturn(null);

        AllocataireNotFoundException exception = assertThrows(AllocataireNotFoundException.class,
                () -> allocationService.updateAllocataire(allocataireId, "newNom", "newPrenom"));

        Mockito.verify(allocataireMapper, times(1)).findById(allocataireId);
        Mockito.verify(allocataireMapper, never()).updateNameAndFirstname(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void updateAllocataire_GivenNullNameInRequest_ShouldThrowIllegalArgumentException() {
        long allocataireId = 15L;
        Mockito.when(allocataireMapper.findById(allocataireId)).thenReturn(new Allocataire(new NoAVS("AVS15"), "oldNom", "oldPrenom"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> allocationService.updateAllocataire(allocataireId, null, "newPrenom"));
        Mockito.verify(allocataireMapper, never()).updateNameAndFirstname(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void updateAllocataire_GivenNullFirstnameInRequest_ShouldThrowIllegalArgumentException() {
        long allocataireId = 15L;
        Mockito.when(allocataireMapper.findById(allocataireId)).thenReturn(new Allocataire(new NoAVS("AVS15"), "oldNom", "oldPrenom"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> allocationService.updateAllocataire(allocataireId, "newNom", null));
        Mockito.verify(allocataireMapper, never()).updateNameAndFirstname(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void updateAllocataire_GivenEmptyNameRequest_ShouldThrowIllegalArgumentException() {
        long allocataireId = 16L;
        Mockito.when(allocataireMapper.findById(allocataireId)).thenReturn(new Allocataire(new NoAVS("AVS16"), "oldNom", "oldPrenom"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> allocationService.updateAllocataire(allocataireId, " ", "newPrenom"));
        Mockito.verify(allocataireMapper, never()).updateNameAndFirstname(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void updateAllocataire_GivenEmptyFirstnameRequest_ShouldThrowIllegalArgumentException() {
        long allocataireId = 16L;
        Mockito.when(allocataireMapper.findById(allocataireId)).thenReturn(new Allocataire(new NoAVS("AVS16"), "oldNom", "oldPrenom"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> allocationService.updateAllocataire(allocataireId, "newNom", "  "));
        Mockito.verify(allocataireMapper, never()).updateNameAndFirstname(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

}