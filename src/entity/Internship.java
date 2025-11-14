package entity;

import java.util.UUID;
import java.time.LocalDate;

public class Internship {
    private UUID id;
    private String title;
    private String description;
    private String level; //Basic, Intermediate, Advanced
    private String preferredMajor; //Assume 1 preferred major will do
    private LocalDate openingDate;
    private LocalDate closingDate;
    private String status; //“Pending”, “Approved”, “Rejected”, “Filled”
    private String companyName;
    private String representatives;
    private String numberOfSlots; //max 10
    private boolean visibility; //true or false

    public Internship(UUID id, String title, String description, String level, String preferredMajor,
                      LocalDate openingDate, LocalDate closingDate, String status,
                      String companyName, String representatives, String numberOfSlots, boolean visibility) {
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
        this.visibility = visibility; //default to false until approved
    }

    public UUID getUUID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getPreferredMajor() {
        return preferredMajor;
    }

    public void setPreferredMajor(String preferredMajor) {
        this.preferredMajor = preferredMajor;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(LocalDate openingDate) {
        this.openingDate = openingDate;
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(LocalDate closingDate) {
        this.closingDate = closingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRepresentatives() {
        return representatives;
    }

    public void setRepresentatives(String representatives) {
        this.representatives = representatives;
    }

    public String getNumberOfSlots() {
        return numberOfSlots;
    }

    public void setNumberOfSlots(String numberOfSlots) {
        this.numberOfSlots = numberOfSlots;
    }

    public void approveInternship() {
        this.status = "Approved";
    }

    public void rejectInternship() {
        this.status = "Rejected";
    }

    public void fillInternship() {
        this.status = "Filled";
    }

    public boolean isVisible() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }
}
