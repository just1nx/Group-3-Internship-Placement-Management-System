package entity;

public class Student extends User {
    private int yearOfStudy;
    private String major;
    private int numberOfApplications;

    public Student(String userID, String name, String passwordHash, String email, int yearOfStudy, String major) {
        super(userID, name, passwordHash, email);
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
}
