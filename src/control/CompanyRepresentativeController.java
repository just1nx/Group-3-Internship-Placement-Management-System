package control;

import entity.Application;
import entity.CompanyRepresentative;
import entity.Internship;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Controller for company representative operations.
 * <p>
 * Handles creating/editing/deleting internships, toggling visibility,
 * and retrieving applications/notifications for a company. Enforces business
 * rules such as maximum internships per company (5) and restricts editing/deletion
 * to pending internships only.
 * </p>
 */
public class CompanyRepresentativeController extends BaseController {
    private final Map<String, Internship> internships;
    private final Map<String, List<Application>> applications;

    // Define the path to the internship and application CSV file
    private static final Path internshipPath = Paths.get("data/sample_internship_list.csv");
    private static final Path applicationPath = Paths.get("data/sample_application_list.csv");

    // Define the maximum number of internships allowed per company
    private static final int maxInternships = 5;

    /**
     * Construct the controller and preload internships and applications from CSV.
     */
    public CompanyRepresentativeController() {
        internships = loadInternships(internshipPath);
        applications = loadApplications(applicationPath);
    }

    /**
     * Check whether the company may create more internships (enforces a maximum per company).
     * <p>
     * Counts non-rejected internships for the company and compares against the maximum limit.
     * Handles null or empty company names by returning false.
     * </p>
     *
     * @param companyName the company name to check (case-insensitive)
     * @return true if the company can create another internship
     */
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

