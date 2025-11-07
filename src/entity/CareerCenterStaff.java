package entity;

public class CareerCenterStaff extends User {
    private String staffDepartment;
    private String role;

    public CareerCenterStaff(String userID, String name, String passwordHash, String email, String staffDepartment,  String role) {
        super(userID, name, passwordHash, email);
        this.staffDepartment = staffDepartment;
        this.role = role;
    }

    public String getStaffDepartment() {
        return staffDepartment;
    }

    public String getRole() {
        return role;
    }
}
