public class Album {

    private final int albumID;
    private final String duration;
    private final String name;
    private final Song[] songs;
    private final Artist[] artists;
    private final Genre[] genres;
    private final boolean canEdit;

    public Album(int albumID, String duration, String name, Song[] songs, Artist[] artists, Genre[] genres, boolean canEdit) {
        this.artists = artists;
        this.albumID = albumID;
        this.duration = duration;
        this.name = name;
        this.songs = songs;
        this.genres = genres;
        this.canEdit = canEdit;
    }

    public Artist[] getArtists() {
        return artists;
    }

    public int getAlbumID() {
        return albumID;
    }

    public int getNumSongs() {
        return songs.length;
    }

    public String getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public Song[] getSongs() {
        return songs;
    }

    public Genre[] getGenres() {
        return genres;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album album = (Album) o;

        return albumID == album.albumID;
    }
}
