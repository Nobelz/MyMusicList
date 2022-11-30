class SearchResult {
    private Entity type;
    private int ID;
    private int secID;
    private String name;

    public SearchResult(Entity type, int ID, int secID, String name) {
        this.type = type;
        this.ID = ID;
        this.secID = secID;
        this.name = name;
    }

    public Entity getType() {
        return type;
    }

    public int getID() {
        return ID;
    }

    public int getSecID() {
        return secID;
    }

    public String getName() {
        return name;
    }

    public enum Entity {
        SONG(1),
        PLAYLIST(2),
        ALBUM(3),
        USER(4),
        ARTIST(5);

        private final int intValue;
        Entity(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }

        @Override
        public String toString() {
            switch (intValue) {
                case 1:
                    return "Song";
                case 2:
                    return "Playlist";
                case 3:
                    return "Album";
                case 4:
                    return "User";
                default:
                    return "Artist";
            }
        }
    }
}




