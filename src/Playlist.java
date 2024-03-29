import java.util.Objects;

public class Playlist {
    private final User user;
    private final int playlistID;
    private final String duration;
    private final String name;
    private final Song[] songs;
    private final boolean canEdit;
    private final boolean isPublic;

    public Playlist(User user, int playlistID, int durationValue, String duration, String name, Song[] songs, boolean canEdit, boolean isPublic) {
        this.user = user;
        this.playlistID = playlistID;
        this.duration = duration;
        this.name = name;
        this.songs = songs;
        this.canEdit = canEdit;
        this.isPublic = isPublic;
    }

    public User getUser() {
        return user;
    }

    public int getPlaylistID() {
        return playlistID;
    }

    public String getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public int getNumSongs() {
        return songs.length;
    }

    public Song[] getSongs() {
        return songs;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return playlistID == playlist.playlistID && Objects.equals(user, playlist.user);
    }
}
