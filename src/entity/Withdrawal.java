package entity;

import java.util.UUID;

public class Withdrawal {
    private UUID id;
    private String status; // "Pending", "Successful", "Unsuccessful"
    private String submittedDate;
    private String userID;
    private String name;
    private String email;
    private String major;
    private int year;

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

    public UUID getUUID() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
    }

    public String getUserId() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMajor() {
        return major;
    }

    public int getYear() {
        return year;
    }
}
