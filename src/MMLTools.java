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
                    songs, canEdit);
        } else
            throw new SQLException("Playlist not found");
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

    public static Song getSong(Connection connection, int songID) throws SQLException {
        String sql = "{call get_song_by_id (" + songID + ")}";
        CallableStatement callableStatement = connection.prepareCall(sql);

        ResultSet songResultSet = callableStatement.executeQuery();

        if (songResultSet.next()) {
            sql = "{call get_artists_by_song (" + songID + ")}";
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
                throw new SQLException("No artists found for song");

            return new Song(songID, songResultSet.getInt("duration"),
                    songResultSet.getString("duration_string"),
                    songResultSet.getString("release_date"), songResultSet.getString("name"),
                    artists);
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
}
