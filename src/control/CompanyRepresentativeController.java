package control;

import entity.Application;
import entity.Internship;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class CompanyRepresentativeController extends BaseController {
    private final Map<String, Internship> internships;
    private final Map<String, List<Application>> applications;

    // Define the path to the internship and application CSV file
    private static final Path internshipPath = Paths.get("data/sample_internship_list.csv");
    private static final Path applicationPath = Paths.get("data/sample_application_list.csv");

    // Define the maximum number of internships allowed per company
    private static final int maxInternships = 5;

    public CompanyRepresentativeController() {
        internships = loadInternships(internshipPath);
        applications = loadApplications(applicationPath);
    }

    public boolean canCreateMoreInternships(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            System.err.println("Error: Company name cannot be null or empty.");
            return false;
        }

        long count = internships.values().stream()
                .filter(i -> i.getCompanyName() != null &&
                        i.getCompanyName().trim().equalsIgnoreCase(companyName.trim()))
                .filter(i -> i.getStatus() != null &&
                        !i.getStatus().equalsIgnoreCase("Rejected"))
                .count();

        // Return true if the current count is less than the maximum allowed.
        return count < maxInternships;
    }

    public List<Internship> viewMyInternships(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String target = companyName.trim();
        return internships.values().stream()
                .filter(i -> i.getCompanyName() != null &&
                        i.getCompanyName().trim().equalsIgnoreCase(target))
                .collect(Collectors.toList());
    }

    public boolean createInternship(
            String title,
            String description,
            String level,
            String preferredMajor,
            String openingDate,
            String closingDate,
            String companyName,
            String representativeId, // This is the 'Representatives' field
            int numberOfSlots
    ) {
        // 1. Generate unique ID and set default values
        UUID uuid = UUID.randomUUID();
        String status = "Pending"; // As per PDF, must be approved by staff
        boolean visibility = false;  // Default to not visible

        LocalDate opening;
        LocalDate closing;
        try {
            opening = LocalDate.parse(openingDate);
            closing = LocalDate.parse(closingDate);
        } catch (Exception e) {
            System.err.println("Invalid date format provided: " + e.getMessage());
            return false;
        }

        // 2. Create new Internship object
        Internship newInternship = new Internship(uuid, title, description, level, preferredMajor,
                opening, closing, status, companyName, representativeId, numberOfSlots, visibility);

        // 3. Add new internship to the in-memory map
        internships.put(uuid.toString(), newInternship);

        // 4. Rewrite the entire CSV file with the new data
        return rewriteInternshipCSV(internshipPath, internships);
    }

    public boolean editInternship(
            String internshipUUID,
            String newTitle,
            String newDescription,
            String newLevel,
            String newPreferredMajor,
            String newOpeningDate,
            String newClosingDate,
            int newNumberOfSlots
    ) {
        Internship internship = internships.get(internshipUUID);

        // Deny edit unless internship is still pending
        String status = internship.getStatus();
        if (status != null && !status.toLowerCase().contains("pending")) {
            System.err.println("Cannot edit internship unless it is pending.");
            return false;
        }

        // Update the object in the map (it's the same object reference)
        try {
            if (newTitle != null && !newTitle.isEmpty()) internship.setTitle(newTitle);
            if (newDescription != null && !newDescription.isEmpty()) internship.setDescription(newDescription);
            if (newLevel != null && !newLevel.isEmpty()) internship.setLevel(newLevel);
            if (newPreferredMajor != null && !newPreferredMajor.isEmpty()) internship.setPreferredMajor(newPreferredMajor);
            if (newOpeningDate != null && !newOpeningDate.isEmpty()) internship.setOpeningDate(LocalDate.parse(newOpeningDate));
            if (newClosingDate != null && !newClosingDate.isEmpty()) internship.setClosingDate(LocalDate.parse(newClosingDate));
            if (newNumberOfSlots >= 1 && newNumberOfSlots <= 10) internship.setNumberOfSlots(newNumberOfSlots);
        } catch (Exception e) {
            System.err.println("Failed to parse new data (e.g., date): " + e.getMessage());
            return false;
        }

        // Rewrite the entire CSV
        return rewriteInternshipCSV(internshipPath, internships);
    }

    public boolean deleteInternship(String internshipUUID) {
        Internship internship = internships.get(internshipUUID);

        // Deny deletion unless internship is still pending
        String status = internship.getStatus();
        if (status != null && !status.toLowerCase().contains("pending")) {
            System.err.println("Cannot delete internship unless it is pending.");
            return false;
        }

        // Remove from the in-memory map
        internships.remove(internship.getUUID().toString());

        // Rewrite the CSV
        return rewriteInternshipCSV(internshipPath, internships);
    }

    public boolean toggleInternshipVisibility(String internshipUUID, int option) {
        Internship internship = internships.get(internshipUUID);

        // Only allow toggling if the internship is approved
        String status = internship.getStatus();
        if (status == null || !status.toLowerCase().contains("approved")) {
            System.err.println("Cannot change visibility: Internship must be approved before toggling visibility.\n");
            return false;
        }

        boolean newVisibility = (option == 1); // 1 for visible, 2 for not visible

        // Update the object in the map
        internship.setVisibility(newVisibility);

        // Rewrite the CSV
        return rewriteInternshipCSV(internshipPath, internships);
    }

    public Map<String, List<Application>> getInternshipsWithApplications(String companyName) {
        // 1. Collect all internship IDs from the IN-MEMORY MAP
        Set<String> companyInternshipIds = internships.values().stream()
                .filter(i -> i.getCompanyName() != null && i.getCompanyName().trim().equalsIgnoreCase(companyName.trim()))
                .map(i -> i.getUUID().toString())
                .collect(Collectors.toSet());

        if (companyInternshipIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 2. Build the result map by filtering the pre-loaded applications
        Map<String, List<Application>> results = new HashMap<>();
        for (String internshipId : companyInternshipIds) {
            // Get the list of applications for this internship, or an empty list if none
            List<Application> appsForThisInternship = applications.getOrDefault(internshipId, new ArrayList<>());
            results.put(internshipId, appsForThisInternship);
        }

        return results;
    }

    public boolean updateApplicationStatus(String internshipUUID, String studentUserId, String newStatus) {
        if (internshipUUID == null || studentUserId == null || newStatus == null) {
            System.err.println("Error: Invalid parameters.");
            return false;
        }

        // 1. Find the application list for this internship
        List<Application> appList = applications.get(internshipUUID);
        if (appList == null) {
            System.err.println("Error: No applications found for internship " + internshipUUID);
            return false; // No applications for this internship
        }

        boolean updated = false;
        // 2. Find the specific application by student ID and update its status
        for (Application app : appList) {
            if (app.getUserId().equals(studentUserId)) {
                app.setStatus(newStatus);
                updated = true;
                break;
            }
        }

        // 3. If an update was made, rewrite the entire application CSV
        if (updated) {
            return rewriteApplicationCSV(applicationPath, applications);
        } else {
            System.err.println("Error: Matching application not found for student " + studentUserId);
            return false;
        }
    }
}
