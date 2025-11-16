package control;

import entity.Application;
import entity.Internship;
import entity.Student;
import entity.Withdrawal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

public class StudentController extends BaseController {
    private final Map<String, Internship> internships;
    private final Map<String, List<Application>> applications;
    private final Map<String, List<Withdrawal>> withdrawals;

    // Define the path to the application and internship CSV file
    private static final Path applicationPath = Paths.get("data/sample_application_list.csv");
    private static final Path internshipPath = Paths.get("data/sample_internship_list.csv");
    private static final Path withdrawalPath = Paths.get("data/sample_withdrawal_list.csv");

    private static final int maxApplication = 3;

    public StudentController(){
        internships = loadInternships(internshipPath);
        applications = loadApplications(applicationPath);
        withdrawals = loadWithdrawals(withdrawalPath);
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

    public List<String> checkNotifications(Student student) {
        List<String> notifications = new ArrayList<>();
        String studentID = student.getUserID();

        List<String[]> applicationsToRemove = new ArrayList<>();
        List<String[]> withdrawalsToRemove = new ArrayList<>();

        // Helper to split lines correctly (handles quoted CSV fields)
        java.util.function.Function<String, String[]> splitLine = line ->
                line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        // Set to hold Internship IDs for which a withdrawal request exists,
        // so we don't process the basic application status first.
        Set<String> withdrawalRequestIds = new HashSet<>();

        // --- A. PRE-STEP: Populate Withdrawal Request IDs for the student ---
        try (Stream<String> lines = Files.lines(withdrawalPath)) {
            lines.skip(1)
                    .map(splitLine)
                    .filter(cols -> cols.length == 8)
                    .filter(cols -> unquote(cols[1]).equals(studentID))
                    .forEach(cols -> withdrawalRequestIds.add(unquote(cols[0])));
        } catch (IOException e) {
            System.err.println("Error reading withdrawal file to check existing requests: " + e.getMessage());
        }

        // --- 1. Check withdrawal request updates (Approved/Rejected/Denied) ---
        try (Stream<String> lines = Files.lines(withdrawalPath)) {
            lines.skip(1)
                    .map(splitLine)
                    .filter(cols -> cols.length == 8)
                    .filter(cols -> unquote(cols[1]).equals(studentID))
                    .forEach(cols -> {
                        String status = unquote(cols[7]); // Status is at index 7
                        String internshipId = unquote(cols[0]);
                        Internship internship = internships.get(internshipId);
                        String internshipTitle = internship.getTitle();

                        if (status.equalsIgnoreCase("Approved")) {
                            notifications.add("Your withdrawal request for Internship: " + internshipTitle + " has been approved.");
                            // Approved withdrawal means the original application must be removed
                            applicationsToRemove.add(new String[]{internshipId, studentID});
                            withdrawalsToRemove.add(new String[]{internshipId, studentID});
                        } else if (status.equalsIgnoreCase("Rejected")) { // Using "Rejected" from your provided code, assuming it means "Denied"
                            notifications.add("Your withdrawal request for Internship: " + internshipTitle + " has been rejected. Your original application status is restored.");
                            // Denied withdrawal means the withdrawal request is removed
                            withdrawalsToRemove.add(new String[]{internshipId, studentID});
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error reading withdrawal request file for notifications: " + e.getMessage());
        }

        // --- 2. Check application status updates (Approved/Rejected) ---
        // Only process applications that DONT have a pending/resolved withdrawal request (already handled in step 1)
        try (Stream<String> lines = Files.lines(applicationPath)) {
            lines.skip(1)
                    .map(splitLine)
                    .filter(cols -> cols.length == 8)
                    .filter(cols -> unquote(cols[1]).equals(studentID))
                    .forEach(cols -> {
                        String status = unquote(cols[7]);
                        String internshipId = unquote(cols[0]);

                        Internship internship = internships.get(internshipId);

                        // Skip this application if a withdrawal request was found for it (handled in step 1)
                        if (withdrawalRequestIds.contains(internshipId)) {
                            return;
                        }

                        String internshipTitle = internship.getTitle();

                        if (status.equalsIgnoreCase("Approved")) {
                            notifications.add("Your application for Internship: " + internshipTitle + " has been approved.");
                        } else if (status.equalsIgnoreCase("Rejected")) {
                            notifications.add("Your application for Internship: " + internshipTitle + " has been rejected.");
                            applicationsToRemove.add(new String[]{internshipId, studentID});
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error reading application file for notifications: " + e.getMessage());
        }

        // Remove Applications
        for (String[] app : applicationsToRemove) {
            removeApplication(app[0], app[1]);
        }

        // Remove Withdrawal Requests
        for (String[] withdrawal : withdrawalsToRemove) {
            removeWithdrawal(withdrawal[0], withdrawal[1]);
        }

        return notifications;
    }

    void removeApplication(String internshipId, String studentId) {
        try {
            List<String> lines = Files.readAllLines(applicationPath);
            List<String> updatedLines = new ArrayList<>();
            updatedLines.add(lines.get(0)); // Keep header

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] cols = line.split(",", -1);
                if (cols.length > 1) {
                    String fileInternshipId = cols[0].trim().replace("\"", "");
                    String fileStudentId = cols[1].trim().replace("\"", "");
                    if (!(fileInternshipId.equals(internshipId) && fileStudentId.equals(studentId))) {
                        updatedLines.add(line);
                    }
                }
            }

            Files.write(applicationPath, updatedLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error removing application: " + e.getMessage());
        }
    }

    void removeWithdrawal(String internshipId, String studentId) {
        try {
            List<String> lines = Files.readAllLines(withdrawalPath);
            List<String> updatedLines = new ArrayList<>();
            updatedLines.add(lines.get(0)); // Keep header

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] cols = line.split(",", -1);
                if (cols.length > 1) {
                    String fileInternshipId = cols[0].trim().replace("\"", "");
                    String fileStudentId = cols[1].trim().replace("\"", "");
                    if (!(fileInternshipId.equals(internshipId) && fileStudentId.equals(studentId))) {
                        updatedLines.add(line);
                    }
                }
            }

            Files.write(withdrawalPath, updatedLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error removing withdrawal request: " + e.getMessage());
        }
    }
}
