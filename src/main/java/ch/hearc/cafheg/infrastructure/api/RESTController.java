package ch.hearc.cafheg.infrastructure.api;

import static ch.hearc.cafheg.infrastructure.persistance.Database.inTransaction;

import ch.hearc.cafheg.business.allocations.Allocataire;
import ch.hearc.cafheg.business.allocations.Allocation;
import ch.hearc.cafheg.business.allocations.AllocationService;
import ch.hearc.cafheg.business.exceptions.AllocataireHasVersementsException;
import ch.hearc.cafheg.business.exceptions.AllocataireNotFoundException;
import ch.hearc.cafheg.business.exceptions.NoChangeToUpdateException;
import ch.hearc.cafheg.business.allocations.ParentAllocRequest;
import ch.hearc.cafheg.business.versements.VersementService;
import ch.hearc.cafheg.infrastructure.pdf.PDFExporter;
import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.AllocationMapper;
import ch.hearc.cafheg.infrastructure.persistance.EnfantMapper;
import ch.hearc.cafheg.infrastructure.persistance.VersementMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
public class RESTController {

  private final AllocationService allocationService;
  private final VersementService versementService;

  public RESTController() {
    this.allocationService = new AllocationService(new AllocataireMapper(), new AllocationMapper(), new VersementMapper());
    this.versementService = new VersementService(new VersementMapper(), new AllocataireMapper(),
        new PDFExporter(new EnfantMapper()));
  }

    /*
    // Headers de la requête HTTP doit contenir "Content-Type: application/json"
    // BODY de la requête HTTP à transmettre afin de tester le endpoint
    {
        "enfantResidence": "Neuchâtel",
        "parent1Residence": "Neuchâtel",
        "parent2Residence": "Bienne",
        "parent1ActiviteLucrative": true,
        "parent2ActiviteLucrative": true,
        "parent1Salaire": 3000,
        "parent2Salaire": 4000,
        "parent1AutoriteParentale": true,
        "parent2AutoriteParentale": false,
        "parentsEnsemble": false,
        "parent1EstIndependant": true,
        "parent2EstIndependant": false,
        "parent1CantonTravail": "Neuchâtel",
        "parent2CantonTravail": "Neuchâtel"
    }
     */
    @PostMapping("/droits/quel-parent")
    public String getParentDroitAllocation(@RequestBody ParentAllocRequest request) {
        return inTransaction(() -> allocationService.getParentDroitAllocation(request));
    }

    @GetMapping("/allocataires")
    public List<Allocataire> allocataires(
            @RequestParam(value = "startsWith", required = false) String start) {
        return inTransaction(() -> allocationService.findAllAllocataires(start));
    }

    @GetMapping("/allocations")
    public List<Allocation> allocations() {
        return inTransaction(allocationService::findAllocationsActuelles);
    }

    @GetMapping("/allocations/{year}/somme")
    public BigDecimal sommeAs(@PathVariable("year") int year) {
        return inTransaction(() -> versementService.findSommeAllocationParAnnee(year).getValue());
    }

    @GetMapping("/allocations-naissances/{year}/somme")
    public BigDecimal sommeAns(@PathVariable("year") int year) {
        return inTransaction(
                () -> versementService.findSommeAllocationNaissanceParAnnee(year).getValue());
    }

    @GetMapping(value = "/allocataires/{allocataireId}/allocations", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] pdfAllocations(@PathVariable("allocataireId") int allocataireId) {
        return inTransaction(() -> versementService.exportPDFAllocataire(allocataireId));
    }

  @GetMapping(value = "/allocataires/{allocataireId}/versements", produces = MediaType.APPLICATION_PDF_VALUE)
  public byte[] pdfVersements(@PathVariable("allocataireId") int allocataireId) {
    return inTransaction(() -> versementService.exportPDFVersements(allocataireId));
  }

  /**
   * Handles DELETE requests to remove a specific allocataire.
   * The allocataire is identified by {allocataireId} in the path.
   *
   * <p>This operation is conditional:
   * <ul>
   * <li>The allocataire must exist.
   * <li>The allocataire must not have any associated versements.
   * </ul>
   *
   * <p>Responds with:
   * <ul>
   * <li>204 No Content on successful deletion.
   * <li>404 Not Found if the allocataire with the given ID does not exist.
   * <li>409 Conflict if the allocataire cannot be deleted due to existing versements.
   * <li>500 Internal Server Error for other unexpected issues.
   * </ul>
   */
  @DeleteMapping("/allocataires/{allocataireId}")
  public ResponseEntity<Void> deleteAllocataire(@PathVariable long allocataireId) {
    try {
      inTransaction(
              () -> {
                allocationService.deleteAllocataire(allocataireId);
                return null; // Void for supplier
              });
              return ResponseEntity.noContent().build();
    } catch (AllocataireNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (AllocataireHasVersementsException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (RuntimeException e) {
      System.out.println("Erreur lors de la suppression d'un allocataire : " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

/**
 * Handles PUT requests to update the last name and first name of an allocataire.
 * The allocataire is identified by {allocataireId} in the path.
 * The request body should be a JSON object containing 'lastname'
 * and 'firstname'.
 *
 * <p>This operation is conditional:
 * <ul>
 * <li>The allocataire must exist.
 * <li>At least one of the names (last or first) must be different from the current values.
 * <li>The AVS number is not modified.
 * </ul>
 *
 * <p>Responds with:
 * <ul>
 * <li>200 OK and the updated allocataire data on success.
 * <li>400 Bad Request if the input is invalid (e.g., missing names, no changes detected).
 * <li>404 Not Found if the allocataire with the given ID does not exist.
 * <li>500 Internal Server Error for other unexpected issues.
 * </ul>
 */
  @PutMapping("/allocataires/{allocataireId}")
  public ResponseEntity<?> updateAllocataire(
          @PathVariable long allocataireId,
          @RequestBody Map<String, String> updatePayload
  ) {
    try {
      String newNom = updatePayload.get("lastname");
      String newPrenom = updatePayload.get("firstname");

      if (newNom == null || newPrenom == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Les champs 'lastname' et 'firstname' sont requis.");
      }

      Allocataire updatedAllocataire = inTransaction(
              () -> allocationService.updateAllocataire(allocataireId, newNom, newPrenom)
      );
      return ResponseEntity.ok(updatedAllocataire);
    } catch (AllocataireNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (NoChangeToUpdateException | IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } catch (RuntimeException e) {
      System.out.println("Erreur lors de la mise à jour d'un allocataire : " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
