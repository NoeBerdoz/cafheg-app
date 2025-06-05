package ch.hearc.cafheg.tests;

import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.Database;
import ch.hearc.cafheg.business.allocations.Allocataire;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocataireMapperIT {

    private static IDatabaseTester databaseTester;
    private final AllocataireMapper mapper = new AllocataireMapper();
    // Ajout du service pour les tests
    private final ch.hearc.cafheg.business.allocations.AllocationService service =
        new ch.hearc.cafheg.business.allocations.AllocationService(
            mapper,
            new ch.hearc.cafheg.infrastructure.persistance.AllocationMapper(),
            new ch.hearc.cafheg.infrastructure.persistance.VersementMapper()
        );

    @BeforeAll
    static void initDb() throws Exception {
        // Exécution explicite du script de création de tables avant d'initialiser DbUnit
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:sample;DB_CLOSE_DELAY=-1", "", "");
             Statement stmt = conn.createStatement();
             InputStream is = AllocataireMapperIT.class.getClassLoader().getResourceAsStream("db/ddl/V1__ddl.sql")) {
            if (is == null) throw new RuntimeException("Script SQL V1__ddl.sql introuvable dans les ressources !");
            String sql = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            for (String s : sql.split(";")) {
                if (!s.trim().isEmpty()) stmt.execute(s);
            }
        }
        new Database().start();
        databaseTester = new JdbcDatabaseTester("org.h2.Driver", "jdbc:h2:mem:sample;DB_CLOSE_DELAY=-1", "", "");
    }

    @BeforeEach
    void setupDataset() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dataset.xml");
        var dataSet = new FlatXmlDataSetBuilder().build(is); // fichier dans /resources
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
    }

    @Test
    void testSupprimerAllocataireSansVersement() {
        Database.inTransaction(() -> {
            // On récupère l'allocataire pour obtenir son ID (normalisation du format AVS)
            Allocataire allocataire = service.findAllAllocataires(null).stream()
                .filter(a -> a.getNoAVS().getValue().replace(".", "").equals("7561234567890"))
                .findFirst().orElse(null);
            assertThat(allocataire).isNotNull();
            // On tente la suppression via le service
            service.deleteAllocataire(1L); // NUMERO=1 dans dataset.xml
            // Vérifie que l'allocataire n'existe plus
            Allocataire result = service.findAllAllocataires(null).stream()
                .filter(a -> a.getNoAVS().getValue().replace(".", "").equals("7561234567890"))
                .findFirst().orElse(null);
            assertThat(result).isNull();
            return null;
        });
    }

    @Test
    void testModifierNomAllocataire() {
        Database.inTransaction(() -> {
            Allocataire a = service.findAllAllocataires(null).stream()
                .filter(x -> x.getNoAVS().getValue().replace(".", "").equals("7569876543210"))
                .findFirst().orElse(null);
            assertThat(a).isNotNull();
            // Modification via le service
            Allocataire modifie = service.updateAllocataire(2L, "Dupont", a.getPrenom()); // NUMERO=2 dans dataset.xml
            assertThat(modifie).isNotNull();
            assertThat(modifie.getNom()).isEqualTo("Dupont");
            // Vérification en base via le service
            Allocataire verif = service.findAllAllocataires(null).stream()
                .filter(x -> x.getNoAVS().getValue().replace(".", "").equals("7569876543210"))
                .findFirst().orElse(null);
            assertThat(verif).isNotNull();
            assertThat(verif.getNom()).isEqualTo("Dupont");
            return null;
        });
    }
}
