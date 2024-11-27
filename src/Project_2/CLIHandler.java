package Project_2;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class CLIHandler {
    private Connection connection;

    public void start() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            try {
                // Step 1: Prompt for Oracle username and password
                System.out.println("Enter Oracle username:");
                String username = scanner.nextLine();

                System.out.println("Enter Oracle password:");
                String password = scanner.nextLine();

                // Step 2: Attempt to connect to the database
                connection = DatabaseConnector.connect(username, password); // Use class-level connection
                System.out.println("Connected to the database.");
                break; // Exit the loop if connection is successful

            } catch (SQLException e) {
                System.out.println(e.getMessage()); // Display error message
                if (e.getErrorCode() == 1017) { // ORA-01017: Invalid username/password
                    System.out.println("Do you want to try again?");
                    System.out.println("1. Yes");
                    System.out.println("2. Exit");
                    String choice = scanner.nextLine();

                    if (choice.equals("2")) {
                        System.out.println("Exiting the program.");
                        return; // Exit the program
                    }
                } else {
                    e.printStackTrace(); // For unexpected errors, print stack trace
                    return; // Exit the program for other errors
                }
            }
        }

        try {
            // Step 3: Proceed with the program if the connection is successful
            System.out.println("Enter the full path to the paper.sql file:");
            String scriptPath = scanner.nextLine();
            DatabaseConnector.executeScript(connection, scriptPath); // Pass the connection explicitly

            // Display the menu for user operations
            boolean running = true;
            while (running) {
                printMenu();
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        Queries.viewTables(connection, scanner);
                        break;
                    case 2:
                        Queries.searchByPublicationID(connection, scanner);
                        break;
                    case 3:
                        Queries.searchByAttributes(connection, scanner);
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close connection on exit
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("Connection closed.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. View table contents");
        System.out.println("2. Search by PUBLICATIONID");
        System.out.println("3. Search by attributes");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");
    }
}
