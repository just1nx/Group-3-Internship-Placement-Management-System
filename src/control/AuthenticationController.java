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

public class AuthenticationController {

    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, String> studentpasswords = new HashMap<>();

    private final Map<String, CompanyRepresentative> companyReps = new HashMap<>();
    private final Map<String, String> companyPasswords = new HashMap<>();

    private final Map<String, CareerCenterStaff> staff = new HashMap<>();
    private final Map<String, String> staffPasswords = new HashMap<>();

    private static final Pattern Email_PATTERN = Pattern.compile("^[A-Za-z0-9+_.\\-]+@[A-Za-z0-9.\\-]+$");

    public AuthenticationController() {
    }

    private void loadStudents(Path csvPath) {
        if (csvPath == null || !Files.exists(csvPath)) {
            System.err.println("CSV not found: " + csvPath);
            return;
        }
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
                        String pw = cols.length > 5 && !cols[5].trim().isEmpty() ? cols[5].trim() : id;

                        Student student = new Student(id, name, pw, year, major);
                        students.put(id, student);
                        studentpasswords.put(id, pw);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read CSV: " + e.getMessage());
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
                        String pw = cols.length > 5 && !cols[5].trim().isEmpty() ? cols[5].trim() : id;

                        // using User for staff; extend if you have a Staff class
                        CareerCenterStaff careerstaff = new CareerCenterStaff(id, name, pw, department);
                        staff.put(id, careerstaff);
                        staffPasswords.put(id, pw);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read staff CSV: " + e.getMessage());
        }
    }

    private void loadCompanyReps(Path csvPath) {
        if (csvPath == null || !Files.exists(csvPath)) {
            System.err.println("Company reps CSV not found: " + csvPath);
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
                        String pw = cols.length > 7 && !cols[7].trim().isEmpty() ? cols[7].trim() : id;

                        // using User for company reps; extend if you have a CompanyRep class
                        CompanyRepresentative companyrep = new CompanyRepresentative(id, name, pw, companyName, department, position, email, "Pending");
                        companyReps.put(id, companyrep);
                        companyPasswords.put(id, pw);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read company reps CSV: " + e.getMessage());
        }
    }

    public boolean isValidStudentId(String studentId) {
        return students.containsKey(studentId);
    }

    public boolean isValidEmail(String email) {
        return Email_PATTERN.matcher(email).matches();
    }

    public boolean isValidCompanyRepEmail(String email) {
        return companyReps.containsKey(email);
    }

    public boolean isValidStaffId(String staffId) {
        return staff.containsKey(staffId);
    }

    public User login(String UserId, String password) {
        Path studentPath = Paths.get("data/sample_student_list.csv");
        loadStudents(studentPath);
        Path companyrepPath = Paths.get("data/sample_company_representative_list.csv");
        loadCompanyReps(companyrepPath);
        Path staffPath = Paths.get("data/sample_staff_list.csv");
        loadStaff(staffPath);
        if ((isValidStudentId(UserId) && Objects.equals(password, studentpasswords.get(UserId)))) {
            Student s = students.get(UserId);
            return s;
        } else if (isValidCompanyRepEmail(UserId) && Objects.equals(password, companyPasswords.get(UserId))) {
            CompanyRepresentative cr = companyReps.get(UserId);
            return cr;
        } else if (isValidStaffId(UserId) && Objects.equals(password, staffPasswords.get(UserId))) {
            CareerCenterStaff ccs = staff.get(UserId);
            return ccs;
        } else {
            return null; // need to add for company rep and career center staff
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

            // update in-memory maps using User to avoid constructor signature issues
            CompanyRepresentative companyrep = new CompanyRepresentative(email, name, password, companyName, department, position, email, status);
            companyReps.put(email, companyrep);
            companyPasswords.put(email, password);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to write company rep CSV: " + e.getMessage());
            return false;
        }
    }

    public void printStudentsDetailed() {
        for (Student s : students.values()) {
            System.out.println(s);
        }
    }
}

//    public static void main(String[] args) {
//        Path csvPath = Paths.get("/Users/ben/IdeaProjects/Group-3-Internship-Placement-Management-System/data/sample_student_list.csv");
//        AuthenticationController authController = new AuthenticationController(csvPath);
//        // print only IDs:
//        authController.printStudentsDetailed();
//        // print full student values:
//
//    }
//    public static void main(String[] args) {
//        AuthenticationController authController = new AuthenticationController();
//        Path studentCsvPath = Paths.get("data/sample_student_list.csv");
//        authController.register("123@gmail.com", "John Doe", "password123", "TechCorp", "Engineering", "Manager");
//    }
//public static void main(String[] args) {
//    AuthenticationController authController = new AuthenticationController();
//    Path studentCsvPath = Paths.get("data/sample_student_list.csv");
//    authController.login("U2310001A", "password");
//    authController.login("sng001", "password");
//}


