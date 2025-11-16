package control;

import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.Student;
import entity.User;

import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.nio.file.Path;

public class AuthenticationController extends BaseController {
    private final Map<String, Student> students;
    private final Map<String, CompanyRepresentative> companyReps;
    private final Map<String, CareerCenterStaff> staffs;

    private static final Path studentPath = Paths.get("data/sample_student_list.csv");
    private static final Path companyRepPath = Paths.get("data/sample_company_representative_list.csv");
    private static final Path staffPath = Paths.get("data/sample_staff_list.csv");

    private static final Pattern Email_PATTERN = Pattern.compile("^[A-Za-z0-9+_.\\-]+@[A-Za-z0-9.\\-]+$");

    public AuthenticationController() {
        students = loadStudents(studentPath);
        companyReps = loadCompanyReps(companyRepPath);
        staffs = loadStaffs(staffPath);
    }

    public boolean isValidEmail(String email) {
        return Email_PATTERN.matcher(email).matches();
    }

    public boolean isValidStudentId(String studentId) {
        return students.containsKey(studentId);
    }

    public boolean isValidCompanyRepEmail(String email) {
        return companyReps.containsKey(email);
    }

    public boolean isValidStaffId(String staffId) {
        return staffs.containsKey(staffId);
    }

    public User login(String userID, String password) {
        if ((isValidStudentId(userID) && Objects.equals(password, students.get(userID).getPasswordHash()))) {
            return students.get(userID);
        } else if (isValidCompanyRepEmail(userID) && Objects.equals(password, companyReps.get(userID).getPasswordHash())) {
            if (Objects.equals(companyReps.get(userID).getStatus(), "Approved")) {
                return companyReps.get(userID);
            } else {
                System.err.println("Account not approved by staff yet.");
                return null;
            }
        } else if (isValidStaffId(userID) && Objects.equals(password, staffs.get(userID).getPasswordHash())) {
            return staffs.get(userID);
        } else {
            System.err.println("Invalid credentials.");
            return null;
        }
    }

    public Boolean register(String email, String name, String password, String companyName, String department, String position) {
        CompanyRepresentative companyRep = new CompanyRepresentative(email, name, password, companyName, department, position, email, "Pending");
        companyReps.put(email, companyRep);
        return rewriteCompanyRepCSV(companyRepPath, companyReps);
    }

    // Change password method. Method should return true for a successful password change and false for a failed password change
    public boolean changePassword(User loggedInUser, String newPassword) {
        // Update the password in the in-memory user object
        // This also updates the object within the 'students', 'companyReps', or 'staff' map
        loggedInUser.setPasswordHash(newPassword);

        // Determine user type and call the appropriate write method
        switch (loggedInUser) {
            case Student student -> {
                return rewriteStudentCSV(studentPath, students);
            }
            case CompanyRepresentative companyRepresentative -> {
                return rewriteCompanyRepCSV(companyRepPath, companyReps);
            }
            case CareerCenterStaff staff -> {
                return rewriteStaffCSV(staffPath, staffs);
            }
            default -> {
                // 3. Handle unknown user types
                System.err.println("Password change failed: Unknown user type.");
                return false;
            }
        }
    }

    // TODO: Password hashing function
}