    /**
     * Return the company's internships, optionally filtered by status/level/major.
     * <p>
     * All filters are case-insensitive and optional (null/empty lists = no filter applied).
     * Results are sorted alphabetically by title.
     * </p>
     *
     * @param companyName   company name to filter by (case-insensitive)
     * @param statusFilters status filters to apply (nullable)
     * @param levelFilters  level filters to apply (nullable)
     * @param majorFilters  preferred major filters to apply (nullable)
     * @return list of internships matching the criteria (may be empty)
     */
    public List<Internship> viewMyInternships(String companyName, List<String> statusFilters, List<String> levelFilters, List<String> majorFilters) {
        if (companyName == null || companyName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String target = companyName.trim();

        // Start with the base stream filtered by company
        Stream<Internship> stream = internships.values().stream()
                .filter(i -> i.getCompanyName() != null &&
                        i.getCompanyName().trim().equalsIgnoreCase(target));

        // Filter logic
        // Apply status filter if provided
        if (statusFilters != null && !statusFilters.isEmpty()) {
            Set<String> normalizedFilters = statusFilters.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            stream = stream.filter(i -> i.getStatus() != null &&
                    normalizedFilters.contains(i.getStatus().toLowerCase()));
        }

        // Apply level filter if provided
        if (levelFilters != null && !levelFilters.isEmpty()) {
            Set<String> normalizedFilters = levelFilters.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            stream = stream.filter(i -> i.getLevel() != null &&
                    normalizedFilters.contains(i.getLevel().toLowerCase()));
        }

        // Apply major filter if provided
        if (majorFilters != null && !majorFilters.isEmpty()) {
            Set<String> normalizedFilters = majorFilters.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            stream = stream.filter(i -> i.getPreferredMajor() != null &&
                    normalizedFilters.contains(i.getPreferredMajor().toLowerCase()));
        }

        return stream
                .sorted(Comparator.comparing(Internship::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Create a new internship and persist it (defaults to Pending / not visible).
     *
     * @param title           internship title
     * @param description     description text
     * @param level           level string (e.g., "Basic", "Advanced")
     * @param preferredMajor  preferred major
     * @param openingDate     opening date as yyyy-MM-dd
     * @param closingDate     closing date as yyyy-MM-dd
     * @param companyName     company name
     * @param representativeId representative id string (stored in "Representatives" field)
     * @param numberOfSlots   number of available slots
     * @return true when creation and CSV write succeed
     */
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
        // Generate unique ID and set default values
        UUID uuid = UUID.randomUUID();
        String status = "Pending"; // Default to pending
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

        // Create new Internship object
        Internship newInternship = new Internship(uuid, title, description, level, preferredMajor,
                opening, closing, status, companyName, representativeId, numberOfSlots, visibility);

        // Add new internship to the in-memory map
        internships.put(uuid.toString(), newInternship);

        // Rewrite the entire CSV file with the new data
        return rewriteInternshipCSV(internshipPath, internships);
    }

    /**
     * Edit an existing internship (only allowed when internship is pending).
     * <p>
     * Only updates fields where non-null/non-empty values are provided.
     * Validates numberOfSlots must be between 1-10 inclusive.
     * </p>
     *
     * @param internshipUUID     internship UUID string
     * @param newTitle           new title (null/empty to keep current)
     * @param newDescription     new description (null/empty to keep current)
     * @param newLevel           new level (null/empty to keep current)
     * @param newPreferredMajor  new preferred major (null/empty to keep current)
     * @param newOpeningDate     new opening date (yyyy-MM-dd) (null/empty to keep current)
     * @param newClosingDate     new closing date (yyyy-MM-dd) (null/empty to keep current)
     * @param newNumberOfSlots   new slot count (1-10, outside range keeps current)
     * @return true when update and persistence succeed, false if not pending or on error
     */
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

    /**
     * Delete a pending internship and persist changes.
     *
     * @param internshipUUID internship UUID string to delete
     * @return true on successful deletion and persistence
     */
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

    /**
     * Toggle visibility of an approved internship.
     * <p>
     * Only approved internships can have visibility toggled. Option 1 sets visible to true,
     * option 2 sets visible to false.
     * </p>
     *
     * @param internshipUUID internship UUID string
     * @param option         1 = visible, 2 = not visible
     * @return true on success, false if not approved or on error
     */
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

    /**
     * Retrieve internships for a company along with their applications.
     * <p>
     * Returns a map where each company internship UUID is mapped to its list of applications.
     * Internships with no applications will have an empty list.
     * </p>
     *
     * @param companyName company to retrieve apps for (case-insensitive)
     * @return map of internship UUID -> list of Application (empty map if no internships found)
     */
    public Map<String, List<Application>> getInternshipsWithApplications(String companyName) {
        // Collect all internship IDs from the IN-MEMORY MAP
        Set<String> companyInternshipIds = internships.values().stream()
                .filter(i -> i.getCompanyName() != null && i.getCompanyName().trim().equalsIgnoreCase(companyName.trim()))
                .map(i -> i.getUUID().toString())
                .collect(Collectors.toSet());

        if (companyInternshipIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Build the result map by filtering the pre-loaded applications
        Map<String, List<Application>> results = new HashMap<>();
        for (String internshipId : companyInternshipIds) {
            // Get the list of applications for this internship, or an empty list if none
            List<Application> appsForThisInternship = applications.getOrDefault(internshipId, new ArrayList<>());
            results.put(internshipId, appsForThisInternship);
        }

        return results;
    }

    /**
     * Update the status of a student's application for a given internship and persist changes.
     * <p>
     * Finds the application by matching both internship UUID and student user ID, then updates
     * the status and rewrites the application CSV file.
     * </p>
     *
     * @param internshipUUID internship UUID string
     * @param studentUserId  student user id
     * @param newStatus      new status string (e.g., "Successful", "Unsuccessful")
     * @return true when update and CSV rewrite succeed, false if application not found or on error
     */
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

    // src/control/CompanyRepresentativeController.java
    /**
     * Produce short notifications for the company representative (e.g., rejected internships).
     * Rejected internships are removed from the in-memory map and persisted.
     * <p>
     * Scans all company internships for "Rejected" status, creates notification messages,
     * and removes those internships from the system permanently.
     * </p>
     *
     * @param companyRep the CompanyRepresentative to check notifications for
     * @return list of notification messages (may be empty)
     */
    public List<String> checkNotifications(CompanyRepresentative companyRep) {
        List<String> notifications = new ArrayList<>();
        String companyName = companyRep.getCompanyName();
        if (companyName == null || companyName.trim().isEmpty()) {
            return notifications;
        }
        String target = companyName.trim();

        // Collect removals to avoid modifying the map during iteration
        List<String> toRemove = new ArrayList<>();

        for (Internship internship : internships.values()) {
            if (internship.getCompanyName() != null &&
                    internship.getCompanyName().trim().equalsIgnoreCase(target)) {
                String status = internship.getStatus();
                if (status != null && status.equalsIgnoreCase("Rejected")) {
                    String message = "Your Internship: '" + internship.getTitle() + "' has been rejected.";
                    notifications.add(message);
                    toRemove.add(internship.getUUID().toString());
                }
            }
        }

        // Apply removals after iteration
        for (String internshipId : toRemove) {
            if (!removeInternshipInternal(internshipId)) {
                System.err.println("Failed to remove internship with ID: " + internshipId);
            }
        }

        return notifications;
    }

    /**
     * Internal helper to remove an internship from the system.
     * <p>
     * Removes from in-memory map and rewrites the internship CSV file.
     * </p>
     *
     * @param internshipUUID internship UUID string to remove
     * @return true when removal and CSV rewrite succeed, false on error
     */
    private boolean removeInternshipInternal(String internshipUUID) {
        if (internshipUUID == null || internshipUUID.trim().isEmpty()) {
            System.err.println("Error: Invalid internship UUID.");
            return false;
        }

        // Remove applications from the in-memory map
        internships.remove(internshipUUID);

        // Rewrite the application CSV
        return rewriteInternshipCSV(internshipPath, internships);
    }
}
