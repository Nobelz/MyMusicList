public class Song {
    private final int songID;
    private final int durationValue;
    private final String duration;
    private final String releaseDate;
    private final String name;
    private final Artist[] artists;

    public Song(int songID, int durationValue, String duration, String releaseDate, String name, Artist[] artists) {
        this.songID = songID;
        this.durationValue = durationValue;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.name = name;
        this.artists = artists;
    }

    public int getSongID() {
        return songID;
    }

    public String getDuration() {
        return duration;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getName() {
        return name;
    }

    public Artist[] getArtists() {
        return artists;
    }
}