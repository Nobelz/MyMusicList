import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class MainCLI {
    private static final String connectionURL =
            "jdbc:sqlserver://localhost;"
            + "database=MyMusicList;"
            + "user=dbuser;"
            + "password=Nobelium;"
            + "encrypt=true;"
            + "trustServerCertificate=true;"
            + "loginTimeout=10;";

    private static final Scanner scanner = new Scanner(System.in);

    // TODO Attribution required https://stackoverflow.com/questions/2979383/how-to-clear-the-console
    private static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows"))
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            else
                Runtime.getRuntime().exec("clear");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Connecting to the MyMusicList Database...");

        try (Connection connection = DriverManager.getConnection(connectionURL)) {
            System.out.println("Connected to MyMusicList. Press ENTER to continue.");
            scanner.nextLine();
            clearConsole();

            mainMenu(connection);
        } catch (SQLException e) {
            System.out.println("Failed to connect to database. Exiting...");
            e.printStackTrace();
        }
        scanner.close();
    }

    private static void mainMenu(Connection connection) {
        // Main Menu Screen
        clearConsole();
        System.out.println("MyMusicList");
        System.out.println("1: Login with existing user");
        System.out.println("2: Register new user");
        System.out.println("3: Exit");

        try {
            int input = scanner.nextInt();
            if (input != 1 && input != 2 && input != 3)
                throw new InputMismatchException("Incorrect input given");
            clearConsole();

            if (input == 1)
                loginScreen(connection);
            else if (input == 2)
                registerScreen(connection);
            else
                System.exit(0); // Exits program

        } catch (Exception e) {
            System.out.println("Incorrect data entered. Try Again.");
            scanner.nextLine();
            mainMenu(connection);
        }
    }

    private static void loginScreen(Connection connection) {
        clearConsole();
        System.out.println("MyMusicList Login");
        System.out.println("Please enter your username: ");
        String username = scanner.nextLine();

        try {
            String sql = "SELECT dbo.login_with_username('?');";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, username);
            preparedStatement.execute();

            ResultSet resultSet = preparedStatement.getResultSet();
            if (resultSet.next()) {
                System.out.println(resultSet.getString(1));
            } else
                throw new Exception("Username incorrect or not found.");
        } catch (SQLException e) {
            System.out.println("Problems with SQL JDBC. Returning to Main Menu.");
            scanner.nextLine();
            mainMenu(connection);
        } catch (Exception e) {
            System.out.println("Username was incorrect or not found. Returning to Main Menu.");
            scanner.nextLine();
            mainMenu(connection);
        }
    }

    private static void registerScreen(Connection connection) {

    }
}