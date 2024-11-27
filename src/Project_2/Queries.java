package Project_2;

import java.sql.*;
import java.util.Scanner;

public class Queries {

    public static void viewTables(Connection connection, Scanner scanner) {
        try {
            boolean viewPublications = getYesNoInput(scanner, "PUBLICATIONS (Yes/No): ");
            boolean viewAuthors = getYesNoInput(scanner, "AUTHORS (Yes/No): ");

            if (!viewPublications && !viewAuthors) {
                System.out.println("You must select at least one table to view.");
                return;
            }

            if (viewPublications) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM PUBLICATIONS");

                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                System.out.println("\nPUBLICATIONS:");
                while (resultSet.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(metaData.getColumnName(i) + ": " + resultSet.getString(i) + ", ");
                    }
                    System.out.println();
                }
            }

            if (viewAuthors) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM AUTHORS");

                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                System.out.println("\nAUTHORS:");
                while (resultSet.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(metaData.getColumnName(i) + ": " + resultSet.getString(i) + ", ");
                    }
                    System.out.println();
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving table contents: " + e.getMessage());
        }
    }

    public static void searchByPublicationID(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter PUBLICATIONID:");
            int publicationId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            String query = "SELECT * FROM PUBLICATIONS WHERE PUBLICATIONID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, publicationId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                System.out.println("No records found for PUBLICATIONID: " + publicationId);
                return;
            }

            System.out.println("\nDetails:");
            while (resultSet.next()) {
                System.out.println("ID: " + resultSet.getInt("PUBLICATIONID"));
                System.out.println("Title: " + resultSet.getString("TITLE"));
                System.out.println("Year: " + resultSet.getInt("YEAR"));
                System.out.println("Type: " + resultSet.getString("TYPE"));
                System.out.println("Summary: " + resultSet.getString("SUMMARY"));
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving data: " + e.getMessage());
        }
    }

    public static void searchByAttributes(Connection connection, Scanner scanner) {
        try {
            // Input fields
            System.out.println("Input fields:");
            System.out.print("AUTHOR (leave blank to skip): ");
            String author = scanner.nextLine().trim();
            System.out.print("TITLE (leave blank to skip): ");
            String title = scanner.nextLine().trim();
            System.out.print("YEAR (leave blank to skip): ");
            String year = scanner.nextLine().trim();
            System.out.print("TYPE (leave blank to skip): ");
            String type = scanner.nextLine().trim();

            // Output fields
            System.out.println("Output fields:");
            boolean outputPublicationId = getYesNoInput(scanner, "PUBLICATIONID (Yes/No): ");
            boolean outputAuthor = getYesNoInput(scanner, "AUTHOR (Yes/No): ");
            boolean outputTitle = getYesNoInput(scanner, "TITLE (Yes/No): ");
            boolean outputYear = getYesNoInput(scanner, "YEAR (Yes/No): ");
            boolean outputType = getYesNoInput(scanner, "TYPE (Yes/No): ");
            boolean outputSummary = getYesNoInput(scanner, "SUMMARY (Yes/No): ");

            // Sorting is mandatory
            System.out.println("Sorting is mandatory.");
            String sortBy = getSortingField(scanner); // Get the sorting field (ascending by default)

            // Build query dynamically
            StringBuilder selectClause = new StringBuilder("SELECT ");
            if (outputPublicationId) selectClause.append("PUBLICATIONS.PUBLICATIONID, ");
            if (outputAuthor) selectClause.append("AUTHORS.AUTHOR, ");
            if (outputTitle) selectClause.append("PUBLICATIONS.TITLE, ");
            if (outputYear) selectClause.append("PUBLICATIONS.YEAR, ");
            if (outputType) selectClause.append("PUBLICATIONS.TYPE, ");
            if (outputSummary) selectClause.append("PUBLICATIONS.SUMMARY, ");
            if (selectClause.charAt(selectClause.length() - 2) == ',') {
                selectClause.setLength(selectClause.length() - 2);
            }

            StringBuilder query = new StringBuilder(selectClause.toString());
            query.append(" FROM PUBLICATIONS LEFT JOIN AUTHORS ON PUBLICATIONS.PUBLICATIONID = AUTHORS.PUBLICATIONID WHERE 1=1");

            if (!author.isEmpty()) query.append(" AND AUTHORS.AUTHOR LIKE '%").append(author).append("%'");
            if (!title.isEmpty()) query.append(" AND PUBLICATIONS.TITLE LIKE '%").append(title).append("%'");
            if (!year.isEmpty()) query.append(" AND PUBLICATIONS.YEAR = '").append(year).append("'");
            if (!type.isEmpty()) query.append(" AND PUBLICATIONS.TYPE LIKE '%").append(type).append("%'");

            // Append sorting clause
            query.append(" ORDER BY ").append(sortBy).append(" ASC");

            // Execute query
            System.out.println("Executing query: " + query);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query.toString());

            if (!resultSet.isBeforeFirst()) {
                System.out.println("No results found for the given criteria.");
                return;
            }

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(metaData.getColumnName(i) + ": " + resultSet.getString(i) + ",  ");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static boolean getYesNoInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Yes")) {
                return true;
            } else if (input.equalsIgnoreCase("No")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'Yes' or 'No'.");
            }
        }
    }

    private static String getSortingField(Scanner scanner) {
        while (true) {
            System.out.println("Choose a field to sort by:");
            System.out.println("1. AUTHOR");
            System.out.println("2. TITLE");
            System.out.println("3. YEAR");
            System.out.println("4. TYPE");
            System.out.print("Enter your choice (1-4): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    return "AUTHORS.AUTHOR";
                case "2":
                    return "PUBLICATIONS.TITLE";
                case "3":
                    return "PUBLICATIONS.YEAR";
                case "4":
                    return "PUBLICATIONS.TYPE";
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 4.");
            }
        }
    }
}
