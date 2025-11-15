package control;

import entity.Internship;
import entity.Student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

public class StudentController {
    private final Map<String, Internship> internships = new HashMap<>();

    // Define the path to the application and internship CSV file
    private static final Path applicationPath = Paths.get("data/sample_application_list.csv");
    private static final Path internshipPath = Paths.get("data/sample_internship_list.csv");

    private static final int maxApplication = 3;

    public StudentController(){
        loadInternships(internshipPath);
    }

    private void loadInternships(Path csvPath) {
        if (!Files.exists(internshipPath)) {
            System.err.println("Internship CSV not found: " + csvPath);
            return;
        }

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
        } catch (IOException e) {
            System.err.println("Failed to read internship CSV: " + e.getMessage());
        }
    }

    // Helper method to escape special characters for CSV format
    private String escapeCSV(String s) {
        if (s == null) s = "";
        String out = s.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\"") || out.contains("\n") || out.contains("\r")) {
            out = "\"" + out + "\"";
        }
        return out;
    }

    // Helper method to unquote fields
    private String unquote(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }

    public List<Internship> getInternshipsForStudents() {
        List<Internship> safeList = new ArrayList<>();
        for (Internship i : this.internships.values()){
            boolean isApproved = "Approved".equalsIgnoreCase(i.getStatus());
            boolean isVisible = i.isVisible();

            if (isApproved && isVisible) {
                safeList.add(i);
            }
        }
        return safeList;
    }

    public int getApplicationCount(String studentID){
        if (!Files.exists(applicationPath)) {
            return 0; // File doesn't exist yet, so count is 0
        }

        try (Stream<String> lines = Files.lines(applicationPath)) {
            int result = (int) lines.skip(1)
                    .map(line -> line.split(",", -1))
                    .filter(cols -> cols.length > 1)
                    .filter(cols -> cols.length > 1 && cols[1].trim().replace("\"", "").equals(studentID))
                    .count(); // Count the number of matching lines

            return result;
        }  catch (IOException e) {
            System.err.println("Error counting applications: " + e.getMessage());
            return maxApplication;
        }
    }

    public List<Internship> getFilteredInternships(Student currentStudent){
        List<Internship> matches = new ArrayList<>();
        List<Internship> availableInternships = getInternshipsForStudents();
        String studentMajor = currentStudent.getMajor();
        int studentYear = currentStudent.getYearOfStudy();
        String uStudentYear;
        int count;

        if (studentYear > 0 && studentYear < 3){
            uStudentYear = "Basic";
            count = 0 ;
        }
        else {
            uStudentYear = "Advanced";
            count = 1;
        }

        for(Internship internship : availableInternships ){

            String jobMajor = internship.getPreferredMajor();
            boolean majorMatch = jobMajor.equalsIgnoreCase(studentMajor);
            boolean yearMatch;
            String jobLevel = internship.getLevel();

            if (count == 0){

                yearMatch = jobLevel.equalsIgnoreCase(uStudentYear);
            }
            else{
                yearMatch = true;
            }

            if (majorMatch && yearMatch){
                matches.add(internship);
            }
        }

        return matches;
    }

    public void applyForInternship(Student currentStudent){
        Scanner sc = new Scanner(System.in);
        int count = getApplicationCount(currentStudent.getUserID());

        if (count >= 3){
            System.out.println("You have already applied to " + count + " internships.");
            System.out.println("You cannot apply for any more.");
            return;
        }

        List<Internship> validInternships = getFilteredInternships(currentStudent);

        List<Internship> finalValidInternships = internshipChecker(validInternships);

        if (finalValidInternships.isEmpty()) {
            System.out.println("No eligible internships found to apply for.");
            return;
        }

        System.out.println("--- Select an Internship to Apply ---");
        int index = 1;
        for (Internship i : finalValidInternships) {
            System.out.println(index + ". " + i.getTitle() + " at " + i.getCompanyName());
            index++;
        }
        System.out.println("0. Cancel");

        // Get User Input
        System.out.print("Enter Ref #: ");
        int choice = -1;
        try {
            choice = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice == 0) return;

        if (choice > 0 && choice <= finalValidInternships.size()) {
            // -1 because List index starts at 0, but display started at 1
            Internship selectedInternship = finalValidInternships.get(choice - 1);

            boolean success = submitApplication(currentStudent, selectedInternship);

            if (success) {
                System.out.println("Application submitted successfully for " + selectedInternship.getTitle());
            } else {
                System.out.println("Application failed. Please try again.");
            }
        } else {
            System.out.println("Invalid Ref #.");
        }
    }

    public boolean submitApplication(Student student, Internship internship) {
        String appId = UUID.randomUUID().toString();
        String status = "Pending"; // Default status
        String date = LocalDate.now().toString();

        // 3. Format CSV Line: Uuid,Userid,Name,Email,Major,Year,SubmittedDate,Status
        String csvLine = String.join(",",
                escapeCSV(internship.getUUID().toString()),
                escapeCSV(student.getUserID()),
                escapeCSV(student.getName()),
                escapeCSV(student.getEmail()),
                escapeCSV(student.getMajor()),
                escapeCSV(String.valueOf(student.getYearOfStudy())),
                escapeCSV(date),
                escapeCSV(status)
        );

        // 4. Write to File
        try {
            // Create file/header if it doesn't exist
            if (!Files.exists(applicationPath)) {
                String header = "Uuid,Userid,Name,Email,Major,Year,SubmittedDate,Status";
                Files.write(applicationPath, Collections.singletonList(header), StandardOpenOption.CREATE);
            }

            // Append the new application
            Files.write(applicationPath, Collections.singletonList(csvLine),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND

            );
            return true;

        } catch (IOException e) {
            System.err.println("Error saving application: " + e.getMessage());
            return false;
        }
    }

    public List<Internship>internshipChecker(List<Internship> validInternships){
        List<Internship> internList = new ArrayList<>();

        for(Internship i : validInternships){

            try (Stream<String> lines = Files.lines(applicationPath)) {
                boolean result = lines.skip(1)
                        .map(line -> line.split(",", -1))
                        .filter(cols -> cols.length > 0)
                        .anyMatch(cols -> cols[0].trim().replace("\"", "").equals(i.getUUID().toString()));


                if (result == false){
                    internList.add(i);
                }
            } catch (IOException e) {
                System.err.println("Error reading application file: " + e.getMessage());
                return internList;
            }
        }
        return internList;
    }
}
