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
        if (csvPath == null || !Files.exists(csvPath)) {
            System.err.println("Internship CSV not found: " + csvPath);
            return;
        }

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1)
                    .map(line -> line.split(",", -1))
                    .filter(cols -> cols.length > 0)
                    .forEach(cols -> {
                        String id = cols.length > 0 ? cols[0].trim() : "";
                        if (id.isEmpty()) return;

                        String title = cols.length > 1 ? cols[1].trim() : "";
                        String description = cols.length > 2 ? cols[2].trim() : "";
                        String level = cols.length > 3 ? cols[3].trim() : "";
                        //required to trim the extra quotation mark from preferredMajor
                        String preferredMajor = cols.length > 4 ? cols[4].trim().replace("\"", "") : "";
                        //
                        LocalDate openingDate = LocalDate.now();
                        if (cols.length > 5) {
                            try {
                                String dateString = cols[5].trim();
                                openingDate = LocalDate.parse(dateString);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        LocalDate closingDate = LocalDate.now();
                        if (cols.length > 6) {
                            try {
                                String dateString = cols[6].trim();
                                closingDate = LocalDate.parse(dateString);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        String status = cols.length > 7 ? cols[7].trim() : "";
                        String companyName = cols.length > 8 ? cols[8].trim() : "";
                        String representatives = cols.length > 9 ? cols[9].trim() : "";
                        String numberOfSlots = cols.length > 10 ? cols[10].trim() : "";

                        Boolean visibility = false;
                        if (cols.length > 11) {
                            try {
                                visibility = Boolean.parseBoolean(cols[11].trim());
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        Internship internship = new Internship(UUID.fromString(id), title, description, level, preferredMajor, openingDate, closingDate, status, companyName, representatives, numberOfSlots, visibility);
                        internships.put(id, internship);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read internship CSV: " + e.getMessage());
        }
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

        // 2. Sanitization Helper
        java.util.function.Function<String, String> esc = s -> {
            if (s == null) s = "";
            String out = s.replace("\"", "\"\"");
            if (out.contains(",") || out.contains("\"") || out.contains("\n") || out.contains("\r")) {
                out = "\"" + out + "\"";
            }
            return out;
        };

        // 3. Format CSV Line: Uuid,Userid,Name,Email,Major,Year,SubmittedDate,Status
        String csvLine = String.join(",",
                esc.apply(internship.getUUID().toString()),
                esc.apply(student.getUserID()),
                esc.apply(student.getName()),
                esc.apply(student.getEmail()),
                esc.apply(student.getMajor()),
                esc.apply(String.valueOf(student.getYearOfStudy())),
                esc.apply(date),
                esc.apply(status)
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
