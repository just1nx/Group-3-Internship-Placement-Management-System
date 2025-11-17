package entity;

/**
 * Represents a student user of the internship placement system.
 * <p>
 * Stores academic attributes such as yearOfStudy and major, and tracks the number
 * of applications submitted by the student.
 * </p>
 */
public class Student extends User {
    /**
     * Current year of study (e.g. 1..4).
     */
    private int yearOfStudy;

    /**
     * Student's major.
     */
    private String major;

    /**
     * Current count of applications the student has submitted.
     */
    private int numberOfApplications;

    /**
     * Create a Student record.
     *
     * @param userID       student user id
     * @param name         student name
     * @param passwordHash password hash
     * @param email        student email
     * @param yearOfStudy  year of study
     * @param major        student major
     */
    public Student(String userID, String name, String passwordHash, String email, int yearOfStudy, String major) {
        super(userID, name, passwordHash, email);
        this.yearOfStudy = yearOfStudy;
        this.major = major;
        this.numberOfApplications = 0;
    }

    /**
     * Get the year of study.
     *
     * @return yearOfStudy
     */
    public int getYearOfStudy() {
        return yearOfStudy;
    }

    /**
     * Get the student's major.
     *
     * @return major
     */
    public String getMajor() {
        return major;
    }

    /**
     * Get the number of applications submitted by the student.
     *
     * @return numberOfApplications
     */
    public int getNumberOfApplications() {
        return numberOfApplications;
    }

    /**
     * Increment the student's application count. Should be called when a new application is recorded.
     */
    public void apply() {
        numberOfApplications++;
    }

    /**
     * Placeholder: view the student's applications.
     * <p>
     * The concrete UI/controller logic shows details to the student; this method is a domain stub.
     * </p>
     */
    public void viewApplications() {
        // Able to view the internship he/she applied for, even after visibility is turned off,
        // and the application status (“Pending”, Successful”, or “Unsuccessful”)
    }

    /**
     * Placeholder: accept an offered internship and trigger withdrawal/cleanup of other applications.
     * <p>
     * The controller implements the full workflow; this remains a domain-level stub.
     * </p>
     */
    public void acceptApplication() {
        // accepts an internship offer, withdraws from the rest
    }

    /**
     * Placeholder: request withdrawal for an application.
     * <p>
     * The controller handles persistence and request lifecycle; this is a stub.
     * </p>
     */
    public void withdrawalRequest() {
        // requests to withdraw an application
    }
}
