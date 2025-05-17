package ch.hearc.cafheg.business.allocations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

import ch.hearc.cafheg.business.common.Montant;
import ch.hearc.cafheg.business.exceptions.AllocataireHasVersementsException;
import ch.hearc.cafheg.business.exceptions.AllocataireNotFoundException;
import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.AllocationMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.hearc.cafheg.infrastructure.persistance.VersementMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AllocationServiceTest {

  private AllocationService allocationService;

  private AllocataireMapper allocataireMapper;
  private AllocationMapper allocationMapper;
  private VersementMapper versementMapper;

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

}