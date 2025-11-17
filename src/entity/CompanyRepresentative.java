package entity;

/**
 * Represents a company representative user.
 * <p>
 * Extends User with company-specific metadata such as companyName, department,
 * position and an account approval status (e.g. "Pending", "Approved", "Rejected").
 * </p>
 */
public class CompanyRepresentative extends User {
    /**
     * Company the representative belongs to.
     */
    private String companyName;

    /**
     * Department within the company.
     */
    private String department;

    /**
     * Position/title of the representative.
     */
    private String position;

    /**
     * Account approval status ("Pending", "Approved", "Rejected").
     */
    private String status;

    /**
     * Construct a company representative record.
     *
     * @param userID       unique user identifier (email used as ID for reps)
     * @param name         representative name
     * @param passwordHash password hash
     * @param email        representative email
     * @param companyName  company name
     * @param department   department name
     * @param position     position/title
     * @param status       account status
     */
    public CompanyRepresentative(String userID, String name, String passwordHash, String email, String companyName, String department, String position, String status) {
        super(userID, name, passwordHash, email);
        this.companyName = companyName;
        this.department = department;
        this.position = position;
        this.status = status; // Default to not verified
    }

    /**
     * Get the company name.
     *
     * @return company name
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Set the company name.
     *
     * @param companyName new company name
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * Get the department name.
     *
     * @return department
     */
    public String getDepartment() {
        return department;
    }

    /**
     * Set the department name.
     *
     * @param department new department
     */
    public void setDepartment(String department) {
        this.department = department;
    }

    /**
     * Get the position/title.
     *
     * @return position
     */
    public String getPosition() {
        return position;
    }

    /**
     * Set the position/title.
     *
     * @param position new position
     */
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * Get the account approval status.
     *
     * @return status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set the account approval status.
     *
     * @param status new status ("Pending", "Approved", "Rejected")
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
