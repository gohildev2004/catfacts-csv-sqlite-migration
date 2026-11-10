import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * One-shot program: creates the facts table and copies every fact from
 * facts.csv into it. Run it on its own; it is not part of the API.
 */
public class MigrateCsvToDb {

    static final String DB_URL = "jdbc:sqlite:catfacts.db";
    static final Path CSV_FILE = Path.of("facts.csv");

    static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS facts (
            id   INTEGER PRIMARY KEY AUTOINCREMENT,
            fact TEXT    NOT NULL
        )
        """;

    static final List<CatFact> facts = new ArrayList<>();
    static int nextId = 1;

    public static void main(String[] args) throws IOException, SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            createTable(conn);
            System.out.println("Table ready");
            copyFacts(conn);
        }
    }

    /** Reads every line of facts.csv into the in-memory list. */
    static void loadFromCsv() throws IOException {
        for (String line : Files.readAllLines(CSV_FILE, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            // Limit of 2: split at the FIRST comma only, so commas in the fact survive.
            String[] parts = line.split(",", 2);
            int id = Integer.parseInt(parts[0].trim());
            String fact = parts[1].trim();
            facts.add(new CatFact(id, fact));
            nextId = Math.max(nextId, id + 1);
        }
    }

    /** Section 2: creates the facts table. */
    static void createTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(CREATE_TABLE);
        }
    }

    /** Section 3: copies every fact from facts.csv into the facts table, as one transaction. */
    static void copyFacts(Connection conn) throws IOException, SQLException {
        loadFromCsv();
        System.out.println("Read " + facts.size() + " facts from facts.csv");

        conn.setAutoCommit(false);

        String sql = "INSERT INTO facts (id, fact) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int inserted = 0;
            for (CatFact fact : facts) {
                pstmt.setInt(1, fact.id());
                pstmt.setString(2, fact.fact());
                inserted += pstmt.executeUpdate();
            }
            conn.commit();
            System.out.println("Migrated " + inserted + " facts");
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Migration failed, rolled back. Nothing was saved.");
            throw e;
        }
    }
}
