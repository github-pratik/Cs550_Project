package Project_2;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {

    public static Connection connect(String username, String password) throws SQLException {
        String driverPrefixURL = "jdbc:oracle:thin:@";
        String jdbcUrl = "artemis.vsnet.gmu.edu:1521/vse18c.vsnet.gmu.edu";

        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (SQLException e) {
            throw new SQLException("Failed to load Oracle JDBC driver.");
        }

        return DriverManager.getConnection(driverPrefixURL + jdbcUrl, username, password);
    }

    public static void executeScript(Connection connection, String scriptPath) throws IOException, SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not initialized.");
        }

        File file = new File(scriptPath);
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("The file at " + scriptPath + " does not exist.");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            Statement statement = connection.createStatement();

            System.out.println("Executing SQL script...");
            Thread loadingThread = startLoadingAnimation(); // Start the loading animation

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }

                // Add the line to the current SQL command
                sqlBuilder.append(line).append(" ");

                // If the line ends with a semicolon, execute the SQL command
                if (line.endsWith(";")) {
                    String sql = sqlBuilder.toString().replace(";", "").trim(); // Remove semicolon
                    try {
                        statement.execute(sql);
                        System.out.println("Executed: " + sql); // Log successful execution
                    } catch (SQLException e) {
                        // Handle specific SQL errors (e.g., table does not exist)
                        System.err.println("Error executing SQL: " + sql);
                        System.err.println("Skipping this statement due to: " + e.getMessage());
                    }
                    sqlBuilder.setLength(0); // Reset for the next command
                }
            }

            stopLoadingAnimation(loadingThread); // Stop the loading animation
            System.out.println("SQL script executed successfully.");
        }
    }


    private static Thread startLoadingAnimation() {
        Thread thread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.print("."); // Print a dot for the loading effect
                    Thread.sleep(500); // Wait for 500ms
                }
            } catch (InterruptedException ignored) {
                // Thread interrupted, exit gracefully
            }
        });
        thread.start(); // Start the loading thread
        return thread;
    }

    private static void stopLoadingAnimation(Thread thread) {
        if (thread != null) {
            thread.interrupt(); // Interrupt the loading thread
            try {
                thread.join(); // Wait for the thread to finish
            } catch (InterruptedException ignored) {
            }
            System.out.println(); // Print a new line after the animation stops
        }
    }

}
