package control;

import entity.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

public abstract class BaseController {
    // Helper method to escape special characters for CSV format
    protected String escapeCSV(String s) {
        if (s == null) s = "";
        String out = s.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\"") || out.contains("\n") || out.contains("\r")) {
            out = "\"" + out + "\"";
        }
        return out;
    }

    // Helper method to unquote fields
    protected String unquote(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }


    // Methods to read in CSV files
    protected Map<String, Student> loadStudents(Path csvPath) {
        if (!Files.exists(csvPath)) {
            System.err.println("Student CSV not found: " + csvPath);
            return null;
        }

        Map<String, Student> students = new HashMap<>();

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

            return students;
        } catch (IOException e) {
            System.err.println("Failed to read student CSV: " + e.getMessage());
            return null;
        }
    }

    protected Map<String, CareerCenterStaff> loadStaffs(Path csvPath) {
        if (!Files.exists(csvPath)) {
            System.err.println("Staff CSV not found: " + csvPath);
            return null;
        }

        Map<String, CareerCenterStaff> staffs = new HashMap<>();

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

                        CareerCenterStaff staff = new CareerCenterStaff(id, name, pw, email, department, role);
                        staffs.put(id, staff);
                    });

            return staffs;
        } catch (IOException e) {
            System.err.println("Failed to read staff CSV: " + e.getMessage());
            return null;
        }
    }

    protected Map<String, CompanyRepresentative> loadCompanyReps(Path csvPath) {
        if (!Files.exists(csvPath)) {
            System.err.println("Company representative CSV not found: " + csvPath);
            return null;
        }

        Map<String, CompanyRepresentative> companyReps = new HashMap<>();

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

            return companyReps;
        } catch (IOException e) {
            System.err.println("Failed to read company representative CSV: " + e.getMessage());
            return null;
        }
    }

    protected Map<String, Internship> loadInternships(Path csvPath) {
        if (!Files.exists(csvPath)) {
            System.err.println("Internship CSV not found: " + csvPath);
            return null;
        }

        Map<String, Internship> internships = new HashMap<>();

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1) // Skip header
                    .map(line -> line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1))
                    .filter(cols -> cols.length == 12)
                    .forEach(cols -> {
                        String id = unquote(cols[0]);
                        String title = unquote(cols[1]);
                        String description = unquote(cols[2]);
                        String level = unquote(cols[3]);
                        String preferredMajor = unquote(cols[4]);
                        LocalDate openingDate = LocalDate.parse(unquote(cols[5])); // Assumes valid format
                        LocalDate closingDate = LocalDate.parse(unquote(cols[6])); // Assumes valid format
                        String status = unquote(cols[7]);
                        String companyName = unquote(cols[8]);
                        String representatives = unquote(cols[9]);
                        int numberOfSlots = Integer.parseInt(unquote(cols[10]));
                        boolean visibility = Boolean.parseBoolean(unquote(cols[11]));

                        Internship internship = new Internship(UUID.fromString(id), title, description, level, preferredMajor, openingDate, closingDate, status, companyName, representatives, numberOfSlots, visibility);
                        internships.put(id, internship);
                    });

            return internships;
        } catch (IOException e) {
            System.err.println("Failed to read internship CSV: " + e.getMessage());
            return null;
        }
    }

    protected Map<String, List<Application>> loadApplications(Path csvPath) {
        if (!Files.exists(csvPath)) {
            System.err.println("Application CSV not found: " + csvPath);
            return null;
        }

        Map<String, List<Application>> applications = new HashMap<>();

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1) // skip header
                    .map(line -> line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1))
                    .filter(cols -> cols.length == 8) // Must have 8 columns
                    .forEach(cols -> {
                        // This is the Internship's UUID, used as the key for the map
                        String internshipId = unquote(cols[0]);
                        UUID appUuid = UUID.fromString(internshipId);

                        String userId = unquote(cols[1]);
                        String name = unquote(cols[2]);
                        String email = unquote(cols[3]);
                        String major = unquote(cols[4]);
                        int year = Integer.parseInt(unquote(cols[5]));
                        String submittedDate = unquote(cols[6]);
                        String status = unquote(cols[7]);

                        Application application = new Application(appUuid, status, submittedDate, userId, name, email, major, year);

                        // Add it to the map, grouped by its Internship ID
                        applications.putIfAbsent(internshipId, new ArrayList<>());
                        applications.get(internshipId).add(application);
                    });

            return applications;
        } catch (IOException e) {
            System.err.println("Failed to read application CSV: " + e.getMessage());
            return null;
        }
    }

    protected Map<String, List<Withdrawal>> loadWithdrawals(Path csvPath) {
        if (!Files.exists(csvPath)) {
            System.err.println("Withdrawal CSV not found: " + csvPath);
            return null;
        }

        Map<String, List<Withdrawal>> withdrawals = new HashMap<>();

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1) // skip header
                    .map(line -> line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1))
                    .filter(cols -> cols.length == 8) // Must have 8 columns
                    .forEach(cols -> {
                        // This is the Internship's UUID
                        String internshipId = unquote(cols[0]);
                        UUID withUuid = UUID.fromString(internshipId);

                        String userId = unquote(cols[1]);
                        String name = unquote(cols[2]);
                        String email = unquote(cols[3]);
                        String major = unquote(cols[4]);
                        int year = Integer.parseInt(unquote(cols[5]));
                        String submittedDate = unquote(cols[6]);
                        String status = unquote(cols[7]);

                        Withdrawal withdrawal = new Withdrawal(withUuid, status, submittedDate, userId, name, email, major, year);

                        // Add it to the map, grouped by its Internship ID
                        withdrawals.putIfAbsent(internshipId, new ArrayList<>());
                        withdrawals.get(internshipId).add(withdrawal);
                    });

            return withdrawals;
        } catch (IOException e) {
            System.err.println("Failed to read withdrawal CSV: " + e.getMessage());
            return null;
        }
    }


    // Methods to write to CSV files
    protected boolean rewriteStudentCSV(Path csvPath, Map<String, Student> students) {
        List<String> lines = new ArrayList<>();
        // Add header
        lines.add("StudentID,Name,Major,Year,Email,Password");

        // Add data lines from in-memory list
        for (Student student : students.values()) {
            lines.add(String.join(",",
                    escapeCSV(student.getUserID()),
                    escapeCSV(student.getName()),
                    escapeCSV(student.getMajor()),
                    escapeCSV(String.valueOf(student.getYearOfStudy())), // Convert int year to String
                    escapeCSV(student.getEmail()),
                    escapeCSV(student.getPasswordHash()) // Use the (potentially new) password
            ));
        }

        // Write to file, overwriting existing content
        try {
            Files.write(csvPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to rewrite student CSV: " + e.getMessage());
            return false;
        }
    }

    protected boolean rewriteStaffCSV(Path csvPath, Map<String, CareerCenterStaff> staffs) {
        List<String> lines = new ArrayList<>();
        // Add header
        lines.add("StaffID,Name,Role,Department,Email,Password");

        // Add data lines from in-memory list
        for (CareerCenterStaff staff : staffs.values()) {
            lines.add(String.join(",",
                    escapeCSV(staff.getUserID()),
                    escapeCSV(staff.getName()),
                    escapeCSV(staff.getRole()),
                    escapeCSV(staff.getStaffDepartment()),
                    escapeCSV(staff.getEmail()),
                    escapeCSV(staff.getPasswordHash()) // Use the (potentially new) password
            ));
        }

        // Write to file, overwriting existing content
        try {
            Files.write(csvPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to rewrite staff CSV: " + e.getMessage());
            return false;
        }
    }

    protected boolean rewriteCompanyRepCSV(Path csvPath, Map<String, CompanyRepresentative> companyReps) {
        List<String> lines = new ArrayList<>();
        // Add header
        lines.add("CompanyRepID,Name,CompanyName,Department,Position,Email,Status,Password");

        // Add data lines from in-memory list
        for (CompanyRepresentative companyRep : companyReps.values()) {
            lines.add(String.join(",",
                    escapeCSV(companyRep.getUserID()),
                    escapeCSV(companyRep.getName()),
                    escapeCSV(companyRep.getCompanyName()),
                    escapeCSV(companyRep.getDepartment()),
                    escapeCSV(companyRep.getPosition()),
                    escapeCSV(companyRep.getEmail()),
                    escapeCSV(companyRep.getStatus()),
                    escapeCSV(companyRep.getPasswordHash())
            ));
        }

        // Write to file, overwriting existing content
        try {
            Files.write(csvPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to rewrite company representative CSV: " + e.getMessage());
            return false;
        }
    }

    protected boolean rewriteInternshipCSV(Path csvPath, Map<String, Internship> internships) {
        List<String> lines = new ArrayList<>();
        // Add header
        lines.add("UUID,Title,Description,Level,PreferredMajor,OpeningDate,ClosingDate,Status,CompanyName,Representatives,NumberOfSlots,Visibility");

        // Add data lines from in-memory map
        for (Internship internship : internships.values()) {
            lines.add(String.join(",",
                    escapeCSV(internship.getUUID().toString()),
                    escapeCSV(internship.getTitle()),
                    escapeCSV(internship.getDescription()),
                    escapeCSV(internship.getLevel()),
                    escapeCSV(internship.getPreferredMajor()),
                    escapeCSV(internship.getOpeningDate().toString()),
                    escapeCSV(internship.getClosingDate().toString()),
                    escapeCSV(internship.getStatus()),
                    escapeCSV(internship.getCompanyName()),
                    escapeCSV(internship.getRepresentatives()),
                    escapeCSV(String.valueOf(internship.getNumberOfSlots())),
                    escapeCSV(String.valueOf(internship.isVisible())) // "true" or "false"
            ));
        }

        // Write to file, overwriting existing content
        try {
            Files.write(csvPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to rewrite internship CSV: " + e.getMessage());
            return false;
        }
    }

    protected boolean rewriteApplicationCSV(Path csvPath, Map<String, List<Application>> applications) {
        List<String> lines = new ArrayList<>();
        // Add header
        lines.add("UUID,UserId,Name,Email,Major,Year,SubmittedDate,Status");

        // Add data lines from in-memory map
        for (Map.Entry<String, List<Application>> entry : applications.entrySet()) {
            for (Application application : entry.getValue()) {
                lines.add(String.join(",",
                        escapeCSV(application.getUUID().toString()), // 0: UUID (which is the InternshipUUID)
                        escapeCSV(application.getUserId()),          // 1: UserId
                        escapeCSV(application.getName()),            // 2: Name
                        escapeCSV(application.getEmail()),           // 3: Email
                        escapeCSV(application.getMajor()),           // 4: Major
                        escapeCSV(String.valueOf(application.getYear())), // 5: Year
                        escapeCSV(application.getSubmittedDate()),     // 6: SubmittedDate
                        escapeCSV(application.getStatus())             // 7: Status
                ));
            }
        }

        // Write to file, overwriting existing content
        try {
            Files.write(csvPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to rewrite application CSV: " + e.getMessage());
            return false;
        }
    }

    protected boolean rewriteWithdrawalCSV(Path csvPath, Map<String, List<Withdrawal>> withdrawals) {
        List<String> lines = new ArrayList<>();
        // Add header
        lines.add("UUID,UserId,Name,Email,Major,Year,SubmittedDate,Status");

        // Add data lines from in-memory map
        for (Map.Entry<String, List<Withdrawal>> entry : withdrawals.entrySet()) {
            for (Withdrawal withdrawal : entry.getValue()) {
                lines.add(String.join(",",
                        escapeCSV(withdrawal.getUUID().toString()), // 0: UUID (which is the InternshipUUID)
                        escapeCSV(withdrawal.getUserId()),          // 1: UserId
                        escapeCSV(withdrawal.getName()),            // 2: Name
                        escapeCSV(withdrawal.getEmail()),           // 3: Email
                        escapeCSV(withdrawal.getMajor()),           // 4: Major
                        escapeCSV(String.valueOf(withdrawal.getYear())), // 5: Year
                        escapeCSV(withdrawal.getSubmittedDate()),     // 6: SubmittedDate
                        escapeCSV(withdrawal.getStatus())             // 7: Status
                ));
            }
        }

        // Write to file, overwriting existing content
        try {
            Files.write(csvPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to rewrite withdrawal CSV: " + e.getMessage());
            return false;
        }
    }
}
