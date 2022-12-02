public class User {
    private int userID;
    private String username;
    private String name;
    private String date;

    public User(int userID, String username, String name, String date) {
        this.userID = userID;
        this.username = username;
        this.name = name;
        this.date = date;
    }

    public int getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return userID == user.userID;
    }

    @Override
    public int hashCode() {
        return userID;
    }
}
