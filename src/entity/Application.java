package entity;

import java.util.UUID;

/**
 * Represents a student's application to an internship posting.
 * <p>
 * Contains metadata about the application such as status, submission date and
 * applicant details (id/name/email/major/year). The UUID identifies the application.
 * </p>
 */
public class Application {
    /**
     * Unique identifier for this application.
     */
    private UUID id;

    /**
     * Application status: "Pending", "Successful", or "Unsuccessful".
     */
    private String status;

    /**
     * Submission date in string form (formatted yyyy-MM-dd).
     */
    private String submittedDate;

    /**
     * Student user id who submitted this application.
     */
    private String userID;

    /**
     * Student full name at time of submission.
     */
    private String name;

    /**
     * Student email at time of submission.
     */
    private String email;

    /**
     * Student major at time of submission.
     */
    private String major;

    /**
     * Student year of study at time of submission.
     */
    private int year;

    /**
     * Construct a new Application instance.
     *
     * @param id            unique application UUID
     * @param status        initial status (e.g. "Pending")
     * @param submittedDate submission date as string (yyyy-MM-dd)
     * @param userID        student user id
     * @param name          student name
     * @param email         student email
     * @param major         student major
     * @param year          student year of study
     */
    public Application(UUID id, String status, String submittedDate, String userID,  String name, String email, String major, int year) {
        this.id = id;
        this.status = status;
        this.submittedDate = submittedDate;
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.major = major;
        this.year = year;
    }

    /**
     * Get the UUID of this application.
     *
     * @return application UUID
     */
    public UUID getUUID() {
        return id;
    }

    /**
     * Get the application status.
     *
     * @return status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Update the application status.
     *
     * @param status new status value
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get the submission date string.
     *
     * @return submitted date
     */
    public String getSubmittedDate() {
        return submittedDate;
    }

    /**
     * Set the submission date string.
     *
     * @param submittedDate new submission date (yyyy-MM-dd)
     */
    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
    }

    /**
     * Return the applicant's user id.
     *
     * @return user id
     */
    public String getUserId() {
        return userID;
    }

    /**
     * Return the applicant's name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the applicant's email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Return the applicant's major.
     *
     * @return major
     */
    public String getMajor() {
        return major;
    }

    /**
     * Return the applicant's year of study.
     *
     * @return year
     */
    public int getYear() {
        return year;
    }
}
