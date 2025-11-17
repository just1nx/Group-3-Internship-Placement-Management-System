package entity;

import java.util.UUID;
import java.time.LocalDate;

/**
 * Represents an internship posting created by a company representative.
 * <p>
 * Contains identifying UUID, descriptive fields (title/description/level/preferredMajor),
 * availability dates, status ("Pending", "Approved", "Rejected", "Filled"),
 * company and representative info, slot count and visibility flag.
 * </p>
 */
public class Internship {
    /**
     * Internship unique identifier.
     */
    private UUID id;

    /**
     * Short human-readable title.
     */
    private String title;

    /**
     * Detailed description of the role.
     */
    private String description;

    /**
     * Internship level (e.g. "Basic", "Intermediate", "Advanced").
     */
    private String level;

    /**
     * Preferred major for candidates.
     */
    private String preferredMajor;

    /**
     * Opening date for applications.
     */
    private LocalDate openingDate;

    /**
     * Closing date for applications.
     */
    private LocalDate closingDate;

    /**
     * Current internship status.
     */
    private String status;

    /**
     * Hiring company name.
     */
    private String companyName;

    /**
     * Comma-separated representative ids/emails for this posting.
     */
    private String representatives;

    /**
     * Number of available slots (max 10).
     */
    private int numberOfSlots;

    /**
     * Visibility flag indicating whether the posting is visible to students.
     */
    private boolean visibility;

    /**
     * Construct an Internship instance.
     *
     * @param id              unique internship UUID
     * @param title           internship title
     * @param description     description text
     * @param level           level string
     * @param preferredMajor  preferred major
     * @param openingDate     opening date
     * @param closingDate     closing date
     * @param status          status string
     * @param companyName     company name
     * @param representatives representative ids/emails
     * @param numberOfSlots   available slots (<=10)
     * @param visibility      visible to students when true
     */
    public Internship(UUID id, String title, String description, String level, String preferredMajor,
                      LocalDate openingDate, LocalDate closingDate, String status,
                      String companyName, String representatives, int numberOfSlots, boolean visibility) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.level = level;
        this.preferredMajor = preferredMajor;
        this.openingDate = openingDate;
        this.closingDate = closingDate;
        this.status = status;
        this.companyName = companyName;
        this.representatives = representatives;
        this.numberOfSlots = numberOfSlots;
        this.visibility = visibility;
    }

    /**
     * Get the internship UUID.
     *
     * @return UUID
     */
    public UUID getUUID() {
        return id;
    }

    /**
     * Get the internship title.
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the internship title.
     *
     * @param title new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description.
     *
     * @param description new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the level.
     *
     * @return level string
     */
    public String getLevel() {
        return level;
    }

    /**
     * Set the level.
     *
     * @param level new level
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * Get preferred major.
     *
     * @return preferred major
     */
    public String getPreferredMajor() {
        return preferredMajor;
    }

    /**
     * Set preferred major.
     *
     * @param preferredMajor new preferred major
     */
    public void setPreferredMajor(String preferredMajor) {
        this.preferredMajor = preferredMajor;
    }

    /**
     * Get opening date.
     *
     * @return opening date
     */
    public LocalDate getOpeningDate() {
        return openingDate;
    }

    /**
     * Set opening date.
     *
     * @param openingDate new opening date
     */
    public void setOpeningDate(LocalDate openingDate) {
        this.openingDate = openingDate;
    }

    /**
     * Get closing date.
     *
     * @return closing date
     */
    public LocalDate getClosingDate() {
        return closingDate;
    }

    /**
     * Set closing date.
     *
     * @param closingDate new closing date
     */
    public void setClosingDate(LocalDate closingDate) {
        this.closingDate = closingDate;
    }

    /**
     * Get current status.
     *
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set current status.
     *
     * @param status new status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get company name.
     *
     * @return company name
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Set company name.
     *
     * @param companyName new company name
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * Get representatives string.
     *
     * @return representatives
     */
    public String getRepresentatives() {
        return representatives;
    }

    /**
     * Set representatives string.
     *
     * @param representatives new representatives value
     */
    public void setRepresentatives(String representatives) {
        this.representatives = representatives;
    }

    /**
     * Get the number of available slots.
     *
     * @return slot count
     */
    public int getNumberOfSlots() {
        return numberOfSlots;
    }

    /**
     * Set the number of available slots (should be <= 10).
     *
     * @param numberOfSlots new slot count
     */
    public void setNumberOfSlots(int numberOfSlots) {
        this.numberOfSlots = numberOfSlots;
    }

    /**
     * Return whether this posting is visible to students.
     *
     * @return true when visible
     */
    public boolean isVisible() {
        return visibility;
    }

    /**
     * Set the visibility flag for this posting.
     *
     * @param visibility true to make visible, false to hide
     */
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }
}
