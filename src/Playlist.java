public class Playlist {
    private final User user;
    private final int playlistID;
    private final int durationValue;
    private final String duration;
    private final String name;
    private final Song[] songs;
    private final boolean canEdit;

    public Playlist(User user, int playlistID, int durationValue, String duration, String name, Song[] songs, boolean canEdit) {
        this.user = user;
        this.playlistID = playlistID;
        this.durationValue = durationValue;
        this.duration = duration;
        this.name = name;
        this.songs = songs;
        this.canEdit = canEdit;
    }

    public User getUser() {
        return user;
    }

    public int getPlaylistID() {
        return playlistID;
    }

    public int getDurationValue() {
        return durationValue;
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
}
