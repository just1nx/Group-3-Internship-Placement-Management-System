package control;

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
    private final Map<String, String> passwords = new HashMap<>();
    private static final Pattern Email_PATTERN = Pattern.compile("^[A-Za-z0-9+_.\\-]+@[A-Za-z0-9.\\-]+$");

    public AuthenticationController() {
        Path csvPath = Paths.get("/Users/ben/IdeaProjects/Group-3-Internship-Placement-Management-System/data/sample_student_list.csv");
        loadStudents(csvPath);
    }

    private void loadStudents(Path csvPath) {
        if (csvPath == null || !Files.exists(csvPath)) {
            System.err.println("CSV not found: " + csvPath);
            return;
        }
        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1) // skip header if present
                    .map(line -> line.split(",", -1))
                    .filter(cols -> cols.length > 0)
                    .forEach(cols -> {
                        String id = cols[0].trim();
                        if (id.isEmpty()) return;
                        String name = cols.length > 1 ? cols[1].trim() : "";
                        String pw = cols.length > 2 && !cols[2].trim().isEmpty() ? cols[2].trim() : id; // fallback to id
                        int year = 0;
                        if (cols.length > 3) {
                            try {
                                year = Integer.parseInt(cols[3].trim());
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        String major = cols.length > 4 ? cols[4].trim() : "";
                        // construct Student from CSV fields (adjust constructor params if entity.Student differs)
                        Student student = new Student(id, name, pw, year, major);
                        students.put(id, student);
                        passwords.put(id, pw);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read CSV: " + e.getMessage());
        }
    }

    public boolean isValidStudentId(String studentId) {
        return students.containsKey(studentId);
    }

    public boolean isValidEmail(String email) {
        return Email_PATTERN.matcher(email).matches();
    }

    public User login(String studentId, String password) {
        if ((isValidStudentId(studentId) && Objects.equals("password", password))) {
            Student s = students.get(studentId);
            return s;
        } else {
            return null; // need to add for company rep and career center staff
        }
    }

    public void printStudentsDetailed() {
        for (Student s : students.values()) {
            System.out.println(s);
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


}


