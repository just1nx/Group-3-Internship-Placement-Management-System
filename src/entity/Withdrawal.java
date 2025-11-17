package entity;

import java.util.UUID;

/**
 * Represents a student's withdrawal request for an application/offer.
 * <p>
 * Similar structure to Application: contains UUID, status, submission date and
 * applicant details. Status values are "Pending", "Successful", or "Unsuccessful".
 * </p>
 */
public class Withdrawal {
    /**
     * Unique identifier for this withdrawal request.
     */
    private UUID id;

    /**
     * Withdrawal request status.
     */
    private String status;

    /**
     * Submission date string (yyyy-MM-dd).
     */
    private String submittedDate;

    /**
     * Student user id who submitted the withdrawal.
     */
    private String userID;

    /**
     * Student name at time of request.
     */
    private String name;

    /**
     * Student email at time of request.
     */
    private String email;

    /**
     * Student major at time of request.
     */
    private String major;

    /**
     * Student year of study at time of request.
     */
    private int year;

    /**
     * Create a withdrawal request record.
     *
     * @param id            unique withdrawal UUID
     * @param status        initial status (e.g. "Pending")
     * @param submittedDate submission date (yyyy-MM-dd)
     * @param userID        student user id
     * @param name          student name
     * @param email         student email
     * @param major         student major
     * @param year          student year of study
     */
    public Withdrawal(UUID id, String status, String submittedDate, String userID,  String name, String email, String major, int year) {
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
     * Get the UUID for this withdrawal.
     *
     * @return UUID
     */
    public UUID getUUID() {
        return id;
    }

    /**
     * Get the withdrawal status.
     *
     * @return status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set the withdrawal status.
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
     * Get the user id who requested the withdrawal.
     *
     * @return user id
     */
    public String getUserId() {
        return userID;
    }

    /**
     * Get the student's name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the student's email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
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
     * Get the student's year of study.
     *
     * @return year
     */
    public int getYear() {
        return year;
    }
}
