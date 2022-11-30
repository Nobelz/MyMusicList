import java.sql.*;
import java.util.InputMismatchException;
import java.util.LinkedList;
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
            e.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {
        System.out.println("Connecting to the MyMusicList Database...");

        try (Connection connection = DriverManager.getConnection(connectionURL)) {
            System.out.println("Connected to MyMusicList. Press ENTER to continue.");
            scanner.nextLine();
            clearConsole();

            loginMenu(connection);
        } catch (SQLException e) {
            System.out.println("Failed to connect to database. Exiting...");
            e.printStackTrace(System.err);
        }
        scanner.close();
    }

    private static void loginMenu(Connection connection) {
        // Main Menu Screen
        clearConsole();
        System.out.println("MyMusicList Login Menu");
        System.out.println("1: Login with existing user");
        System.out.println("2: Register new user");
        System.out.println("3: Exit");

        try {
            int input = scanner.nextInt();
            scanner.nextLine(); // Read end line character
            if (input != 1 && input != 2 && input != 3)
                throw new InputMismatchException("Incorrect input given");
            clearConsole();

            if (input == 1) {
                int userID = loginScreen(connection);
                if (userID > 0) {
                    while (mainMenu(connection, userID) == 1);
                }
            } else if (input == 2) {
                int userID = registerScreen(connection);
                if (userID > 0) {
                    while (mainMenu(connection, userID) == 1);
                }
            } else {
                connection.close();
                System.exit(0); // Exits program
            }
            loginMenu(connection);
        } catch (InputMismatchException e) {
            System.out.println("Incorrect data entered. Please try Again.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            loginMenu(connection);
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Exiting.");
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }

    private static int loginScreen(Connection connection) {
        clearConsole();
        System.out.println("MyMusicList Login");
        System.out.print("Please enter your username: ");
        String username = scanner.nextLine();

        try {
            String sql = "SELECT dbo.login_with_username(?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, username);
            preparedStatement.execute();

            ResultSet resultSet = preparedStatement.getResultSet();

            if (resultSet.wasNull())
                throw new Exception("Username incorrect or not found.");

            if (resultSet.next()) {
                String result = resultSet.getString(1);

                int userID = Integer.parseInt(result);
                return userID;
            }

            throw new Exception("Username incorrect or not found.");
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning to Login Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        } catch (Exception e) {
            System.out.println("Username was incorrect or not found. Returning to Login Menu.");
            scanner.nextLine();
            return 0;
        }
    }

    private static int registerScreen(Connection connection) {
        clearConsole();
        System.out.println("MyMusicList Register");
        System.out.print("Please enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Please enter your name: ");
        String name = scanner.nextLine();

        try {
            String sql = "INSERT INTO music_user(username, name) VALUES(?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, name);
            preparedStatement.execute();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();

            while (resultSet.next()) {
                int userID = Integer.parseInt(resultSet.getString(1));
                return userID;
            }

            return -1;
        } catch (SQLException e) {
            try {
                int sqlState = Integer.parseInt(e.getSQLState());
                if (sqlState == 23000) { // Integrity constraint violation
                    System.out.println("Username already exists. Either login or try a different username. Returning to Login Menu.");
                    scanner.nextLine();
                    return 0;
                } else
                    throw new Exception("Other SQL Exception");
            } catch (Exception ex) {
                System.out.println("Error connecting to SQL database. Returning to Login Menu.");
                e.printStackTrace(System.err);
                scanner.nextLine();
                return -1;
            }
        } catch (Exception e) {
            System.out.println("Error connecting to SQL database. Returning to Login Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        }
    }

    private static int mainMenu(Connection connection, int userID) {
        try {
            String sql = "SELECT name FROM music_user WHERE user_id = " + userID + ";";
            Statement statement = connection.createStatement();
            statement.execute(sql);

            ResultSet resultSet = statement.getResultSet();

            if (resultSet.wasNull())
                throw new Exception("Could not find music_user user_id");

            String name = "";
            while (resultSet.next()) {
                name = resultSet.getString("name");
            }

            sql = "SELECT artist_id FROM artist WHERE artist_id = " + userID + ";";
            statement.execute(sql);

            resultSet = statement.getResultSet();
            boolean isArtist = resultSet.wasNull();

            clearConsole();
            System.out.println("Welcome, " + name + ".");
            System.out.println("Main Menu");
            System.out.println("1: Search MyMusicList Database");
            System.out.println("2: View/Edit Playlists");
            System.out.println("3: View/Make Song Recommendations");
            System.out.println("4: Other Queries...");
            System.out.println("5: Profile Settings");
            if (isArtist) {
                System.out.println("6: Artist Menu");
                System.out.println("7: Logout");
                System.out.println("8: Exit Program");
            } else {
                System.out.println("6: Logout");
                System.out.println("7: Exit Program");
            }

            int input = scanner.nextInt();
            scanner.nextLine(); // Read end line character
            if (input < 1 || input > 8 || (!isArtist && input > 7))
                throw new InputMismatchException("Incorrect input given");
            clearConsole();

            switch (input) {
                case 1:
                    int[] codes = searchScreen(connection, userID);
                    int referralCode = codes[0];
                    int ID = codes[1];

                    // TODO implement search functionality (song, playlist, album, artist, user)
                    break;
                case 2:
                    int playlistID = playlistMenu(connection, userID);

                    // TODO implement playlist functionality
                    break;
                case 3:
                    int songID = recommendationMenu(connection, userID);

                    // TODO implement playlist functionality
                    break;
                case 4:
                    while (queryMenu(connection, userID) == 1);
                    break;
                case 5:
                    while (profileSettings(connection, userID) == 1);
                    break;
                case 6:
                    if (isArtist)
                        while (artistMenu(connection, userID) == 1);
                    else
                        return 0;
                    break;
                case 7:
                    if (isArtist)
                        return 0;
                    else
                        System.exit(0);
                    break;
                default:
                    System.exit(0);
            }
            return 1;
        } catch (InputMismatchException e) {
            System.out.println("Incorrect menu output. Please try again");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return 1;
        } catch (Exception e) {
            System.out.println("Error connecting to SQL database. Returning to Login Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        }
    }

    /* TODO Codes:
        -1: Return to Main Menu, Error occurred
        0: No error, Return to Main menu
        1: Song (song_id)
        2: Playlist (user_id, playlist_id)
        3: Album (album_id)
        4: User (user_id)
        5: Artist (artist_id)
     */
    private static int[] searchScreen(Connection connection, int userID) {
        clearConsole();
        System.out.println("Search MyMusicList Database");
        System.out.println("Please enter search keyword: ");
        String query = scanner.nextLine();

        try {
            String sql = "{call search (" + userID + ", ?, 10, 1, 'y', 'y', 'y', 'y', 'y')}"; 
            CallableStatement callableStatement = connection.prepareCall(sql);  

            callableStatement.setString(1, query);
            ResultSet resultSet = callableStatement.executeQuery();

            LinkedList<SearchResult> results = new LinkedList<>();

            int i = 0;
            while (resultSet.next()) {
                String type = resultSet.getString(1);
                int ID = resultSet.getInt(2);
                int secID = resultSet.getInt(3);
                String name = resultSet.getString(4);

                switch (type) {
                    case "song":
                        results.add(new SearchResult(SearchResult.Entity.SONG, ID, 0, name));
                        break;
                    case "playlist":
                        results.add(new SearchResult(SearchResult.Entity.PLAYLIST, ID, secID, name));
                        break;
                    case "album":
                        results.add(new SearchResult(SearchResult.Entity.ALBUM, ID, 0, name));
                        break;
                    case "user":
                        results.add(new SearchResult(SearchResult.Entity.USER, ID, 0, name));
                        break;
                    case "artist":
                        results.add(new SearchResult(SearchResult.Entity.ARTIST, ID, 0, name));
                }

                i++;
            }

            if (i == 0) {
                System.out.println("No results found for that query. Returning to Main Menu,");
                scanner.nextLine();
                return new int[] {0};
            }

            SearchResult[] resultsArray = new SearchResult[i];
            results.toArray(resultsArray);

            clearConsole();
            System.out.println("Results:");
            System.out.printf("    %-10s %-30s\n", "Type", "Name");
            for (i = 0; i < resultsArray.length; i++) {
                if (i < 10)
                    System.out.printf(i + ":  %-10s %-30s\n", resultsArray[i].getType(), resultsArray[i].getName());
                else
                    System.out.printf(i + ": %-10s %-30s\n", resultsArray[i].getType(), resultsArray[i].getName());
            }
            System.out.println(i + ": Return to Main Menu");
            System.out.print("Select an Entry: ");
            int input = scanner.nextInt();

            if (input < 1 || input > i)
                throw new InputMismatchException("Incorrect input given");

            if (input == i)
                return new int[] {0};
            else if (resultsArray[i].getType().equals(SearchResult.Entity.PLAYLIST))
                return new int[] {2, resultsArray[i].getID(), resultsArray[i].getSecID()};
            else
                return new int[] {resultsArray[i].getType().getIntValue(), resultsArray[i].getID()};

        } catch (InputMismatchException e) {
            System.out.println("Incorrect input given. Returning to Main Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return new int[] {-1};
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning to Main Menu.");
            e.printStackTrace();
            scanner.nextLine();
            return new int[] {-1};
        }
    }

    private static int playlistMenu(Connection connection, int userID) {
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static int recommendationMenu(Connection connection, int userID) {
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static int queryMenu(Connection connection, int userID) {
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static int profileSettings(Connection connection, int userID) {
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static int artistMenu(Connection connection, int userID) {
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}