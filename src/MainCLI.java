import java.sql.*;
import java.util.HashSet;
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

    private static Scanner scanner = new Scanner(System.in);

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

            // TODO Change to Return Codes
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
                    User user = authenticateUser(connection, userID);
                    while (mainMenu(connection, user) == 1);
                } else
                    throw new InputMismatchException("Incorrect username");
            } else if (input == 2) {
                int userID = registerScreen(connection);

                if (userID > 0) {
                    User user = authenticateUser(connection, userID);
                    while (mainMenu(connection, user) == 1);
                } else
                    throw new InputMismatchException("Incorrect registration details");
            } else {
                connection.close();
                System.exit(0); // Exits program
            }
            loginMenu(connection);
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect data entered. Please try Again.");
            scanner = new Scanner(System.in);
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
                int userID = resultSet.getInt(1);
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

    private static User authenticateUser(Connection connection, int userID) throws SQLException {
        User user;
        try {
            user = MMLTools.getArtist(connection, userID);
        } catch (SQLException e1) {
            user = MMLTools.getUser(connection, userID);
        }

        return user;
    }

    private static int mainMenu(Connection connection, User user) {
        try {
            String name = user.getName();
            boolean isArtist = user instanceof Artist;

            clearConsole();
            System.out.println("Welcome, " + name + ".");
            System.out.println("Main Menu");
            System.out.println("1: Search MyMusicList Database");
            System.out.println("2: View/Edit Playlists");
            System.out.println("3: View Recommendations");
            System.out.println("4: Other Queries...");
            System.out.println("5: Delete User Account");
            if (isArtist) {
                System.out.println("6: Artist Menu");
                System.out.println("7: Logout");
                System.out.println("8: Exit Program");
            } else {
                System.out.println("6: Create Artist Account");
                System.out.println("7: Logout");
                System.out.println("8: Exit Program");
            }

            int input = scanner.nextInt();
            scanner.nextLine(); // Read end line character
            if (input < 1 || input > 8)
                throw new InputMismatchException("Incorrect input given");
            clearConsole();

            switch (input) {
                case 1:
                    int[] codes = searchScreen(connection, user);
                    int referralCode = codes[0];
                    if (referralCode <= 0)
                        return 1;

                    if (codes.length < 2)
                        return 1;

                    switch (codes[0]) {
                        case 1:
                            int songID = codes[1];
                            int code = 0;
                            while (code == 0 || code == -1) {
                                code = viewSong(connection, user, MMLTools.getSong(connection, songID));
                            }
                            return 1;
                        case 2:
                            int userID = codes[1];
                            int playlistID = codes[2];

                            songID = 0;
                            while (songID == 0 || songID == -1) {
                                songID = viewPlaylist(connection, user, MMLTools.getPlaylist(connection, userID,
                                        playlistID, false));

                                if (songID > 0) {
                                    code = 0;
                                    while (code == 0 || code == -1) {
                                        code = viewSong(connection, user, MMLTools.getSong(connection, songID));
                                    }
                                    songID = 0;
                                }
                            }
                        case 3:
                            int albumID = codes[1];
                            songID = 0;
                            while (songID == 0 || songID == -1) {
                                songID = viewAlbum(connection, user, MMLTools.getAlbum(connection, albumID, false));

                                if (songID > 0) {
                                    code = 0;
                                    while (code == 0 || code == -1) {
                                        code = viewSong(connection, user, MMLTools.getSong(connection, songID));
                                    }
                                    songID = 0;
                                }
                            }
                        default:
                            return 1;
                    }
                case 2:
                    int playlistID = 0;
                    while (playlistID == 0 || playlistID == -1) {
                        playlistID = playlistMenu(connection, user);
                        if (playlistID > 0) {
                            int songID = 0;
                            while (songID == 0 || songID == -1) {
                                songID = viewPlaylist(connection, user, MMLTools.getPlaylist(connection, user.getUserID(),
                                        playlistID, true));

                                if (songID > 0) {
                                    int code = 0;
                                    while (code == 0 || code == -1) {
                                        code = viewSong(connection, user, MMLTools.getSong(connection, songID));
                                    }
                                    songID = 0;
                                }
                            }
                            playlistID = 0;
                        }
                    }
                    return 1;
                case 3:
                    int code = 0;
                    while (code == 0 || code == -1) {
                        code = recommendationMenu(connection, user);
                    }

                    return 1;
                case 4:
                    code = 0;
                    while (code == 0 || code == -1) {
                        code = queryMenu(connection, user);
                    }

                    return 1;
                case 5:
                    System.out.print("Are you sure you want to delete your account? All songs, albums, playlists, " +
                            "and stats will be deleted. 'y' or 'n': ");
                    String line = scanner.nextLine();

                    if (line.length() != 1 || (line.charAt(0) != 'y' && line.charAt(0) != 'n'))
                        throw new InputMismatchException("Incorrect input");

                    if (line.charAt(0) == 'n')
                        return 1;

                    String sql = "{call delete_user (" + user.getUserID() + ")}";
                    CallableStatement callableStatement = connection.prepareCall(sql);
                    callableStatement.execute();

                    System.out.println("Account deleted. Returning to Login Menu.");
                    scanner.nextLine();

                    return 0;
                case 6:
                    if (isArtist) {
                        code = 0;
                        while (code == 0 || code == -1) {
                            if (user instanceof Artist)
                                code = artistMenu(connection, (Artist) user);
                            else throw new SQLException("User was designated as Artist but is not Artist");
                        }

                        if (code == -4)
                            return 0;

                        return 1;
                    } else {
                        sql = "{call create_artist (" + user.getUserID() + ")}";
                        callableStatement = connection.prepareCall(sql);
                        callableStatement.execute();

                        System.out.println("Account designated as artist account. Returning to Login Menu.");
                        scanner.nextLine();

                        return 0;
                    }
                case 7:
                    return 0;
                default:
                    System.exit(0);
            }
            return 1;
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect menu output. Please try again");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
            return 1;
        } catch (SQLException e) {
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
    private static int[] searchScreen(Connection connection, User user) {
        clearConsole();
        System.out.println("Search MyMusicList Database");
        System.out.print("Please enter search keyword: ");
        String query = scanner.nextLine();

        try {
            String sql = "{call search (" + user.getUserID() + ", ?, 10, 1, 'y', 'y', 'y', 'y', 'y')}";
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
                System.out.println("No results found for that query. Returning to Main Menu.");
                scanner.nextLine();
                return new int[] {0};
            }

            SearchResult[] resultsArray = new SearchResult[i];
            results.toArray(resultsArray);

            clearConsole();
            System.out.println("Results:");
            System.out.printf("    %-10s %-30s\n", "Type", "Name");
            for (i = 0; i < resultsArray.length; i++) {
                if (i < 9)
                    System.out.printf((i + 1) + ":  %-10s %-30s\n", resultsArray[i].getType(), resultsArray[i].getName());
                else
                    System.out.printf((i + 1) + ": %-10s %-30s\n", resultsArray[i].getType(), resultsArray[i].getName());
            }
            System.out.println((i + 1) + ": Return to Main Menu");
            System.out.print("Select an Entry: ");
            int input = scanner.nextInt();
            scanner.nextLine();

            if (input < 1 || input > (i + 1))
                throw new InputMismatchException("Incorrect input given");

            if (input == i + 1)
                return new int[] {0};
            else if (resultsArray[input - 1].getType().equals(SearchResult.Entity.PLAYLIST))
                return new int[] {2, resultsArray[input - 1].getID(), resultsArray[input - 1].getSecID()};
            else
                return new int[] {resultsArray[input - 1].getType().getIntValue(), resultsArray[input - 1].getID()};

        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect input given. Returning to Main Menu.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
            return new int[] {-1};
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning to Main Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return new int[] {-1};
        }
    }

    /*
     * -3: Error, no repeat
     * -2: No error, no repeat
     * -1: Error, repeat
     * 0: No error, repeat
     * >0: playlist ID
     */
    private static int playlistMenu(Connection connection, User user) {
        clearConsole();
        System.out.println("Playlist Menu");
        System.out.printf("    %-30s %-12s %-12s\n", "Name", "Song Count", "Duration");

        try {
            Playlist[] playlists = MMLTools.findPlaylists(connection, user);

            int i = 0;
            for (Playlist playlist : playlists) {
                if (i < 9)
                    System.out.printf((i + 1) + ":  %-30s %-12s %-12s\n", playlist.getName(),
                            playlist.getNumSongs(), playlist.getDuration());
                else
                    System.out.printf((i + 1) + ": %-30s %-12s %-12s\n", playlist.getName(),
                            playlist.getNumSongs(), playlist.getDuration());
                i++;
            }

            if (playlists.length == 0) {
                System.out.println("No Playlists to Display");
            }

            System.out.println((i + 1) + ": Create New Playlist");
            System.out.println((i + 2) + ": Return to Main Menu");
            System.out.print("Select an Entry: ");
            int input = scanner.nextInt();
            scanner.nextLine();

            if (input < 1 || input > i + 2)
                throw new InputMismatchException("Incorrect input given");

            if (input == i + 2)
                return -2;
            else if (input == i + 1) {
                createPlaylistScreen(connection, user);
                return 0;
            } else
                return playlists[input - 1].getPlaylistID();
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect input given. Please try again.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        } catch (SQLException e) {

            System.out.println("Error connecting to SQL database. Returning to Main Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -3;
        }
    }

    /*
     0: Return with repeat
    -1: Error: repeat
    -2: Return with no repeat
    -3: Error: return with no repeat
    -4: Return to Login Menu
     */
    private static int recommendationMenu(Connection connection, User user) {
        clearConsole();
        System.out.println("Recommendation Menu");
        System.out.println("1: View User Recommendations");
        System.out.println("2: View Auto-generated Recommendations");
        System.out.println("3: Return to Main Menu");

        int input = scanner.nextInt();
        scanner.nextLine();
        if (input < 1 || input > 3)
            throw new InputMismatchException("Incorrect input given");

        try {
            switch (input) {
                case 1:
                    int songID = 0;
                    while (songID == 0 || songID == -1) {
                        songID = viewPersonalRecommendations(connection, user);
                        if (songID > 0) {
                            int code = 0;
                            while (code == 0 || code == -1) {
                                code = viewSong(connection, user, MMLTools.getSong(connection, songID));
                            }
                            songID = 0;
                        }
                    }
                    return 0;
                case 2:
                    songID = 0;
                    while (songID == 0 || songID == -1) {
                        songID = viewAutoGeneratedRecommendations(connection, user);
                        if (songID > 0) {
                            int code = 0;
                            while (code == 0 || code == -1) {
                                code = viewSong(connection, user, MMLTools.getSong(connection, songID));
                            }
                            songID = 0;
                        }
                    }
                    return 0;
                default:
                    return -2;
            }
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect input given. Please try again.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning to Main Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -3;
        }
    }

    private static int queryMenu(Connection connection, User user) {
        clearConsole();
        System.out.println("Other Queries");
        System.out.println("1: View Your Favorite Artists");
        System.out.println("2: View Your Favorite Genres");
        System.out.println("3: View Your Favorite Songs");
        System.out.println("4: Find Most Popular Songs");
        System.out.println("5: Find Highest Rated Songs");
        System.out.println("6: Return to Main Menu");
        System.out.println();

        try {
            System.out.print("Select an Entry: ");
            int input = scanner.nextInt();
            scanner.nextLine(); // Read end line character

            if (input < 1 || input > 6)
                throw new InputMismatchException("Incorrect input given");

            switch (input) {
                case 1:
                    clearConsole();
                    System.out.println("Your Favorite Artists:");
                    try {
                        String sql = "SELECT * FROM dbo.fav_artists(" + user.getUserID() + ", 10)";
                        Statement statement = connection.createStatement();
                        statement.execute(sql);
                        ResultSet resultSet = statement.getResultSet();

                        int i = 0;
                        System.out.printf("    %-30s %-5s\n", "Name", "Listens");
                        while (resultSet.next()) {
                            if (i < 9)
                                System.out.printf((i + 1) + ":  %-30s %-5s\n", resultSet.getString("name"),
                                        resultSet.getInt("total_listens"));
                            else
                                System.out.printf((i + 1) + ": %-30s %-5s\n", resultSet.getString("name"),
                                        resultSet.getInt("total_listens"));
                            i++;
                        }
                        if (i == 0)
                            System.out.println("No listens recorded yet.");

                        scanner.nextLine();
                    } catch (NumberFormatException | InputMismatchException e) {
                        System.out.println("Incorrect menu output. Returning to Query Menu.");
                        scanner = new Scanner(System.in);
                        e.printStackTrace(System.err);
                        scanner.nextLine();
                    } catch (SQLException e) {
                        System.out.println("Error connecting to SQL database. Returning to Query Menu.");
                        e.printStackTrace(System.err);
                        scanner.nextLine();
                    }
                    return 0;
                case 2:
                    clearConsole();
                    System.out.println("Your Favorite Genres:");
                    try {
                        String sql = "SELECT * FROM dbo.fav_genres(" + user.getUserID() + ", 10)";
                        Statement statement = connection.createStatement();
                        statement.execute(sql);
                        ResultSet resultSet = statement.getResultSet();

                        int i = 0;
                        System.out.printf("    %-30s %-5s\n", "Name", "Listens");
                        while (resultSet.next()) {
                            if (i < 9)
                                System.out.printf((i + 1) + ":  %-30s %-5s\n",
                                        resultSet.getString("genre_name"),
                                        resultSet.getInt("total_listens"));
                            else
                                System.out.printf((i + 1) + ": %-30s %-5s\n",
                                        resultSet.getString("genre_name"),
                                        resultSet.getInt("total_listens"));
                            i++;
                        }
                        if (i == 0)
                            System.out.println("No listens recorded yet.");

                        scanner.nextLine();
                    } catch (NumberFormatException | InputMismatchException e) {
                        System.out.println("Incorrect menu output. Returning to Query Menu.");
                        scanner = new Scanner(System.in);
                        e.printStackTrace(System.err);
                        scanner.nextLine();
                    } catch (SQLException e) {
                        System.out.println("Error connecting to SQL database. Returning to Query Menu.");
                        e.printStackTrace(System.err);
                        scanner.nextLine();
                    }
                    return 0;
                case 3:
                    clearConsole();
                    System.out.println("Your Favorite Songs:");
                    try {
                        String sql = "SELECT * FROM dbo.fav_songs(" + user.getUserID() + ", 10)";
                        Statement statement = connection.createStatement();
                        statement.execute(sql);
                        ResultSet resultSet = statement.getResultSet();

                        int i = 0;
                        System.out.printf("    %-30s %-5s\n", "Name", "Listens");
                        while (resultSet.next()) {
                            if (i < 9)
                                System.out.printf((i + 1) + ":  %-30s %-5s\n",
                                        resultSet.getString("name"),
                                        resultSet.getInt("total_listens"));
                            else
                                System.out.printf((i + 1) + ": %-30s %-5s\n",
                                        resultSet.getString("name"),
                                        resultSet.getInt("total_listens"));
                            i++;
                        }
                        if (i == 0)
                            System.out.println("No listens recorded yet.");

                        scanner.nextLine();
                    } catch (NumberFormatException | InputMismatchException e) {
                        System.out.println("Incorrect menu output. Returning to Query Menu.");
                        scanner = new Scanner(System.in);
                        e.printStackTrace(System.err);
                        scanner.nextLine();
                    } catch (SQLException e) {
                        System.out.println("Error connecting to SQL database. Returning to Query Menu.");
                        e.printStackTrace(System.err);
                        scanner.nextLine();
                    }
                    return 0;
                case 4:
                    clearConsole();
                    System.out.println("Most Popular Songs:");
                    try {
                        String sql = "SELECT * FROM dbo.most_popular_songs(" + user.getUserID() + ", 10)";
                        Statement statement = connection.createStatement();
                        statement.execute(sql);
                        ResultSet resultSet = statement.getResultSet();

                        int i = 0;
                        System.out.printf("    %-30s %-5s\n", "Name", "Total Listens");
                        while (resultSet.next()) {
                            if (i < 9)
                                System.out.printf((i + 1) + ":  %-30s %-5s\n",
                                        resultSet.getString("name"),
                                        resultSet.getInt("total_listens"));
                            else
                                System.out.printf((i + 1) + ": %-30s %-5s\n",
                                        resultSet.getString("name"),
                                        resultSet.getInt("total_listens"));
                            i++;
                        }
                        if (i == 0)
                            System.out.println("No listens recorded yet.");

                        scanner.nextLine();
                    } catch (NumberFormatException | InputMismatchException e) {
                        System.out.println("Incorrect menu output. Returning to Query Menu.");
                        scanner = new Scanner(System.in);
                        e.printStackTrace(System.err);
                        scanner.nextLine();
                    } catch (SQLException e) {
                        System.out.println("Error connecting to SQL database. Returning to Query Menu.");
                        e.printStackTrace(System.err);
                        scanner.nextLine();
                    }
                    return 0;
                case 5:
                    clearConsole();
                    System.out.println("Highest Rated Songs:");
                    try {
                        String sql = "SELECT * FROM dbo.highest_rated_songs(" + user.getUserID() + ", 10)";
                        Statement statement = connection.createStatement();
                        statement.execute(sql);
                        ResultSet resultSet = statement.getResultSet();

                        int i = 0;
                        System.out.printf("    %-30s %-5s\n", "Name", "Average Rating");
                        while (resultSet.next()) {
                            if (i < 9)
                                System.out.printf((i + 1) + ":  %-30s %-5s\n",
                                        resultSet.getString("name"),
                                        Math.round(10 * resultSet.getFloat("avg_rating")) / 10.0);
                            else
                                System.out.printf((i + 1) + ": %-30s %-5s\n",
                                        resultSet.getString("name"),
                                        Math.round(10 * resultSet.getFloat("avg_rating")) / 10.0);
                            i++;
                        }
                        if (i == 0)
                            System.out.println("No listens recorded yet.");

                        scanner.nextLine();
                    } catch (NumberFormatException | InputMismatchException e) {
                        System.out.println("Incorrect menu output. Returning to Query Menu.");
                        scanner = new Scanner(System.in);
                        e.printStackTrace(System.err);
                        scanner.nextLine();
                    } catch (SQLException e) {
                        System.out.println("Error connecting to SQL database. Returning to Query Menu.");
                        e.printStackTrace(System.err);
                        scanner.nextLine();
                    }
                    return 0;
                default:
                    return -2;
            }
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect input given. Please try again.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        }
    }

    private static int artistMenu(Connection connection, Artist artist) {
        clearConsole();
        System.out.println("Artist Menu");
        System.out.println("1: View/Edit Albums");
        System.out.println("2: View/Edit Songs");
        System.out.println("3: Delete Artist Account");
        System.out.println("4: Return to Main Menu");

        try {
            System.out.print("Select an Entry: ");
            int input = scanner.nextInt();
            scanner.nextLine(); // Read end line character

            if (input < 1 || input > 4)
                throw new InputMismatchException("Incorrect input given");

            switch (input) {
                case 1:
                    int albumID = 0;
                    while (albumID == 0 || albumID == -1) {
                        albumID = albumMenu(connection, artist);
                        if (albumID > 0) {
                            int songID = 0;
                            while (songID == 0 || songID == -1) {
                                songID = viewAlbum(connection, artist, MMLTools.getAlbum(connection, albumID, true));

                                if (songID > 0) {
                                    int code = 0;
                                    while (code == 0 || code == -1) {
                                        code = viewSong(connection, artist, MMLTools.getSong(connection, songID));
                                    }
                                    songID = 0;
                                }
                            }
                            albumID = 0;
                        }
                    }
                    return 0;
                case 2:
                    int songID = 0;
                    while (songID == 0 || songID == -1) {
                        songID = songMenu(connection, artist);

                        if (songID > 0) {
                            int code = 0;
                            while (code == 0 || code == -1) {
                                code = viewSong(connection, artist, MMLTools.getSong(connection, songID));
                            }
                            songID = 0;
                        }
                    }
                    return 0;
                case 3:
                    System.out.print("Are you sure you want to delete this artist account? All songs and albums " +
                            "will be deleted, even if they have collaborating artists. 'y' or 'n': ");
                    String line = scanner.nextLine();

                    if (line.length() != 1 || (line.charAt(0) != 'y' && line.charAt(0) != 'n'))
                        throw new InputMismatchException("Incorrect input");

                    if (line.charAt(0) == 'n')
                        return 0;

                    String sql = "{call delete_artist (" + artist.getUserID() + ")}";
                    CallableStatement callableStatement = connection.prepareCall(sql);
                    callableStatement.execute();

                    System.out.println("Artist account deleted. Returning to Login Menu.");
                    scanner.nextLine();
                    return -4;
                default:
                    return -2;
            }
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect input given. Please try again.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning to Main Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -3;
        }
    }

    private static void createPlaylistScreen(Connection connection, User user) {
        clearConsole();
        try {
            System.out.println("Create New Playlist ");
            System.out.print("What is the name of your playlist? ");
            String name = scanner.nextLine();
            System.out.print("Should your playlist be public? 'y' or 'n': ");
            String privacy = scanner.nextLine();

            if (privacy.length() != 1 || (privacy.charAt(0) != 'y' && privacy.charAt(0) != 'n'))
                throw new InputMismatchException("Incorrect input");

            String sql = "SELECT dbo.generate_playlist_id(" + user.getUserID() + ")";
            Statement statement = connection.createStatement();
            statement.execute(sql);
            ResultSet resultSet = statement.getResultSet();

            if (!resultSet.next())
                throw new SQLException("Playlist ID could not be generated");

            int playlistID = resultSet.getInt(1);
            sql = "{call make_playlist (" + user.getUserID() + ", " + playlistID + ", ?, ?)}";
            CallableStatement callableStatement = connection.prepareCall(sql);
            callableStatement.setString(1, name);
            callableStatement.setString(2, privacy);
            callableStatement.execute();

            System.out.println("Created new playlist. Returning to Playlist Menu.");
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect input entered. Returning to Playlist Menu.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning to Playlist Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
        }
    }

    /*
    >0: songID
     0: Return with repeat
    -1: Error: repeat
    -2: Return with no repeat
    -3: Error: return with no repeat
     */
    private static int viewPlaylist(Connection connection, User user, Playlist playlist) {
        clearConsole();
        System.out.println(playlist.getName());
        if (playlist.isPublic())
            System.out.println("Public playlist by " + playlist.getUser().getName());
        else
            System.out.println("Private playlist by " + playlist.getUser().getName());

        if (playlist.getSongs().length == 0)
            System.out.println("No songs yet.");
        else {
            System.out.printf("    %-30s %-12s\n", "Name", "Duration");
            for (int i = 0; i < playlist.getSongs().length; i++) {
                Song song = playlist.getSongs()[i];
                if (i < 9)
                    System.out.printf((i + 1) + ":  %-30s %-12s\n", song.getName(), song.getDuration());
                else
                    System.out.printf((i + 1) + ": %-30s %-12s\n", song.getName(), song.getDuration());
            }
        }
        if (playlist.isCanEdit()) {
            if (playlist.getNumSongs() > 0) {
                System.out.println((playlist.getNumSongs() + 1) + ": Remove Songs");
                System.out.println((playlist.getNumSongs() + 2) + ": Toggle Playlist Privacy");
                System.out.println((playlist.getNumSongs() + 3) + ": Delete Playlist");
                System.out.println((playlist.getNumSongs() + 4) + ": Return to Playlist Menu");
            } else {
                System.out.println((playlist.getNumSongs() + 1) + ": Toggle Playlist Privacy");
                System.out.println((playlist.getNumSongs() + 2) + ": Delete Playlist");
                System.out.println((playlist.getNumSongs() + 3) + ": Return to Playlist Menu");
            }
        } else
            System.out.println((playlist.getNumSongs() + 1) + ": Return to Playlist Menu");

        System.out.print("Select an Entry: ");
        try {
            int input = scanner.nextInt();
            scanner.nextLine(); // Read end line character
            if (input < 1 || input > playlist.getNumSongs() + 4 ||
                    (!playlist.isCanEdit() && input > playlist.getNumSongs() + 1) ||
                    (playlist.getNumSongs() == 0 && input > playlist.getNumSongs() + 3))
                throw new InputMismatchException("Incorrect input given");

            if (input == playlist.getNumSongs() + 4 || (input == playlist.getNumSongs() + 1 && !playlist.isCanEdit()) ||
                    (input == playlist.getNumSongs() + 3 && playlist.getNumSongs() == 0))
                return -2;
            else if (input == playlist.getNumSongs() + 3 ||
                    (input == playlist.getNumSongs() + 2 && playlist.getNumSongs() == 0)) {
                System.out.print("Are you sure you want to delete this playlist? 'y' or 'n': ");
                String line = scanner.nextLine();

                if (line.length() != 1 || (line.charAt(0) != 'y' && line.charAt(0) != 'n'))
                    throw new InputMismatchException("Incorrect input");

                if (line.charAt(0) == 'n')
                    return 0;

                String sql = "{call delete_playlist (" + playlist.getUser().getUserID() + ", " +
                        playlist.getPlaylistID() + ")}";
                CallableStatement callableStatement = connection.prepareCall(sql);
                callableStatement.execute();

                System.out.println("Playlist deleted. Returning to Playlist Menu.");
                scanner.nextLine();
                return -2;
            } else if (input == playlist.getNumSongs() + 2 ||
                    (input == playlist.getNumSongs() + 1 && playlist.getNumSongs() == 0)) {
                if (playlist.isPublic())
                    System.out.print("Are you sure you want to make this playlist private? 'y' or 'n': ");
                else
                    System.out.print("Are you sure you want to make this playlist public? 'y' or 'n': ");

                String line = scanner.nextLine();
                if (line.length() != 1 || (line.charAt(0) != 'y' && line.charAt(0) != 'n'))
                    throw new InputMismatchException("Incorrect input");

                if (line.charAt(0) == 'n')
                    return 0;

                String sql = "{call toggle_playlist_privacy (" + playlist.getUser().getUserID() + ", " +
                        playlist.getPlaylistID() + ")}";
                CallableStatement callableStatement = connection.prepareCall(sql);
                callableStatement.execute();

                if (playlist.isPublic())
                    System.out.println("Playlist was made private.");
                else
                    System.out.println("Playlist was made public.");
                scanner.nextLine();

                return 0;
            } else if (input == playlist.getNumSongs() + 1) {
                System.out.print("Enter the numbers of the songs you want deleted, separated by commas, no spaces: ");
                String line = scanner.nextLine();
                String[] entries = line.split(",");
                int[] songEntries = new int[entries.length];

                if (entries.length == 0)
                    throw new InputMismatchException("No entries specified");

                for (int i = 0; i < entries.length; i++) {
                    int parsed = Integer.parseInt(entries[i]) - 1;
                    if (parsed < 0 || parsed >= playlist.getNumSongs())
                        throw new InputMismatchException("Entry number not valid");
                    songEntries[i] = parsed;
                }

                for (int i = 0; i < songEntries.length; i++) {
                    String sql = "{call remove_from_playlist (" + playlist.getSongs()[songEntries[i]].getSongID() + ", " +
                            playlist.getUser().getUserID() + ", " + playlist.getPlaylistID() + ")}";
                    CallableStatement callableStatement = connection.prepareCall(sql);
                    callableStatement.execute();
                }

                System.out.println("Songs removed.");
                scanner.nextLine();

                return 0;
            }

            return playlist.getSongs()[input - 1].getSongID();
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect menu output. Please try again.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning to Playlist Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -3;
        }
    }

    /*
    >0: songID
     0: Return with repeat
    -1: Error: repeat
    -2: Return with no repeat
    -3: Error: return with no repeat
     */
    private static int songMenu(Connection connection, Artist artist) {
        clearConsole();
        System.out.println("Songs:");
        try {
            Song[] songs = MMLTools.findSongs(connection, artist);
            if (songs.length == 0)
                System.out.println("No songs yet.");
            else {
                System.out.printf("    %-30s %-12s\n", "Name", "Duration");
                for (int i = 0; i < songs.length; i++) {
                    Song song = songs[i];
                    if (i < 9)
                        System.out.printf((i + 1) + ":  %-30s %-12s\n", song.getName(), song.getDuration());
                    else
                        System.out.printf((i + 1) + ": %-30s %-12s\n", song.getName(), song.getDuration());
                }
            }
            if (songs.length > 0) {
                System.out.println((songs.length + 1) + ": Remove Songs");
                System.out.println((songs.length + 2) + ": Add Song");
                System.out.println((songs.length + 3) + ": Return to Artist Menu");
            } else {
                System.out.println((songs.length + 1) + ": Add Song");
                System.out.println((songs.length + 2) + ": Return to Artist Menu");
            }
            System.out.print("Select an Entry: ");
            int input = scanner.nextInt();
            scanner.nextLine(); // Read end line character

            if (input < 1 || input > songs.length + 3 || (songs.length == 0 && input > songs.length + 2))
                throw new InputMismatchException("Incorrect input given");

            if (input == songs.length + 3 || (songs.length == 0 && input == songs.length + 2))
                return -2;
            else if (input == songs.length + 2 || (songs.length == 0 && input == songs.length + 1)) {
                createSongScreen(connection, artist);
                return 0;
            } else if (input == songs.length + 1) {
                System.out.print("Enter the numbers of the songs you want deleted, separated by commas, no spaces: ");
                String line = scanner.nextLine();

                String[] entries = line.split(",");
                int[] songEntries = new int[entries.length];

                if (entries.length == 0)
                    throw new InputMismatchException("No entries specified");

                for (int i = 0; i < entries.length; i++) {
                    int parsed = Integer.parseInt(entries[i]) - 1;
                    if (parsed < 0 || parsed >= songs.length)
                        throw new InputMismatchException("Entry number not valid");
                    songEntries[i] = parsed;
                }

                for (int i = 0; i < songEntries.length; i++) {
                    String sql = "{call remove_song (" + songs[songEntries[i]].getSongID() + ")}";
                    CallableStatement callableStatement = connection.prepareCall(sql);
                    callableStatement.execute();
                }

                System.out.println("Songs deleted. Returning to Song Menu.");
                scanner.nextLine();
                return 0;
            }

            return songs[input - 1].getSongID();
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect menu output. Please try again.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning to Song Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -3;
        }
    }

    /*
   >0: songID
    0: Return with repeat
   -1: Error: repeat
   -2: Return with no repeat
   -3: Error: return with no repeat
    */
    private static int viewAlbum(Connection connection, User user, Album album) {
        clearConsole();
        System.out.println(album.getName());
        System.out.print("Album by ");
        for (int i = 0; i < album.getArtists().length; i++) {
            Artist artist = album.getArtists()[i];
            if (i == album.getArtists().length - 1)
                System.out.print(artist.getName() + "\n");
            else
                System.out.print(artist.getName() + ", ");
        }

        try {
            String sql = "SELECT dbo.num_plays_album(" + album.getAlbumID() + ")";
            Statement statement = connection.createStatement();
            statement.execute(sql);
            ResultSet resultSet = statement.getResultSet();

            int numPlays;
            if (resultSet.next()) {
                numPlays = resultSet.getInt(1);
            } else
                throw new SQLException("No num plays found");

            sql = "SELECT dbo.avg_rating_album(" + album.getAlbumID() + ")";
            statement = connection.createStatement();
            statement.execute(sql);
            resultSet = statement.getResultSet();

            double avgRating;
            if (resultSet.next()) {
                if (resultSet.wasNull())
                    avgRating = -1;
                else
                    avgRating = resultSet.getFloat(1);
            } else
                throw new SQLException("No average rating found");

            System.out.println("Duration: " + album.getDuration());
            if (avgRating < 1)
                System.out.println("Not rated yet");
            else
                System.out.println("Average rating: " + Math.round(avgRating * 10) / 10.0 + " out of 10");
            System.out.println("Number of plays: " + numPlays);

            System.out.println();
            System.out.println("Songs:");
            if (album.getSongs().length == 0)
                System.out.println("No songs yet.");
            else {
                System.out.printf("    %-30s %-12s\n", "Name", "Duration");
                for (int i = 0; i < album.getSongs().length; i++) {
                    Song song = album.getSongs()[i];
                    if (i < 9)
                        System.out.printf((i + 1) + ":  %-30s %-12s\n", song.getName(), song.getDuration());
                    else
                        System.out.printf((i + 1) + ": %-30s %-12s\n", song.getName(), song.getDuration());
                }
            }

            if (album.isCanEdit()) {
                if (album.getNumSongs() > 0) {
                    System.out.println((album.getNumSongs() + 1) + ": Remove Songs");
                    System.out.println((album.getNumSongs() + 2) + ": Delete Album");
                    System.out.println((album.getNumSongs() + 3) + ": Return to Album Menu");
                } else {
                    System.out.println((album.getNumSongs() + 1) + ": Delete Album");
                    System.out.println((album.getNumSongs() + 2) + ": Return to Album Menu");
                }
            } else
                System.out.println((album.getNumSongs() + 1) + ": Return to Album Menu");

            System.out.print("Select an Entry: ");
            int input = scanner.nextInt();
            scanner.nextLine(); // Read end line character
            if (input < 1 || input > album.getNumSongs() + 3 || (!album.isCanEdit() &&
                    input > album.getNumSongs() + 1) ||
                    (album.getNumSongs() == 0 && input > album.getNumSongs() + 2))
                throw new InputMismatchException("Incorrect input given");

            if (input == album.getNumSongs() + 3 || (input == album.getNumSongs() + 1 && !album.isCanEdit()) ||
                    (input == album.getNumSongs() + 2 && album.getNumSongs() == 0))
                return -2;
            else if (input == album.getNumSongs() + 2 ||
                    (input == album.getNumSongs() + 1 && album.getNumSongs() == 0)) {
                System.out.print("Are you sure you want to delete this album? 'y' or 'n': ");
                String line = scanner.nextLine();

                if (line.length() != 1 || (line.charAt(0) != 'y' && line.charAt(0) != 'n'))
                    throw new InputMismatchException("Incorrect input");

                if (line.charAt(0) == 'n')
                    return 0;

                sql = "{call delete_album (" + album.getAlbumID() + ")}";
                CallableStatement callableStatement = connection.prepareCall(sql);
                callableStatement.execute();

                System.out.println("Album deleted. Returning to Album Menu.");
                scanner.nextLine();
                return -2;
            } else if (input == album.getNumSongs() + 1) {
                System.out.print("Enter the numbers of the songs you want deleted, separated by commas, no spaces: ");
                String line = scanner.nextLine();

                String[] entries = line.split(",");
                int[] songEntries = new int[entries.length];

                if (entries.length == 0)
                    throw new InputMismatchException("No entries specified");

                for (int i = 0; i < entries.length; i++) {
                    int parsed = Integer.parseInt(entries[i]) - 1;
                    if (parsed < 0 || parsed >= album.getNumSongs())
                        throw new InputMismatchException("Entry number not valid");
                    songEntries[i] = parsed;
                }

                for (int i = 0; i < songEntries.length; i++) {
                    sql = "{call remove_from_album (" + album.getSongs()[songEntries[i]].getSongID() + ", " +
                            album.getAlbumID() + ")}";
                    CallableStatement callableStatement = connection.prepareCall(sql);
                    callableStatement.execute();
                }

                System.out.println("Songs removed from album. Returning to Album Menu.");
                scanner.nextLine();
                return 0;
            }

            return album.getSongs()[input - 1].getSongID();
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect menu output. Please try again.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -3;
        }
    }

    /*
     0: Return with repeat
    -1: Error: repeat
    -2: Return with no repeat
    -3: Error: return with no repeat
     */
    private static int viewSong(Connection connection, User user, Song song) {
        clearConsole();
        System.out.println(song.getName());
        System.out.print("Song by ");
        for (int i = 0; i < song.getArtists().length; i++) {
            Artist artist = song.getArtists()[i];
            if (i == song.getArtists().length - 1)
                System.out.print(artist.getName() + "\n");
            else
                System.out.print(artist.getName() + ", ");
        }

        try {
            String sql = "SELECT dbo.num_plays_song(" + song.getSongID() + ")";
            Statement statement = connection.createStatement();
            statement.execute(sql);
            ResultSet resultSet = statement.getResultSet();

            int numPlays;
            if (resultSet.next()) {
                numPlays = resultSet.getInt(1);
            } else
                throw new SQLException("No num plays found");

            sql = "SELECT dbo.num_playlists_song(" + song.getSongID() + ")";
            statement = connection.createStatement();
            statement.execute(sql);
            resultSet = statement.getResultSet();

            int numPlaylists;
            if (resultSet.next()) {
                numPlaylists = resultSet.getInt(1);
            } else
                throw new SQLException("No num playlists found");

            sql = "SELECT dbo.num_albums_song(" + song.getSongID() + ")";
            statement = connection.createStatement();
            statement.execute(sql);
            resultSet = statement.getResultSet();

            int numAlbums;
            if (resultSet.next()) {
                numAlbums = resultSet.getInt(1);
            } else
                throw new SQLException("No num albums found");

            sql = "SELECT dbo.avg_rating_song(" + song.getSongID() + ")";
            statement = connection.createStatement();
            statement.execute(sql);
            resultSet = statement.getResultSet();

            double avgRating;
            if (resultSet.next()) {
                if (resultSet.wasNull())
                    avgRating = -1;
                else
                    avgRating = resultSet.getFloat(1);
            } else
                throw new SQLException("No average rating found");

            System.out.println("Duration: " + song.getDuration());
            if (avgRating < 1)
                System.out.println("Not rated yet");
            else
                System.out.println("Average rating: " + Math.round(avgRating * 10) / 10.0 + " out of 10");
            System.out.println("Number of plays: " + numPlays);
            if (numPlaylists == 1)
                System.out.println("Found in " + numPlaylists + " playlist");
            else
                System.out.println("Found in " + numPlaylists + " playlists");
            if (numAlbums == 1)
                System.out.println("Found in " + numPlaylists + " album");
            else
                System.out.println("Found in " + numPlaylists + " albums");

            System.out.println("1: Add to Playlist");

            if (song.isArtist(user)) {
                System.out.println("2: Add to Album");
                System.out.println("3: Log Listen");
                System.out.println("4: Review/Rate");
                System.out.println("5: Recommend to Others");
                System.out.println("6: Return");
            } else {
                System.out.println("2: Log Listen");
                System.out.println("3: Review/Rate");
                System.out.println("4: Recommend to Others");
                System.out.println("5: Return");
            }

            System.out.print("Select an Entry: ");
            int input = scanner.nextInt();
            scanner.nextLine(); // Read end line character

            if (input < 1 || input > 6 || (input > 5 && !song.isArtist(user)))
                throw new InputMismatchException("Incorrect input given");

            if (input == 1) {
                addToPlaylist(connection, user, song);
                return 0;
            } else if (input == 2 && song.isArtist(user)) {
                if (user instanceof Artist)
                    addToAlbum(connection, (Artist) user, song);
                else throw new SQLException("User created a song without being an Artist");
                return 0;
            } else if (input == 2) {
                logListenScreen(connection, user, song);
                return 0;
            } else if (input == 3) {
                reviewScreen(connection, user, song);
                return 0;
            } else if (input == 4) {
                makeRecommendationScreen(connection, user, song);
                return 0;
            } else
                return -2;
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect menu output. Please try again.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -3;
        }
    }

    private static void addToPlaylist(Connection connection, User user, Song song) {
        clearConsole();

        try {
            Playlist[] playlists = MMLTools.findPlaylists(connection, user);

            if (playlists.length == 0) {
                System.out.println("You do not have any playlists. Please create a playlist " +
                        "before adding songs to playlists.");
                scanner.nextLine();
                return;
            }

            System.out.printf("    %-30s %-12s %-12s\n", "Name", "Song Count", "Duration");
            for (int i = 0; i < playlists.length; i++) {
                Playlist playlist = playlists[i];
                if (i < 9)
                    System.out.printf((i + 1) + ":  %-30s %-12s %-12s\n", playlist.getName(),
                            playlist.getNumSongs(), playlist.getDuration());
                else
                    System.out.printf((i + 1) + ": %-30s %-12s %-12s\n", playlist.getName(),
                            playlist.getNumSongs(), playlist.getDuration());
            }

            System.out.print("Add to which playlist? ");
            int input = scanner.nextInt();
            scanner.nextLine(); // Read end line character

            if (input < 1 || input > playlists.length)
                throw new InputMismatchException("Incorrect input given");

            String sql = "{call add_to_playlist (" + song.getSongID() + ", " +
                    playlists[input - 1].getUser().getUserID() + ", " + playlists[input - 1].getPlaylistID() + ")}";
            CallableStatement callableStatement = connection.prepareCall(sql);
            callableStatement.execute();

            System.out.println("Successfully added to playlist.");
            scanner.nextLine();
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect input given. Returning.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
        } catch (SQLException e) {
            try {
                int sqlState = Integer.parseInt(e.getSQLState());
                if (sqlState == 23000) { // Integrity constraint violation
                    System.out.println("That playlist already has the song added. Returning.");
                    scanner.nextLine();
                } else
                    throw new Exception("Other SQL Exception");
            } catch (Exception ex) {
                System.out.println("Error connecting to SQL database. Returning.");
                e.printStackTrace(System.err);
                scanner.nextLine();
            }
        }
    }

    private static void addToAlbum(Connection connection, Artist artist, Song song) {
        clearConsole();

        try {
            Album[] albums = MMLTools.findAlbums(connection, artist);

            if (albums.length == 0) {
                System.out.println("You do not have any albums. Please create an album " +
                        "before adding songs to albums.");
                scanner.nextLine();
                return;
            }

            System.out.printf("    %-30s %-12s %-12s\n", "Name", "Song Count", "Duration");
            for (int i = 0; i < albums.length; i++) {
                Album album = albums[i];
                if (i < 9)
                    System.out.printf((i + 1) + ":  %-30s %-12s %-12s\n", album.getName(),
                            album.getNumSongs(), album.getDuration());
                else
                    System.out.printf((i + 1) + ": %-30s %-12s %-12s\n", album.getName(),
                            album.getNumSongs(), album.getDuration());
            }

            System.out.print("Add to which album? ");
            int input = scanner.nextInt();
            scanner.nextLine(); // Read end line character

            if (input < 1 || input > albums.length)
                throw new InputMismatchException("Incorrect input given");

            String sql = "{call add_to_album (" + song.getSongID() + ", " +
                    albums[input - 1].getAlbumID() + ")}";
            CallableStatement callableStatement = connection.prepareCall(sql);
            callableStatement.execute();

            System.out.println("Successfully added to album.");
            scanner.nextLine();
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect input given. Returning.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
        } catch (SQLException e) {
            try {
                int sqlState = Integer.parseInt(e.getSQLState());
                if (sqlState == 23000) { // Integrity constraint violation
                    System.out.println("That album already has the song added. Returning.");
                    scanner.nextLine();
                } else
                    throw new Exception("Other SQL Exception");
            } catch (Exception ex) {
                System.out.println("Error connecting to SQL database. Returning.");
                e.printStackTrace(System.err);
                scanner.nextLine();
            }
        }
    }

    /*
     >0: Song ID
     0: Return with repeat
    -1: Error: repeat
    -2: Return with no repeat
    -3: Error: return with no repeat
     */
    private static int viewPersonalRecommendations(Connection connection, User user) {
        clearConsole();
        try {
            Recommendation[] recommendations = MMLTools.findPersonalRecommendations(connection, user);

            if (recommendations.length == 0) {
                System.out.println("No user recommendations yet. Come back later!");
                return -2;
            }

            System.out.println("User Recommendations");
            System.out.printf("    %-30s %-20s\n", "Name", "Recommended By");
            for (int i = 0; i < recommendations.length; i++) {
                Recommendation recommendation = recommendations[i];
                if (i < 9)
                    System.out.printf((i + 1) + ":  %-30s %-20s\n", recommendation.getSong().getName(),
                            recommendation.getFromUser().getName());
                else
                    System.out.printf((i + 1) + ": %-30s %-20s\n", recommendation.getSong().getName(),
                            recommendation.getFromUser().getName());
            }
            System.out.println((recommendations.length + 1) + ": Return to Recommendation Menu");
            System.out.print("Select an Entry: ");
            int input = scanner.nextInt();
            scanner.nextLine();

            if (input < 1 || input > recommendations.length + 1)
                throw new InputMismatchException("Incorrect input given");

            if (input == recommendations.length + 1)
                return -2;

            return recommendations[input - 1].getSong().getSongID();
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect input given. Please try again.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning to Main Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -3;
        }
    }

    /*
     >0: Song ID
     0: Return with repeat
    -1: Error: repeat
    -2: Return with no repeat
    -3: Error: return with no repeat
     */
    private static int viewAutoGeneratedRecommendations(Connection connection, User user) {
        clearConsole();
        try {
            Recommendation[] recommendations = MMLTools.findAutoGeneratedRecommendations(connection, user);

            if (recommendations.length == 0) {
                System.out.println("No auto-generated recommendations yet. Listen to songs first!");
                return -2;
            }

            System.out.println("Songs by your Favorite Artists");
            int i = 0;
            while (i < recommendations.length && recommendations[i].getType().equals("artist")) {
                Recommendation recommendation = recommendations[i];
                if (i < 9)
                    System.out.printf((i + 1) + ":  %-30s\n", recommendation.getSong().getName());
                else
                    System.out.printf((i + 1) + ": %-30s\n", recommendation.getSong().getName());
                i++;
            }
            if (i == 0)
                System.out.println("No artist recommendations yet. Come back later!");

            System.out.println();

            System.out.println("Songs in your Favorite Genres");
            int j = i;
            while(j < recommendations.length) {
                Recommendation recommendation = recommendations[j];
                if (j < 9)
                    System.out.printf((j + 1) + ":  %-30s\n", recommendation.getSong().getName());
                else
                    System.out.printf((j + 1) + ": %-30s\n", recommendation.getSong().getName());
                j++;
            }
            if (i == j)
                System.out.println("No genre recommendations yet. Come back later!");

            System.out.println();

            System.out.println((recommendations.length + 1) + ": Return to Recommendation Menu");
            System.out.print("Select an Entry: ");
            int input = scanner.nextInt();
            scanner.nextLine();

            if (input < 1 || input > recommendations.length + 1)
                throw new InputMismatchException("Incorrect input given");

            if (input == recommendations.length + 1)
                return -2;

            return recommendations[input - 1].getSong().getSongID();
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect input given. Please try again.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning to Main Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -3;
        }
    }

    private static void makeRecommendationScreen(Connection connection, User user, Song song) {
        clearConsole();
        System.out.print("Please enter the username of the user you want to recommend the song to: ");
        String username = scanner.nextLine();

        User toUser;
        try {
            toUser = MMLTools.getUser(connection, username);
        } catch(SQLException e) {
            System.out.println("Username not found. Returning.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return;
        }

        try {
            String sql = "{call make_recommendation (" + user.getUserID() + ", " + toUser.getUserID() + ", " +
                    song.getSongID() + ")}";
            CallableStatement callableStatement = connection.prepareCall(sql);
            callableStatement.execute();

            System.out.println("Successfully recommended " + song.getName() + " to " + toUser.getName() + ".");
            scanner.nextLine();
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect data entered. Returning.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
        } catch (SQLException e) {
            try {
                int sqlState = Integer.parseInt(e.getSQLState());
                if (sqlState == 23000) { // Integrity constraint violation
                    System.out.println("You already recommended this song to this user. Returning.");
                    scanner.nextLine();
                } else
                    throw new Exception("Other SQL Exception");
            } catch (Exception ex) {
                System.out.println("Error connecting to SQL database. Returning.");
                e.printStackTrace(System.err);
                scanner.nextLine();
            }
        }
    }

    private static void logListenScreen(Connection connection, User user, Song song) {
        clearConsole();
        try {
            System.out.print("How many listens do you want to log for this song? ");
            int input = scanner.nextInt();
            scanner.nextLine();

            if (input < 0)
                throw new InputMismatchException("Number of listens cannot be less than 0.");

            String sql = "{call add_listens (" + user.getUserID() + ", " + song.getSongID() + ", " + input + ")}";
            CallableStatement callableStatement = connection.prepareCall(sql);
            callableStatement.execute();

            sql = "SELECT dbo.get_num_listens (" + user.getUserID() + ", " + song.getSongID() + ")";
            Statement statement = connection.createStatement();
            statement.execute(sql);

            ResultSet resultSet = statement.getResultSet();

            if (resultSet.next()) {
                int numListens = resultSet.getInt(1);
                if (numListens == 1)
                    System.out.println("You have now listened to " + song.getName() + " 1 time.");
                else
                    System.out.println("You have now listened to " + song.getName() + " " + numListens + " times.");

                scanner.nextLine();
            } else
                throw new SQLException("INSERT or UPDATE did not work for listens");
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect data entered. Returning to Recommendation Menu.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning to Recommendation Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
        }
    }

    private static void reviewScreen(Connection connection, User user, Song song) {
        clearConsole();

        int input = -1;
        String review = "";

        try {
            System.out.print("From a scale of 1 to 10, how would you rate '" + song.getName() +
                    "'? Whole number values only: ");
            input = scanner.nextInt();
            scanner.nextLine();

            if (input < 1 || input > 10)
                throw new InputMismatchException("Rating out of bounds");

            System.out.print("Would you like to leave a review? 'y' or 'n': ");
            String line = scanner.nextLine();

            if (line.length() != 1 || (line.charAt(0) != 'y' && line.charAt(0) != 'n'))
                throw new InputMismatchException("Incorrect line input");

            if (line.charAt(0) == 'y') {
                System.out.println("Leave your review below, then press ENTER: ");
                review = scanner.nextLine();
            }

            String sql = "{call make_review (" + user.getUserID() + ", " + song.getSongID() + ", " + input + ", ?)}";
            CallableStatement callableStatement = connection.prepareCall(sql);
            callableStatement.setString(1, review);
            callableStatement.execute();

            System.out.println("Review made. Thank you for your feedback!");
            scanner.nextLine();
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect data entered. Returning.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
        } catch (SQLException e1) {
            try {
                int sqlState = Integer.parseInt(e1.getSQLState());
                if (sqlState == 23000) { // Integrity constraint violation
                    System.out.print("You already rated this song. Would you like to overwrite that review? " +
                            "'y' or 'n': ");
                    String line = scanner.nextLine();

                    if (line.length() != 1 || (line.charAt(0) != 'y' && line.charAt(0) != 'n'))
                        throw new InputMismatchException("Incorrect line input");

                    if (line.charAt(0) == 'y') {
                        String sql = "{call update_review (" + user.getUserID() + ", " + song.getSongID() + ", " +
                                input + ", ?)}";
                        CallableStatement callableStatement = connection.prepareCall(sql);
                        callableStatement.setString(1, review);
                        callableStatement.execute();

                        System.out.println("Review updated. Thank you for your feedback!");
                        scanner.nextLine();
                    }
                } else
                    throw new SQLException(e1);
            } catch (InputMismatchException e2) {
                System.out.println("Incorrect data entered. Returning.");
                scanner = new Scanner(System.in);
                e2.printStackTrace(System.err);
            } catch (Exception e2) {
                System.out.println("Error connecting to SQL database. Returning.");
                e2.printStackTrace(System.err);
                scanner.nextLine();
            }
        }
    }

    /*
     * -3: Error, no repeat
     * -2: No error, no repeat
     * -1: Error, repeat
     * 0: No error, repeat
     * >0: album ID
     */
    private static int albumMenu(Connection connection, Artist artist) {
        clearConsole();
        System.out.println("Album Menu");
        System.out.printf("    %-30s %-12s %-12s\n", "Name", "Song Count", "Duration");

        try {
            Album[] albums = MMLTools.findAlbums(connection, artist);

            int i = 0;
            for (Album album : albums) {
                if (i < 9)
                    System.out.printf((i + 1) + ":  %-30s %-12s %-12s\n", album.getName(),
                            album.getNumSongs(), album.getDuration());
                else
                    System.out.printf((i + 1) + ": %-30s %-12s %-12s\n", album.getName(),
                            album.getNumSongs(), album.getDuration());
                i++;
            }

            if (albums.length == 0) {
                System.out.println("No Albums to Display");
            }

            System.out.println((i + 1) + ": Create New Album");
            System.out.println((i + 2) + ": Return to Album Menu");
            System.out.print("Select an Entry: ");
            int input = scanner.nextInt();
            scanner.nextLine();

            if (input < 1 || input > i + 2)
                throw new InputMismatchException("Incorrect input given");

            if (input == i + 2)
                return -2;
            else if (input == i + 1) {
                createAlbumScreen(connection, artist);
                return 0;
            } else
                return albums[input - 1].getAlbumID();
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect input given. Please try again.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -1;
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL database. Returning to Album Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
            return -3;
        }
    }

    private static void createAlbumScreen(Connection connection, Artist artist) {
        clearConsole();
        try {
            System.out.println("Create New Album");
            System.out.print("What is the name of your album? ");
            String name = scanner.nextLine();

            System.out.print("Enter the names of the genres for the album, separated by commas, no spaces: ");
            String line = scanner.nextLine();
            String[] entries = line.split(",");

            if (entries.length == 0)
                throw new InputMismatchException("No genres specified");

            HashSet<Genre> genreSet = new HashSet<>();
            try {
                for (String entry : entries) {
                    genreSet.add(MMLTools.getGenre(connection, entry));
                }
            } catch (SQLException e) {
                throw new InputMismatchException("Invalid genre input");
            }

            Genre[] genres = new Genre[genreSet.size()];
            genreSet.toArray(genres);

            System.out.print("Enter the usernames of other collaborating artists, separated by commas, no spaces. " +
                    "If you are the sole artist, just press ENTER: ");
            line = scanner.nextLine();
            entries = line.split(",");

            HashSet<Artist> artistSet = new HashSet<>();
            artistSet.add(artist);
            try {
                for (String entry : entries) {
                    if (entry.length() != 0)
                        artistSet.add(MMLTools.getArtist(connection, MMLTools.getUser(connection, entry).getUserID()));
                }
            } catch (SQLException e) {
                throw new InputMismatchException("Invalid artist input");
            }

            Artist[] artists = new Artist[artistSet.size()];
            artistSet.toArray(artists);

            String sql = "BEGIN TRANSACTION [Transaction1]";
            Statement statement = connection.createStatement();
            statement.execute(sql);
            
            sql = "{call make_album (?)}";
            CallableStatement callableStatement = connection.prepareCall(sql);
            callableStatement.setString(1, name);
            callableStatement.execute();

            int albumID;
            sql = "SELECT max(album_id) FROM album";
            statement = connection.createStatement();
            statement.execute(sql);

            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                albumID = resultSet.getInt(1);
            } else
                throw new SQLException("Insertion didn't work");

            for (Artist a : artists) {
                sql = "{call add_album_artist (" + albumID + ", " + a.getUserID() + ")}";
                callableStatement = connection.prepareCall(sql);
                callableStatement.execute();
            }

            for (Genre g : genres) {
                sql = "{call add_album_genre(" + albumID + ", ?)}";
                callableStatement = connection.prepareCall(sql);
                callableStatement.setString(1, g.getName());
                callableStatement.execute();
            }

            sql = "COMMIT TRANSACTION [Transaction1]";
            statement = connection.createStatement();
            statement.execute(sql);

            System.out.println("Album created. Returning to Album Menu.");
            scanner.nextLine();
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect input entered. Returning to Album Menu.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
        } catch (SQLException e) {
            try {
                String sql = "ROLLBACK TRANSACTION [Transaction1]";
                Statement statement = connection.createStatement();
                statement.execute(sql);
            } catch (SQLException ignored) {}     
            System.out.println("Error connecting to SQL database. Returning to Album Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
        }
    }

    private static void createSongScreen(Connection connection, Artist artist) {
        clearConsole();
        try {
            System.out.println("Create New Song");
            System.out.print("What is the name of your song? ");
            String name = scanner.nextLine();

            System.out.print("Enter the names of the genres for the song, separated by commas, no spaces: ");
            String line = scanner.nextLine();
            String[] entries = line.split(",");

            if (entries.length == 0)
                throw new InputMismatchException("No genres specified");

            HashSet<Genre> genreSet = new HashSet<>();
            try {
                for (String entry : entries) {
                    genreSet.add(MMLTools.getGenre(connection, entry));
                }
            } catch (SQLException e) {
                throw new InputMismatchException("Invalid genre input");
            }

            Genre[] genres = new Genre[genreSet.size()];
            genreSet.toArray(genres);

            System.out.print("Enter the usernames of other collaborating artists, separated by commas, no spaces. " +
                    "If you are the sole artist, just press ENTER: ");
            line = scanner.nextLine();
            entries = line.split(",");

            HashSet<Artist> artistSet = new HashSet<>();
            artistSet.add(artist);
            try {
                for (String entry : entries) {
                    if (entry.length() != 0)
                        artistSet.add(MMLTools.getArtist(connection, MMLTools.getUser(connection, entry).getUserID()));
                }
            } catch (SQLException e) {
                throw new InputMismatchException("Invalid artist input");
            }

            Artist[] artists = new Artist[artistSet.size()];
            artistSet.toArray(artists);

            System.out.print("Enter the duration of the song, in number of seconds: ");
            int duration = scanner.nextInt();
            scanner.nextLine();

            if (duration < 0)
                throw new InputMismatchException("Song duration cannot be less than 0");

            String sql = "BEGIN TRANSACTION [Transaction1]";
            Statement statement = connection.createStatement();
            statement.execute(sql);

            sql = "{call make_song (?, " + duration + ")}";
            CallableStatement callableStatement = connection.prepareCall(sql);
            callableStatement.setString(1, name);
            callableStatement.execute();

            int songID;
            sql = "SELECT max(song_id) FROM song";
            statement = connection.createStatement();
            statement.execute(sql);

            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                songID = resultSet.getInt(1);
            } else
                throw new SQLException("Insertion didn't work");

            for (Artist a : artists) {
                sql = "{call add_song_artist (" + songID + ", " + a.getUserID() + ")}";
                callableStatement = connection.prepareCall(sql);
                callableStatement.execute();
            }

            for (Genre g : genres) {
                sql = "{call add_song_genre (" + songID + ", ?)}";
                callableStatement = connection.prepareCall(sql);
                callableStatement.setString(1, g.getName());
                callableStatement.execute();
            }

            sql = "COMMIT TRANSACTION [Transaction1]";
            statement = connection.createStatement();
            statement.execute(sql);

            System.out.println("Song created. Returning to Song Menu.");
            scanner.nextLine();
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Incorrect input entered. Returning to Song Menu.");
            scanner = new Scanner(System.in);
            e.printStackTrace(System.err);
            scanner.nextLine();
        } catch (SQLException e) {
            try {
                String sql = "ROLLBACK TRANSACTION [Transaction1]";
                Statement statement = connection.createStatement();
                statement.execute(sql);
            } catch (SQLException ignored) {}
            System.out.println("Error connecting to SQL database. Returning to Song Menu.");
            e.printStackTrace(System.err);
            scanner.nextLine();
        }
    }
}