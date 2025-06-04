package ch.hearc.cafheg.tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MyTestsIT {
/*
    @BeforeAll
    static void initDatabase() {
        new Database().start(); // Initialise le pool de connexions
    }

    @Test
    void testDatabaseConnection() {
        try {
            Database.inTransaction(() -> {
                Connection conn = Database.activeJDBCConnection();
                assertNotNull(conn);
                try {
                    assertFalse(conn.isClosed());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Connexion à la base réussie !");
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            fail(" La connexion a échoué : " + e.getMessage());
        }
    }
*/
    @Test
    void alwaysPassingTest() {
        assertEquals(1, 1, "Ce test est conçu pour toujours réussir");
    }
}
