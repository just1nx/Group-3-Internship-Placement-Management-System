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
import java.util.stream.Collectors;
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

        // Lists to store IDs of items to remove *after* iteration is complete
        // This avoids a ConcurrentModificationException
        List<String[]> applicationsToRemove = new ArrayList<>();
        List<String[]> withdrawalsToRemove = new ArrayList<>();

        // --- A. PRE-STEP: Populate Withdrawal Request IDs for the student ---
        // Get all internship IDs for which this student has a withdrawal request
        Set<String> withdrawalRequestIds = withdrawals.values().stream()
                .flatMap(List::stream) // Flatten Stream<List<Withdrawal>> to Stream<Withdrawal>
                .filter(w -> w.getUserId().equals(studentID))
                .map(w -> w.getUUID().toString()) // Get the internship ID (UUID)
                .collect(Collectors.toSet());

        // --- 1. Check withdrawal request updates (Approved/Rejected) ---
        // Iterate over all withdrawals from the in-memory map
        withdrawals.values().stream()
                .flatMap(List::stream)
                .filter(w -> w.getUserId().equals(studentID))
                .forEach(withdrawal -> {
                    String status = withdrawal.getStatus();
                    String internshipId = withdrawal.getUUID().toString();
                    Internship internship = internships.get(internshipId);
                    String internshipTitle = (internship != null) ? internship.getTitle() : "[Unknown Internship]";

                    if (status.equalsIgnoreCase("Approved")) {
                        notifications.add("Your withdrawal request for Internship: " + internshipTitle + " has been approved.");
                        // Mark both the withdrawal and the original application for removal
                        applicationsToRemove.add(new String[]{internshipId, studentID});
                        withdrawalsToRemove.add(new String[]{internshipId, studentID});
                    } else if (status.equalsIgnoreCase("Rejected")) {
                        notifications.add("Your withdrawal request for Internship: " + internshipTitle + " has been rejected. Your original application status is restored.");
                        // Mark only the withdrawal request for removal
                        withdrawalsToRemove.add(new String[]{internshipId, studentID});
                    }
                });

        // --- 2. Check application status updates (Approved/Rejected) ---
        // Iterate over all applications from the in-memory map
        applications.values().stream()
                .flatMap(List::stream)
                .filter(app -> app.getUserId().equals(studentID))
                .forEach(application -> {
                    String status = application.getStatus();
                    String internshipId = application.getUUID().toString();

                    // Skip this application if a withdrawal request was found for it (handled in step 1)
                    if (withdrawalRequestIds.contains(internshipId)) {
                        return;
                    }

                    Internship internship = internships.get(internshipId);
                    String internshipTitle = (internship != null) ? internship.getTitle() : "[Unknown Internship]";

                    if (status.equalsIgnoreCase("Approved")) {
                        notifications.add("Your application for Internship: " + internshipTitle + " has been approved.");
                    } else if (status.equalsIgnoreCase("Rejected")) {
                        notifications.add("Your application for Internship: " + internshipTitle + " has been rejected.");
                        // Mark the rejected application for removal
                        applicationsToRemove.add(new String[]{internshipId, studentID});
                    }
                });

        // --- 3. Perform all removals AFTER iterations are complete ---

        // Use a Set to avoid removing from the same list multiple times if (e.g.) two
        // rejected apps for the same internship were found (which shouldn't happen, but is safe)
        Set<String> modifiedAppInternshipIds = new HashSet<>();
        Set<String> modifiedWithdrawalInternshipIds = new HashSet<>();

        for (String[] app : applicationsToRemove) {
            if (removeApplicationInternal(app[0], app[1])) {
                modifiedAppInternshipIds.add(app[0]);
            }
        }

        for (String[] withdrawal : withdrawalsToRemove) {
            if (removeWithdrawalInternal(withdrawal[0], withdrawal[1])) {
                modifiedWithdrawalInternshipIds.add(withdrawal[0]);
            }
        }

        // --- 4. Rewrite CSV files only if changes were made ---

        // Check if any lists that were modified still exist (they might be empty now)
        // and rewrite the whole file.
        if (!modifiedAppInternshipIds.isEmpty()) {
            rewriteApplicationCSV(applicationPath, applications);
        }

        if (!modifiedWithdrawalInternshipIds.isEmpty()) {
            rewriteWithdrawalCSV(withdrawalPath, withdrawals);
        }

        return notifications;
    }

    private boolean removeApplicationInternal(String internshipId, String studentId) {
        List<Application> appList = applications.get(internshipId);
        if (appList != null) {
            // Use removeIf to find and remove the matching application
            return appList.removeIf(app -> app.getUserId().equals(studentId));
        }
        return false;
    }

    private boolean removeWithdrawalInternal(String internshipId, String studentId) {
        List<Withdrawal> withdrawalList = withdrawals.get(internshipId);
        if (withdrawalList != null) {
            // Use removeIf to find and remove the matching withdrawal request
            return withdrawalList.removeIf(w -> w.getUserId().equals(studentId));
        }
        return false;
    }
}
