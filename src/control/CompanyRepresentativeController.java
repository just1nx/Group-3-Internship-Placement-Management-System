package control;

import entity.Application;
import entity.Internship;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompanyRepresentativeController {
    private final Map<String, Internship> internships = new HashMap<>();
    private final Map<String, List<Application>> applicationsByInternshipId = new HashMap<>();

    // Define the path to the internship and application CSV file
    private static final Path internshipPath = Paths.get("data/sample_internship_list.csv");
    private static final Path applicationPath = Paths.get("data/sample_application_list.csv");

    // Define the maximum number of internships allowed per company
    private static final int maxInternships = 5;

    public CompanyRepresentativeController() {
        loadInternships(internshipPath);
        loadApplications(applicationPath);
    }

    private void loadInternships(Path csvPath) {
        if (!Files.exists(csvPath)) {
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

    private void loadApplications(Path csvPath) {
        if (!Files.exists(csvPath)) {
            System.err.println("Application CSV not found: " + csvPath);
            return;
        }

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

                        Application app = new Application(appUuid, status, submittedDate, userId, name, email, major, year);

                        // Add it to the map, grouped by its Internship ID
                        applicationsByInternshipId.putIfAbsent(internshipId, new ArrayList<>());
                        applicationsByInternshipId.get(internshipId).add(app);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read applications CSV: " + e.getMessage());
        }
    }

    private boolean rewriteInternshipCSV() {
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
            Files.write(internshipPath, lines, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to rewrite internship CSV: " + e.getMessage());
            return false;
        }
    }

    private boolean rewriteApplicationCSV() {
        List<String> lines = new ArrayList<>();
        // Add header
        lines.add("UUID,UserId,Name,Email,Major,Year,SubmittedDate,Status");

        // Add data lines from in-memory map
        for (Map.Entry<String, List<Application>> entry : applicationsByInternshipId.entrySet()) {
            for (Application app : entry.getValue()) {
                lines.add(String.join(",",
                        escapeCSV(app.getUUID().toString()), // 0: UUID (which is the InternshipUUID)
                        escapeCSV(app.getUserId()),          // 1: UserId
                        escapeCSV(app.getName()),            // 2: Name
                        escapeCSV(app.getEmail()),           // 3: Email
                        escapeCSV(app.getMajor()),           // 4: Major
                        escapeCSV(String.valueOf(app.getYear())), // 5: Year
                        escapeCSV(app.getSubmittedDate()),     // 6: SubmittedDate
                        escapeCSV(app.getStatus())             // 7: Status
                ));
            }
        }

        // Write to file, overwriting existing content
        try {
            Files.write(applicationPath, lines, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to rewrite application CSV: " + e.getMessage());
            return false;
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

    public boolean canCreateMoreInternships(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            System.err.println("Error: Company name cannot be null or empty.");
            return false;
        }

        long count = internships.values().stream()
                .filter(i -> i.getCompanyName() != null &&
                        i.getCompanyName().trim().equalsIgnoreCase(companyName.trim()))
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
        return rewriteInternshipCSV();
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
        return rewriteInternshipCSV();
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
        return rewriteInternshipCSV();
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
        return rewriteInternshipCSV();
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
            List<Application> appsForThisInternship = applicationsByInternshipId.getOrDefault(internshipId, new ArrayList<>());
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
        List<Application> appList = applicationsByInternshipId.get(internshipUUID);
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
            return rewriteApplicationCSV();
        } else {
            System.err.println("Error: Matching application not found for student " + studentUserId);
            return false;
        }
    }
}
