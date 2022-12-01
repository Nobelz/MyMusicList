public class Recommendation {
    private final boolean isAutoGenerated;
    private final Song song;
    private final User fromUser;
    private final User toUser;

    public Recommendation(boolean isAutoGenerated, Song song, User fromUser, User toUser) {
        this.isAutoGenerated = isAutoGenerated;
        this.song = song;
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

    public boolean isAutoGenerated() {
        return isAutoGenerated;
    }

    public Song getSong() {
        return song;
    }

    public User getFromUser() {
        return fromUser;
    }

    public User getToUser() {
        return toUser;
    }
}
