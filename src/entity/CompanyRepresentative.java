package entity;

public class CompanyRepresentative extends User {
    private String companyName;
    private String department;
    private String position;
    private String email;
    private boolean isVerified;

    public CompanyRepresentative(String name, String password, String companyName, String department, String position, String email) {
        super(userId, name, password);
        this.companyName = companyName;
        this.department = department;
        this.position = position;
        this.email = email;
        this.isVerified = false; // Default to not verified
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

}
