package entity;

public class CareerCenterStaff extends User {
    private String staffDepartment;

    CareerCenterStaff(String userID, String name, String passwordHash, String staffDepartment) {
        super(userID, name, passwordHash);
        this.staffDepartment = staffDepartment;
    }

    public String getStaffDepartment() {
        return staffDepartment;
    }
}
