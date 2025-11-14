package control;

import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.Student;
import entity.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
//import java.util.regex.Pattern;
import java.nio.file.Path;
import java.util.stream.Stream;

public class AuthenticationController {
    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, CompanyRepresentative> companyReps = new HashMap<>();
    private final Map<String, CareerCenterStaff> staff = new HashMap<>();
    //private static final Pattern Email_PATTERN = Pattern.compile("^[A-Za-z0-9+_.\\-]+@[A-Za-z0-9.\\-]+$");

    public AuthenticationController() {
        Path studentPath = Paths.get("data/sample_student_list.csv");
        loadStudents(studentPath);
        Path companyRepPath = Paths.get("data/sample_company_representative_list.csv");
        loadCompanyReps(companyRepPath);
        Path staffPath = Paths.get("data/sample_staff_list.csv");
        loadStaff(staffPath);
    }

    private void loadStudents(Path csvPath) {
        if (csvPath == null || !Files.exists(csvPath)) {
            System.err.println("Student CSV not found: " + csvPath);
            return;
        }

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1)
                    .map(line -> line.split(",", -1))
                    .filter(cols -> cols.length > 0)
                    .forEach(cols -> {
                        String id = cols.length > 0 ? cols[0].trim() : "";
                        if (id.isEmpty()) return;

                        String name = cols.length > 1 ? cols[1].trim() : "";
                        String major = cols.length > 2 ? cols[2].trim() : "";
                        int year = 0;
                        if (cols.length > 3) {
                            try {
                                year = Integer.parseInt(cols[3].trim());
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        String email = cols.length > 4 ? cols[4].trim() : "";
                        String pw = cols.length > 5 && !cols[5].trim().isEmpty() ? cols[5].trim() : "password";

                        Student student = new Student(id, name, pw, email, year, major);
                        students.put(id, student);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read student CSV: " + e.getMessage());
        }
    }

    private void loadStaff(Path csvPath) {
        if (csvPath == null || !Files.exists(csvPath)) {
            System.err.println("Staff CSV not found: " + csvPath);
            return;
        }

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1)
                    .map(line -> line.split(",", -1))
                    .filter(cols -> cols.length > 0)
                    .forEach(cols -> {
                        String id = cols.length > 0 ? cols[0].trim() : "";
                        if (id.isEmpty()) return;

                        String name = cols.length > 1 ? cols[1].trim() : "";
                        String role = cols.length > 2 ? cols[2].trim() : "";
                        String department = cols.length > 3 ? cols[3].trim() : "";
                        String email = cols.length > 4 ? cols[4].trim() : "";
                        String pw = cols.length > 5 && !cols[5].trim().isEmpty() ? cols[5].trim() : "password";

                        CareerCenterStaff careerStaff = new CareerCenterStaff(id, name, pw, email, department, role);
                        staff.put(id, careerStaff);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read staff CSV: " + e.getMessage());
        }
    }

    private void loadCompanyReps(Path csvPath) {
        if (csvPath == null || !Files.exists(csvPath)) {
            System.err.println("Company representatives CSV not found: " + csvPath);
            return;
        }

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1)
                    .map(line -> line.split(",", -1))
                    .filter(cols -> cols.length > 0)
                    .forEach(cols -> {
                        String id = cols.length > 0 ? cols[0].trim() : "";
                        if (id.isEmpty()) return;

                        String name = cols.length > 1 ? cols[1].trim() : "";
                        String companyName = cols.length > 2 ? cols[2].trim() : "";
                        String department = cols.length > 3 ? cols[3].trim() : "";
                        String position = cols.length > 4 ? cols[4].trim() : "";
                        String email = cols.length > 5 ? cols[5].trim() : "";
                        String status = cols.length > 6 ? cols[6].trim() : "";
                        String pw = cols.length > 7 && !cols[7].trim().isEmpty() ? cols[7].trim() : "password";

                        CompanyRepresentative companyRep = new CompanyRepresentative(id, name, pw, email, companyName, department, position, status);
                        companyReps.put(id, companyRep);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read company representatives CSV: " + e.getMessage());
        }
    }

    public boolean isValidStudentId(String studentId) {
        return students.containsKey(studentId);
    }

//    public boolean isValidEmail(String email) {
//        return Email_PATTERN.matcher(email).matches();
//    }

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

        java.util.function.Function<String, String> esc = s -> {
            if (s == null) s = "";
            String out = s.replace("\"", "\"\"");
            if (out.contains(",") || out.contains("\"") || out.contains("\n") || out.contains("\r")) {
                out = "\"" + out + "\"";
            }
            return out;
        };
        String line = String.join(",",
                esc.apply(email),
                esc.apply(name),
                esc.apply(companyName),
                esc.apply(department),
                esc.apply(position),
                esc.apply(email),
                esc.apply(status),
                esc.apply(password)
        );

        Path path = Paths.get("data/sample_company_representative_list.csv");
        try {
            Files.write(path, Collections.singletonList(line), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);

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
                Path path = Paths.get("data/sample_student_list.csv");
                List<String> lines = new ArrayList<>();
                lines.add("StudentID,Name,Major,Year,Email,Password"); // Add header

                // Re-write all students from the 'students' map
                for (Student s : students.values()) {
                    lines.add(String.join(",",
                            s.getUserID(),
                            s.getName(),
                            s.getMajor(),
                            String.valueOf(s.getYearOfStudy()), // Convert int year to String
                            s.getEmail(),
                            s.getPasswordHash() // Use the (potentially new) password
                    ));
                }
                // Overwrite the file with the new lines
                Files.write(path, lines, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
                return true;

            } else if (loggedInUser instanceof CompanyRepresentative) {
                Path path = Paths.get("data/sample_company_representative_list.csv");
                List<String> lines = new ArrayList<>();
                lines.add("CompanyRepID,Name,CompanyName,Department,Position,Email,Status,Password"); // Add header

                // Re-write all company reps from the 'companyReps' map
                for (CompanyRepresentative r : companyReps.values()) {
                    lines.add(String.join(",",
                            r.getUserID(),
                            r.getName(),
                            r.getCompanyName(),
                            r.getDepartment(),
                            r.getPosition(),
                            r.getEmail(),
                            r.getStatus(),
                            r.getPasswordHash() // Use the (potentially new) password
                    ));
                }
                // Overwrite the file with the new lines
                Files.write(path, lines, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
                return true;

            } else if (loggedInUser instanceof CareerCenterStaff) {
                Path path = Paths.get("data/sample_staff_list.csv");
                List<String> lines = new ArrayList<>();
                lines.add("StaffID,Name,Role,Department,Email,Password"); // Add header

                // Re-write all staff from the 'staff' map
                for (CareerCenterStaff s : staff.values()) {
                    lines.add(String.join(",",
                            s.getUserID(),
                            s.getName(),
                            s.getRole(),
                            s.getStaffDepartment(),
                            s.getEmail(),
                            s.getPasswordHash() // Use the (potentially new) password
                    ));
                }
                // Overwrite the file with the new lines
                Files.write(path, lines, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
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
