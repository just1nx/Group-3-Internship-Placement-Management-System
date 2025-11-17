package entity;

/**
 * Abstract base class for all user types in the system.
 * <p>
 * Stores common authentication/profile fields: userID, name, passwordHash and email.
 * Subclasses represent concrete roles (Student, CompanyRepresentative, CareerCenterStaff).
 * </p>
 */
public abstract class User {
    /**
     * Unique user identifier.
     */
    private String userID;

    /**
     * Full name of the user.
     */
    private String name;

    /**
     * Password hash stored for authentication.
     */
    private String passwordHash;

    /**
     * Contact email for the user.
     */
    private String email;

    /**
     * Construct a basic User record.
     *
     * @param userID       unique user id
     * @param name         full name
     * @param passwordHash password hash
     * @param email        email address
     */
    public User(String userID, String name, String passwordHash, String email) {
        this.userID = userID;
        this.name = name;
        this.passwordHash = passwordHash;
        this.email = email;
    }

    /**
     * Get the user id.
     *
     * @return userID
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Get the user's full name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the stored password hash.
     *
     * @return passwordHash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Set the stored password hash.
     *
     * @param passwordHash new password hash value
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Get the user's email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }
}
