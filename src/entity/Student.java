package entity;

public class Student extends User {
    private int yearOfStudy;
    private String major;
    private int numberOfApplications;

    public Student(String id, String password, int yearOfStudy, String major) {
        super(id, password);
        this.yearOfStudy = yearOfStudy;
        this.major = major;
        this.numberOfApplications = 0;
    }

    public void applyApplications() {
        numberOfApplications++;
    }

    public void viewApplications() {
        //Able to view the internship he/she applied for, even after visibility is turned off,
        // and the application status (“Pending”, Successful”, or “Unsuccessful”)
    }

    public void acceptApplication() {
        //accepts an internship offer, withdraws from the rest
    }

    public void requestWithdrawal() {
        //requests to withdraw an application
    }

}
