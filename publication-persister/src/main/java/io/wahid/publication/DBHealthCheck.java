package io.wahid.publication;

import java.io.IOException;
import java.sql.*;
import java.sql.SQLException;
import java.util.List;

public class DBHealthCheck {

    public static void main(String[] args) throws IOException {
//        String url = "jdbc:h2:tcp://h2db:1521/publication-db;MODE=MySQL;DB_CLOSE_DELAY=-1;CACHE_SIZE=65536;LOCK_MODE=0"; // match your docker-compose service name
        String url = "jdbc:postgresql://db:5432/publication-db";
        String user = "wahid";
        String password = "anwar";
        System.out.println("Checking DB health!!");
        int retries = 5;
        int waitSeconds = 5;

        for (int i = 1; i <= retries; i++) {
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                System.out.println("DB is reachable!!");
                List<String> tables = List.of("PENDING_PUBLICATIONS", "AUTHORS", "PUBLICATIONMODEL", "BOOK", "MAGAZINE");
                for (String table : tables) {
                    new DBHealthCheck().checkTableExists(conn, table);
                }
                System.out.println("DB is healthy and all tables exists!!");
                System.exit(0);
            } catch (SQLException e) {
                System.err.printf("❌ Attempt %d/%d failed: %s%n", i, retries, e.getMessage());
                try {
                    Thread.sleep(waitSeconds * 1000L);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private void checkTableExists(Connection conn, String tableName) throws SQLException {
        ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null);
        if (!rs.next()) {
            System.out.println("Table " + tableName + " does not exist");
            System.exit(1);
        }
    }

    /*
    ResultSet rs = stmt.executeQuery("SELECT 1 FROM PENDING_PUBLICATIONS LIMIT 1");
            if (!rs.next()) {
                System.out.println("DB is healthy but schema does not exist");
                System.exit(1);
            }
            rs = stmt.executeQuery("SELECT 1 FROM AUTHORS LIMIT 1");
            if (!rs.next()) {
                System.out.println("DB is healthy but authors table does not exist");
                System.exit(1);
            }
            rs = stmt.executeQuery("SELECT 1 FROM PUBLICATIONMODEL LIMIT 1");
            if (!rs.next()) {
                System.out.println("DB is healthy but publicationmodel table does not exist");
                System.exit(1);
            }
            rs = stmt.executeQuery("SELECT 1 FROM BOOK LIMIT 1");
            if (!rs.next()) {
                System.out.println("DB is healthy but book table does not exist");
                System.exit(1);
            }
            rs = stmt.executeQuery("SELECT 1 FROM MAGAZINE LIMIT 1");
            if (!rs.next()) {
                System.out.println("DB is healthy but magazine does not exist");
                System.exit(1);
            }
     */
}
