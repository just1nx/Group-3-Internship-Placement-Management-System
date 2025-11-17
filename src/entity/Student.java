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

}
