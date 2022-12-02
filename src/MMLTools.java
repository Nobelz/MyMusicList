import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class MMLTools {
    public static Playlist getPlaylist(Connection connection, int userID, int playlistID, boolean canEdit) throws SQLException {
        String sql = "{call get_playlist_by_id (" + userID + ", " + playlistID + ")}";
        CallableStatement callableStatement = connection.prepareCall(sql);
        ResultSet playlistResultSet = callableStatement.executeQuery();

        if (playlistResultSet.next()) {
            User user = getUser(connection, userID);

            sql = "{call get_songs_by_playlist (" + userID + ", " + playlistID + ")}";
            callableStatement = connection.prepareCall(sql);

            ResultSet songResultSet = callableStatement.executeQuery();

            LinkedList<Song> songList = new LinkedList<>();
            int j = 0;
            while (songResultSet.next()) {
                songList.add(getSong(connection, songResultSet.getInt(1)));
                j++;
            }

            Song[] songs = new Song[j];
            songList.toArray(songs);

            return new Playlist(user, playlistID, playlistResultSet.getInt("duration_value"),
                    playlistResultSet.getString("duration"), playlistResultSet.getString("name"),
                    songs, canEdit, playlistResultSet.getString("is_public").equals("y"));
        } else
            throw new SQLException("Playlist not found");
    }

    public static Album getAlbum(Connection connection, int albumID, boolean canEdit) throws SQLException {
        String sql = "{call get_album_by_id (" + albumID + ")}";
        CallableStatement callableStatement = connection.prepareCall(sql);
        ResultSet albumResultSet = callableStatement.executeQuery();

        if (albumResultSet.next()) {
            sql = "{call get_songs_by_album (" + albumID + ")}";
            callableStatement = connection.prepareCall(sql);
            ResultSet songResultSet = callableStatement.executeQuery();

            LinkedList<Song> songList = new LinkedList<>();
            int i = 0;
            while (songResultSet.next()) {
                songList.add(getSong(connection, songResultSet.getInt(1)));
                i++;
            }

            Song[] songs = new Song[i];
            songList.toArray(songs);

            sql = "{call get_artists_by_album (" + albumID + ")}";
            callableStatement = connection.prepareCall(sql);
            ResultSet artistResultSet = callableStatement.executeQuery();

            LinkedList<Artist> artistList = new LinkedList<>();
            int j = 0;
            while (artistResultSet.next()) {
                artistList.add(getArtist(connection, artistResultSet.getInt(1)));
                j++;
            }

            Artist[] artists = new Artist[j];
            artistList.toArray(artists);

            if (artists.length == 0)
                throw new SQLException("No artists found for album");

            sql = "{call get_genres_by_album (" + albumID + ")}";
            callableStatement = connection.prepareCall(sql);
            ResultSet genreResultSet = callableStatement.executeQuery();

            LinkedList<Genre> genreList = new LinkedList<>();
            int k = 0;
            while (genreResultSet.next()) {
                genreList.add(getGenre(connection, genreResultSet.getString(1)));
                k++;
            }

            Genre[] genres = new Genre[k];
            genreList.toArray(genres);

            if (genres.length == 0)
                throw new SQLException("No genres found for album");

            return new Album(albumID, albumResultSet.getString("duration"),
                    albumResultSet.getString("name"), songs, artists, genres, canEdit);
        } else
            throw new SQLException("Playlist not found");
    }

    public static Genre getGenre(Connection connection, String name) throws SQLException {
        String sql = "{call get_genre_by_name (?)}";
        CallableStatement callableStatement = connection.prepareCall(sql);
        callableStatement.setString(1, name);
        ResultSet resultSet = callableStatement.executeQuery();

        if (resultSet.next())
            return new Genre(name, resultSet.getString("description"));
        else
            throw new SQLException("Genre not found");
    }

    public static User getUser(Connection connection, int userID) throws SQLException {
        String sql = "{call get_user_by_id (" + userID + ")}";
        CallableStatement callableStatement = connection.prepareCall(sql);

        ResultSet resultSet = callableStatement.executeQuery();

        if (resultSet.next())
            return new User(userID, resultSet.getString("username"), resultSet.getString("name"),
                    resultSet.getString("join_date"));
        else
            throw new SQLException("User not found");
    }

    public static User getUser(Connection connection, String username) throws SQLException {
        String sql = "{call get_user_by_username (" + username + ")}";
        CallableStatement callableStatement = connection.prepareCall(sql);

        ResultSet resultSet = callableStatement.executeQuery();

        if (resultSet.next())
            return new User(resultSet.getInt(1), username, resultSet.getString("name"),
                    resultSet.getString("join_date"));
        else
            throw new SQLException("User not found");
    }

    public static Song getSong(Connection connection, int songID) throws SQLException {
        String sql = "{call get_song_by_id (" + songID + ")}";
        CallableStatement callableStatement = connection.prepareCall(sql);

        ResultSet songResultSet = callableStatement.executeQuery();

        if (songResultSet.next()) {
            sql = "{call get_artists_by_song (" + songID + ")}";
            callableStatement = connection.prepareCall(sql);

            ResultSet artistResultSet = callableStatement.executeQuery();

            LinkedList<Artist> artistList = new LinkedList<>();
            int i = 0;
            while (artistResultSet.next()) {
                artistList.add(getArtist(connection, artistResultSet.getInt(1)));
                i++;
            }

            Artist[] artists = new Artist[i];
            artistList.toArray(artists);

            if (artists.length == 0)
                throw new SQLException("No artists found for song");

            sql = "{call get_genres_by_song (" + songID + ")}";
            callableStatement = connection.prepareCall(sql);
            ResultSet genreResultSet = callableStatement.executeQuery();

            LinkedList<Genre> genreList = new LinkedList<>();
            int j = 0;
            while (genreResultSet.next()) {
                genreList.add(getGenre(connection, genreResultSet.getString(1)));
                j++;
            }

            Genre[] genres = new Genre[j];
            genreList.toArray(genres);

            if (genres.length == 0)
                throw new SQLException("No genres found for song");

            return new Song(songID, songResultSet.getInt("duration"),
                    songResultSet.getString("duration_string"),
                    songResultSet.getString("release_date"), songResultSet.getString("name"),
                    artists, genres);
        } else
            throw new SQLException("Song not found");
    }

    public static Artist getArtist(Connection connection, int artistID) throws SQLException {
        String sql = "{call get_artist_by_id (" + artistID + ")}";
        CallableStatement callableStatement = connection.prepareCall(sql);

        ResultSet resultSet = callableStatement.executeQuery();

        if (resultSet.next())
            return new Artist(artistID, resultSet.getString("username"),
                    resultSet.getString("name"), resultSet.getString("join_date"));
        else
            throw new SQLException("User not found");
    }

    public static Recommendation getRecommendation(Connection connection, String type, int fromID, int toID, int songID, boolean isAutoGenerated) throws SQLException {
        return new Recommendation(isAutoGenerated, type, getSong(connection, songID),
                (fromID == 0) ? null : getUser(connection, fromID), getUser(connection, toID));
    }

    public static Playlist[] findPlaylists(Connection connection, User user) throws SQLException {
        String sql = "{call view_playlists (" + user.getUserID() + ", 'n')}";
        CallableStatement callableStatement = connection.prepareCall(sql);
        ResultSet resultSet = callableStatement.executeQuery();

        LinkedList<Playlist> playlistList = new LinkedList<>();

        int i = 0;
        while (resultSet.next()) {
            int playlistID = resultSet.getInt(1);
            Playlist playlist = getPlaylist(connection, user.getUserID(), playlistID, true);
            playlistList.add(playlist);
            i++;
        }

        Playlist[] playlists = new Playlist[i];
        playlistList.toArray(playlists);

        return playlists;
    }

    public static Album[] findAlbums(Connection connection, Artist artist) throws SQLException {
        String sql = "{call view_albums (" + artist.getUserID() + ")}";
        CallableStatement callableStatement = connection.prepareCall(sql);
        ResultSet resultSet = callableStatement.executeQuery();

        LinkedList<Album> albumList = new LinkedList<>();

        int i = 0;
        while (resultSet.next()) {
            int albumID = resultSet.getInt(1);
            Album album = getAlbum(connection, albumID, true);
            albumList.add(album);
            i++;
        }

        Album[] albums = new Album[i];
        albumList.toArray(albums);

        return albums;
    }

    public static Recommendation[] findPersonalRecommendations(Connection connection, User user) throws SQLException {
        String sql = "{call view_user_recommendations (" + user.getUserID() + ")}";
        CallableStatement callableStatement = connection.prepareCall(sql);
        ResultSet resultSet = callableStatement.executeQuery();

        LinkedList<Recommendation> recommendationList = new LinkedList<>();

        int i = 0;
        while (resultSet.next()) {
            Recommendation recommendation = getRecommendation(connection, "user", resultSet.getInt(3),
                    user.getUserID(), resultSet.getInt(1), false);
            recommendationList.add(recommendation);
            i++;
        }

        Recommendation[] recommendations = new Recommendation[i];
        recommendationList.toArray(recommendations);

        return recommendations;
    }

    public static Recommendation[] findAutoGeneratedRecommendations(Connection connection, User user) throws SQLException {
        String sql = "{call view_auto_recommendations (" + user.getUserID() + ", 10)}";
        CallableStatement callableStatement = connection.prepareCall(sql);
        ResultSet resultSet = callableStatement.executeQuery();

        LinkedList<Recommendation> recommendationList = new LinkedList<>();

        int i = 0;
        while (resultSet.next()) {
            Recommendation recommendation = getRecommendation(connection, resultSet.getString(1), 0,
                    user.getUserID(), resultSet.getInt(2), false);
            recommendationList.add(recommendation);
            i++;
        }

        Recommendation[] recommendations = new Recommendation[i];
        recommendationList.toArray(recommendations);

        return recommendations;
    }
}
