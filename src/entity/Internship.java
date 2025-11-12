package entity;

import java.util.UUID;

public class Internship {
    private UUID id;
    private String title;
    private String description;
    private String level; //Basic, Intermediate, Advanced
    private String preferredMajor; //Assume 1 preferred major will do
    private String openingDate;
    private String closingDate;
    private String status; //“Pending”, “Approved”, “Rejected”, “Filled”
    private String companyName;
    private String representatives;
    private String numberOfSlots; //max 10
    private boolean visibility; //true or false

    public Internship(String title, String description, String level, String preferredMajor,
                      String openingDate, String closingDate, String status,
                      String companyName, String representatives, String numberOfSlots, boolean visibility) {
        this.id = UUID.randomUUID();
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
        this.visibility = true; //default to true
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

    public String getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(String openingDate) {
        this.openingDate = openingDate;
    }

    public String getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(String closingDate) {
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
