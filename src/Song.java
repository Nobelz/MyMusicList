public class Song {
    private final int songID;
    private final int durationValue;
    private final String duration;
    private final String releaseDate;
    private final String name;
    private final Artist[] artists;
    private final Genre[] genres;

    public Song(int songID, int durationValue, String duration, String releaseDate, String name, Artist[] artists, Genre[] genres) {
        this.songID = songID;
        this.durationValue = durationValue;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.name = name;
        this.artists = artists;
        this.genres = genres;
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

    public Genre[] getGenres() {
        return genres;
    }

    public boolean isArtist(User user) {
        for (Artist artist : getArtists()) {
            if (user.getUserID() == artist.getUserID())
                return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        return songID == song.songID;
    }
}