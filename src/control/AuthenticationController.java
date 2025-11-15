package control;

import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.Student;
import entity.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.nio.file.Path;
import java.util.stream.Stream;

public class AuthenticationController extends BaseController {
    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, CompanyRepresentative> companyReps = new HashMap<>();
    private final Map<String, CareerCenterStaff> staff = new HashMap<>();

    private static final Path studentPath = Paths.get("data/sample_student_list.csv");
    private static final Path companyRepPath = Paths.get("data/sample_company_representative_list.csv");
    private static final Path staffPath = Paths.get("data/sample_staff_list.csv");

    private static final Pattern Email_PATTERN = Pattern.compile("^[A-Za-z0-9+_.\\-]+@[A-Za-z0-9.\\-]+$");

    public AuthenticationController() {
        loadStudents(studentPath);
        loadCompanyReps(companyRepPath);
        loadStaff(staffPath);
    }

    private void loadStudents(Path csvPath) {
        if (!Files.exists(csvPath)) {
            System.err.println("Student CSV not found: " + csvPath);
            return;
        }

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1)
                    .map(line -> line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1))
                    .filter(cols -> cols.length == 6)
                    .forEach(cols -> {
                        String id = unquote(cols[0]);
                        String name = unquote(cols[1]);
                        String major = unquote(cols[2]);
                        int year = Integer.parseInt(unquote(cols[3]));
                        String email = unquote(cols[4]);
                        String pw = unquote(cols[5]);
                        pw = pw.isEmpty() ? "password" : pw;

                        Student student = new Student(id, name, pw, email, year, major);
                        students.put(id, student);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read student CSV: " + e.getMessage());
        }
    }

    private void loadStaff(Path csvPath) {
        if (!Files.exists(csvPath)) {
            System.err.println("Staff CSV not found: " + csvPath);
            return;
        }

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1)
                    .map(line -> line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1))
                    .filter(cols -> cols.length == 6)
                    .forEach(cols -> {
                        String id = unquote(cols[0]);
                        String name = unquote(cols[1]);
                        String role = unquote(cols[2]);
                        String department = unquote(cols[3]);
                        String email = unquote(cols[4]);
                        String pw = unquote(cols[5]);
                        pw = pw.isEmpty() ? "password" : pw;

                        CareerCenterStaff careerStaff = new CareerCenterStaff(id, name, pw, email, department, role);
                        staff.put(id, careerStaff);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read staff CSV: " + e.getMessage());
        }
    }

    private void loadCompanyReps(Path csvPath) {
        if (!Files.exists(csvPath)) {
            System.err.println("Company representatives CSV not found: " + csvPath);
            return;
        }

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1) // Skip header
                    .map(line -> line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1))
                    .filter(cols -> cols.length == 8)
                    .forEach(cols -> {
                        String id = unquote(cols[0]);
                        String name = unquote(cols[1]);
                        String companyName = unquote(cols[2]);
                        String department = unquote(cols[3]);
                        String position = unquote(cols[4]);
                        String email = unquote(cols[5]);
                        String status = unquote(cols[6]);
                        String pw = unquote(cols[7]);
                        pw = pw.isEmpty() ? "password" : pw;

                        CompanyRepresentative companyRep = new CompanyRepresentative(id, name, pw, email, companyName, department, position, status);
                        companyReps.put(id, companyRep);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read company representatives CSV: " + e.getMessage());
        }
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
        return staff.containsKey(staffId);
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
        } else if (isValidStaffId(userID) && Objects.equals(password, staff.get(userID).getPasswordHash())) {
            return staff.get(userID);
        } else {
            System.err.println("Invalid credentials.");
            return null;
        }
    }

    public Boolean register(String email, String name, String password, String companyName, String department, String position) {
        String status = "Pending";

        String line = String.join(",",
                escapeCSV(email),
                escapeCSV(name),
                escapeCSV(companyName),
                escapeCSV(department),
                escapeCSV(position),
                escapeCSV(email),
                escapeCSV(status),
                escapeCSV(password)
        );

        try {
            Files.write(companyRepPath, Collections.singletonList(line), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);

            CompanyRepresentative companyRep = new CompanyRepresentative(email, name, password, companyName, department, position, email, status);
            companyReps.put(email, companyRep);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to write company rep CSV: " + e.getMessage());
            return false;
        }
    }

    // Change password method. Method should return true for a successful password change and false for a failed password change
    public boolean changePassword(User loggedInUser, String newPassword) {
        // 1. Update the password in the in-memory user object
        // (This also updates the object within the 'students', 'companyReps', or 'staff' map)
        loggedInUser.setPasswordHash(newPassword);

        try {
            // 2. Determine user type and call the appropriate write method
            if (loggedInUser instanceof Student) {
                List<String> lines = new ArrayList<>();
                lines.add("StudentID,Name,Major,Year,Email,Password"); // Add header

                // Re-write all students from the 'students' map
                for (Student s : students.values()) {
                    lines.add(String.join(",",
                            escapeCSV(s.getUserID()),
                            escapeCSV(s.getName()),
                            escapeCSV(s.getMajor()),
                            escapeCSV(String.valueOf(s.getYearOfStudy())), // Convert int year to String
                            escapeCSV(s.getEmail()),
                            escapeCSV(s.getPasswordHash()) // Use the (potentially new) password
                    ));
                }
                // Overwrite the file with the new lines
                Files.write(studentPath, lines, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
                return true;

            } else if (loggedInUser instanceof CompanyRepresentative) {
                List<String> lines = new ArrayList<>();
                lines.add("CompanyRepID,Name,CompanyName,Department,Position,Email,Status,Password"); // Add header

                // Re-write all company reps from the 'companyReps' map
                for (CompanyRepresentative r : companyReps.values()) {
                    lines.add(String.join(",",
                            escapeCSV(r.getUserID()),
                            escapeCSV(r.getName()),
                            escapeCSV(r.getCompanyName()),
                            escapeCSV(r.getDepartment()),
                            escapeCSV(r.getPosition()),
                            escapeCSV(r.getEmail()),
                            escapeCSV(r.getStatus()),
                            escapeCSV(r.getPasswordHash()) // Use the (potentially new) password
                    ));
                }
                // Overwrite the file with the new lines
                Files.write(companyRepPath, lines, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
                return true;

            } else if (loggedInUser instanceof CareerCenterStaff) {
                List<String> lines = new ArrayList<>();
                lines.add("StaffID,Name,Role,Department,Email,Password"); // Add header

                // Re-write all staff from the 'staff' map
                for (CareerCenterStaff s : staff.values()) {
                    lines.add(String.join(",",
                            escapeCSV(s.getUserID()),
                            escapeCSV(s.getName()),
                            escapeCSV(s.getRole()),
                            escapeCSV(s.getStaffDepartment()),
                            escapeCSV(s.getEmail()),
                            escapeCSV(s.getPasswordHash()) // Use the (potentially new) password
                    ));
                }
                // Overwrite the file with the new lines
                Files.write(staffPath, lines, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
                return true;

            } else {
                // 3. Handle unknown user types
                System.err.println("Password change failed: Unknown user type.");
                return false;
            }
        } catch (IOException e) {
            // 4. Handle file writing errors
            System.err.println("Failed to write CSV during password change: " + e.getMessage());
            return false; // IO error occurred
        }
    }
    // TODO: Password hashing function
}
