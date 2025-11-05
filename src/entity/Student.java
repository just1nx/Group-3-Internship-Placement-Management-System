package entity;

public class Student extends User {
    private int yearOfStudy;
    private String major;
    private int numberOfApplications;

    public Student(String userID, String name, String passwordHash, int yearOfStudy, String major) {
        super(userID, name, passwordHash);
        this.yearOfStudy = yearOfStudy;
        this.major = major;
        this.numberOfApplications = 0;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }

    public String getMajor() {
        return major;
    }

    public int getNumberOfApplications() {
        return numberOfApplications;
    }

    public void apply() {
        numberOfApplications++;
    }

    public void viewApplications() {
        // Able to view the internship he/she applied for, even after visibility is turned off,
        // and the application status (“Pending”, Successful”, or “Unsuccessful”)
    }

    public void acceptApplication() {
        // accepts an internship offer, withdraws from the rest
    }

    public void withdrawalRequest() {
        // requests to withdraw an application
    }

    // Temporary
    @Override
    public String toString() {
        String userID = getUserID();
        String name = getName();
        String passwordHash = getPasswordHash();
        return "Student[id=" + userID
                + ", name=" + (name == null || name.isEmpty() ? "<n/a>" : name)
                + ", major=" + (passwordHash == null || passwordHash.isEmpty() ? "<n/a>" : passwordHash)
                + ", year=" + (yearOfStudy == 0 ? "<n/a>" : yearOfStudy)
                + ", email=" + (major == null || major.isEmpty() ? "<n/a>" : major)
                + "]";
    }
}
