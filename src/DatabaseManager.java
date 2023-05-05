import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vocabulary";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "local";

    private static final String CREATE_TABLE_SQL = "CREATE TABLE word_list (word_index INT PRIMARY KEY NOT NULL AUTO_INCREMENT, word VARCHAR(80), word_score INT)";
    private static final String INSERT_WORD_SQL = "INSERT INTO word_list (word, word_score) VALUES (?, 0)";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            // create table if it doesn't exist
            try (PreparedStatement createTableStmt = conn.prepareStatement(CREATE_TABLE_SQL)) {
                createTableStmt.execute();
            } catch (SQLException e) {
                System.out.println("Table already exists.");
            }

            // read words from file and insert into database
            try (BufferedReader reader = new BufferedReader(new FileReader("D:\\Shanto\\Shanto\\src\\Word.txt"));
                 PreparedStatement insertWordStmt = conn.prepareStatement(INSERT_WORD_SQL)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    insertWordStmt.setString(1, line);
                    insertWordStmt.executeUpdate();
                }
                System.out.println("Words inserted successfully.");
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
