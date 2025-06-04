package ch.hearc.cafheg.business.versements;

import static java.util.stream.Collectors.toMap;

import ch.hearc.cafheg.business.allocations.Allocataire;
import ch.hearc.cafheg.business.common.Montant;
import ch.hearc.cafheg.infrastructure.pdf.PDFExporter;
import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.VersementMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VersementService {

    private static final Logger log = LoggerFactory.getLogger(VersementService.class);

    private final VersementMapper versementMapper;
    private final AllocataireMapper allocataireMapper;
    private final PDFExporter pdfExporter;

    public VersementService(
            VersementMapper versementMapper,
            AllocataireMapper allocataireMapper,
            PDFExporter pdfExporter) {
        this.versementMapper = versementMapper;
        this.allocataireMapper = allocataireMapper;
        this.pdfExporter = pdfExporter;
    }

    public byte[] exportPDFVersements(long allocataireId) {
        log.info("Exporter le PDF des versements pour l'allocataire {}", allocataireId);
        List<VersementParentParMois> versementParentEnfantParMois = versementMapper
                .findVersementParentEnfantParMois();

        Map<LocalDate, Montant> montantParMois = versementParentEnfantParMois.stream()
                .filter(v -> v.getParentId() == allocataireId)
                .collect(toMap(VersementParentParMois::getMois,
                        v -> new Montant(v.getMontant().getValue()),
                        (v1, v2) -> new Montant(v1.value.add(v2.value))));

        Allocataire allocataire = allocataireMapper.findById(allocataireId);

        return pdfExporter.generatePDFVversement(allocataire, montantParMois);
    }

    public Montant findSommeAllocationNaissanceParAnnee(int year) {
        log.info("Rechercher la somme des allocations de naissances pour l'année {}", year);
        List<VersementAllocationNaissance> versements = versementMapper
                .findAllVersementAllocationNaissance();
        return VersementAllocationNaissance.sommeParAnnee(versements, year);
    }

    public Montant findSommeAllocationParAnnee(int year) {
        log.info("Rechercher la somme des allocations pour l'année {}", year);
        List<VersementAllocation> versements = versementMapper
                .findAllVersementAllocation();
        return VersementAllocation.sommeParAnnee(versements, year);
    }

    public byte[] exportPDFAllocataire(long allocataireId) {
        log.info("Exporter les PDF pour l'allocataire: {}", allocataireId);
        List<VersementParentEnfant> versements = versementMapper.findVersementParentEnfant();

        Map<Long, Montant> montantsParEnfant = versements.stream()
                .filter(v -> v.getParentId() == allocataireId)
                .collect(Collectors.toMap(VersementParentEnfant::getEnfantId,
                        VersementParentEnfant::getMontant, (v1, v2) -> v1));

        Allocataire allocataire = allocataireMapper.findById(allocataireId);

        return pdfExporter.generatePDFAllocataire(allocataire, montantsParEnfant);
    }


}
