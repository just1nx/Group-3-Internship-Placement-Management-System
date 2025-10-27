package entity;

public class Applications {
    private String applicationId;
    private String status; // "Pending", "Successful", "Unsuccessful"
    private String submittedDate;
    private String userId;

    public Applications(String applicationId, String status, String submittedDate, String userId) {
        this.applicationId = applicationId;
        this.status = status;
        this.submittedDate = submittedDate;
        this.userId = userId;
    }

    public String getApplicationId() {
        return applicationId;
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
        return userId;
    }
}
