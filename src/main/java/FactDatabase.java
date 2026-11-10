import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Runs all of the API's SQL against the database it was given. */
class FactDatabase {

    private final String dbUrl;

    FactDatabase(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    /** GET /facts */
    List<CatFact> getAllFacts() throws SQLException {
        List<CatFact> results = new ArrayList<>();
        String sql = "SELECT id, fact FROM facts ORDER BY id";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                results.add(new CatFact(rs.getInt("id"), rs.getString("fact")));
            }
        }
        return results;
    }

    /** GET /facts/{id} */
    Optional<CatFact> findFactById(int id) throws SQLException {
        String sql = "SELECT id, fact FROM facts WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new CatFact(rs.getInt("id"), rs.getString("fact")));
                }
            }
        }
        return Optional.empty();
    }

    /** POST /facts: saves the text and returns it with the id the database gave it. */
    CatFact insertFact(String text) throws SQLException {
        String sql = "INSERT INTO facts (fact) VALUES (?) RETURNING id";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, text);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return new CatFact(rs.getInt("id"), text);
            }
        }
    }
}
