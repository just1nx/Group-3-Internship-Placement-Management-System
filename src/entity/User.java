package entity;

public abstract class User {
    private String userID;
    private String name;
    private String passwordHash;
    private String email;

    public User(String userID, String name, String passwordHash, String email) {
        this.userID = userID;
        this.name = name;
        this.passwordHash = passwordHash;
        this.email = email;
    }

    public String getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }
}
