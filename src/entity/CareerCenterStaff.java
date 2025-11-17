package entity;

/**
 * Represents a Career Centre staff user.
 * <p>
 * Extends User with department and role information for staff that manage approvals
 * and reports within the system.
 * </p>
 */
public class CareerCenterStaff extends User {
    /**
     * Department the staff belongs to.
     */
    private String staffDepartment;

    /**
     * Role/title of the staff (e.g. "Admin", "Staff").
     */
    private String role;

    /**
     * Construct a CareerCenterStaff instance.
     *
     * @param userID         staff user id
     * @param name           staff name
     * @param passwordHash   password hash
     * @param email          staff email
     * @param staffDepartment department name
     * @param role           staff role/title
     */
    public CareerCenterStaff(String userID, String name, String passwordHash, String email, String staffDepartment,  String role) {
        super(userID, name, passwordHash, email);
        this.staffDepartment = staffDepartment;
        this.role = role;
    }

    /**
     * Get the staff department.
     *
     * @return department
     */
    public String getStaffDepartment() {
        return staffDepartment;
    }

    /**
     * Get the staff role/title.
     *
     * @return role
     */
    public String getRole() {
        return role;
    }
}
