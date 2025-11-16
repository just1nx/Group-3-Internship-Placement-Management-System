package control;

import entity.Application;
import entity.Internship;
import entity.Student;
import entity.Withdrawal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<Internship> getAvailableInternships(Student student) {
        String studentMajor = student.getMajor();
        int studentYear = student.getYearOfStudy();
        LocalDate today = LocalDate.now(); // Get the current date once

        return internships.values().stream()
                .filter(Internship::isVisible) // Must be visible
                .filter(i -> "Approved".equalsIgnoreCase(i.getStatus())) // Must be approved by staff

                .filter(i -> !i.getClosingDate().isBefore(today)) // Must not be past closing date
                .filter(i -> !hasAlreadyApplied(student, i)) // Student must not have applied

                .filter(i -> i.getPreferredMajor() != null &&
                        i.getPreferredMajor().equalsIgnoreCase(studentMajor)) // Major must match
                .filter(i -> {
                    // Filter by level based on student year
                    String level = i.getLevel();
                    if (studentYear <= 2) {
                        // Year 1 & 2 can only apply for Basic
                        return "Basic".equalsIgnoreCase(level);
                    } else {
                        // Year 3+ can apply for any level
                        return true;
                    }
                })
                .sorted(Comparator.comparing(Internship::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public boolean hasAlreadyApplied(Student student, Internship internship) {
        List<Application> appList = applications.get(internship.getUUID().toString());
        if (appList == null) {
            return false;
        }
        return appList.stream().anyMatch(app -> app.getUserId().equals(student.getUserID()));
    }

    public boolean canApply(Student student) {
        // Get all applications for the student
        List<Application> allMyApps = applications.values().stream()
                .flatMap(List::stream)
                .filter(app -> app.getUserId().equals(student.getUserID()))
                .collect(Collectors.toList());

        // Check if they have already accepted an offer
        boolean hasAccepted = allMyApps.stream()
                .anyMatch(app -> "Accepted".equalsIgnoreCase(app.getStatus()));

        if (hasAccepted) {
            return false; // Cannot apply if one is accepted
        }

        // Check if they are at the 3-application limit (only count active ones)
        long activeAppCount = allMyApps.stream()
                .filter(app -> "Pending".equalsIgnoreCase(app.getStatus()) ||
                        "Successful".equalsIgnoreCase(app.getStatus()))
                .count();

        return activeAppCount < maxApplication;
    }

    public boolean applyForInternship(Student student, Internship internship) {
        // Create new Application object
        String status = "Pending"; // Default status
        String date = LocalDate.now().toString();
        Application application = new Application(
                internship.getUUID(),
                status,
                date,
                student.getUserID(),
                student.getName(),
                student.getEmail(),
                student.getMajor(),
                student.getYearOfStudy()
        );

        // Add to the in-memory map
        String internshipId = internship.getUUID().toString();
        applications.putIfAbsent(internshipId, new ArrayList<>());
        applications.get(internshipId).add(application);

        // Save changes to CSV
        return rewriteApplicationCSV(applicationPath, applications);
    }

    public Map<Application, Internship> getMyApplications(Student student) {
        Map<Application, Internship> myApps = new HashMap<>();
        String studentId = student.getUserID();

        // Flatten the map of lists into a single stream of all applications
        applications.values().stream()
                .flatMap(List::stream) // Stream<Application>
                .filter(app -> studentId.equals(app.getUserId()))
                .forEach(app -> {
                    // For each of an student's applications, find the matching internship
                    Internship internship = internships.get(app.getUUID().toString());
                    if (internship != null) {
                        myApps.put(app, internship);
                    }
                });
        return myApps;
    }

    public boolean acceptOffer(Student student, Application appToAccept) {
        // Check if student has already accepted another offer
        boolean alreadyAccepted = getMyApplications(student).keySet().stream()
                .anyMatch(app -> "Accepted".equalsIgnoreCase(app.getStatus()));

        if (alreadyAccepted) {
            return false; // Cannot accept more than one
        }

        // Set the chosen application to "Accepted"
        appToAccept.setStatus("Accepted");

        // Remove all other "Pending" or "Successful" applications
        // Iterate over each list in the map's values
        for (List<Application> appList : applications.values()) {
            // Use removeIf to safely find and remove matching applications
            appList.removeIf(app ->
                    app.getUserId().equals(student.getUserID()) && // Belongs to this student
                            !app.getUUID().equals(appToAccept.getUUID()) && // NOT the one they accepted
                            ("Pending".equalsIgnoreCase(app.getStatus()) || "Successful".equalsIgnoreCase(app.getStatus()))
            );
        }

        // Remove all pending withdrawal requests for this student
        boolean withdrawalsChanged = false;
        for (List<Withdrawal> wList : withdrawals.values()) {
            // Remove any "Pending" withdrawal request submitted by this student
            if (wList.removeIf(w -> w.getUserId().equals(student.getUserID()) && "Pending".equalsIgnoreCase(w.getStatus()))) {
                withdrawalsChanged = true;
            }
        }

        // Update the Internship's slots and status
        String acceptedInternshipId = appToAccept.getUUID().toString();
        Internship acceptedInternship = internships.get(acceptedInternshipId);
        boolean internshipChanged = false;

        if (acceptedInternship != null) {
            int currentSlots = acceptedInternship.getNumberOfSlots();
            if (currentSlots > 0) {
                acceptedInternship.setNumberOfSlots(currentSlots - 1);

                // If slots are now 0, set status to "Filled"
                if (acceptedInternship.getNumberOfSlots() == 0) {
                    acceptedInternship.setStatus("Filled");
                }
                internshipChanged = true;
            } else {
                // This case (accepting an offer for an internship with 0 slots)
                // shouldn't happen if logic is correct, but it's good to know.
                System.err.println("Warning: Student accepted an offer for internship " +
                        acceptedInternshipId + " which already had 0 slots.");
                acceptedInternship.setStatus("Filled"); // Ensure it's filled
                internshipChanged = true;
            }
        } else {
            System.err.println("CRITICAL ERROR: Could not find internship " +
                    acceptedInternshipId + " to update slots.");
        }

        // Save all changes to all relevant CSV files
        boolean appSave = rewriteApplicationCSV(applicationPath, applications);
        boolean wthSave = true;
        boolean intSave = true;

        if (withdrawalsChanged) {
            wthSave = rewriteWithdrawalCSV(withdrawalPath, withdrawals);
        }

        if (internshipChanged) {
            intSave = rewriteInternshipCSV(internshipPath, internships);
        }

        return appSave && wthSave && intSave;
    }

    public boolean requestWithdrawal(Application appToWithdraw) {
        // Create a new Withdrawal object
        Withdrawal withdrawal = new Withdrawal(
                appToWithdraw.getUUID(),
                "Pending", // Status is pending staff approval
                LocalDate.now().toString(),
                appToWithdraw.getUserId(),
                appToWithdraw.getName(),
                appToWithdraw.getEmail(),
                appToWithdraw.getMajor(),
                appToWithdraw.getYear()
        );

        // Add it to the withdrawals map
        String internshipId = appToWithdraw.getUUID().toString();
        withdrawals.putIfAbsent(internshipId, new ArrayList<>());
        withdrawals.get(internshipId).add(withdrawal);

        // Save the withdrawals file
        return rewriteWithdrawalCSV(withdrawalPath, withdrawals);
    }

    public Set<String> getPendingWithdrawalRequests(Student student) {
        return withdrawals.values().stream()
                .flatMap(List::stream)
                .filter(w -> w.getUserId().equals(student.getUserID()))
                .filter(w -> "Pending".equalsIgnoreCase(w.getStatus()))
                .map(w -> w.getUUID().toString()) // Get the Internship UUID
                .collect(Collectors.toSet());
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
                        notifications.add("Your withdrawal request for Internship: '" + internshipTitle + "' has been approved.");
                        // Mark both the withdrawal and the original application for removal
                        applicationsToRemove.add(new String[]{internshipId, studentID});
                        withdrawalsToRemove.add(new String[]{internshipId, studentID});
                    } else if (status.equalsIgnoreCase("Rejected")) {
                        notifications.add("Your withdrawal request for Internship: '" + internshipTitle + "' has been rejected. Your original application status is restored.");
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

                    if (status.equalsIgnoreCase("Successful")) {
                        notifications.add("Your application for Internship: " + internshipTitle + " has been approved.");
                    } else if (status.equalsIgnoreCase("Unsuccessful")) {
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
