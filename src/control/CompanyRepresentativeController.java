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
    // Define the path to the internship CSV file
    private static final Path internshipPath = Paths.get("data/sample_internship_list.csv");
    // Define the maximum number of internships allowed per company
    private static final int maxInternships = 5;

    public CompanyRepresentativeController() {
        loadInternships(internshipPath);
    }

    private void loadInternships(Path csvPath) {
        if (csvPath == null || !Files.exists(csvPath)) {
            System.err.println("Internship CSV not found: " + csvPath);
            return;
        }

        Map<String, Internship> temp = new HashMap<>();

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1)
                    .map(line -> line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1)) // handle quoted commas
                    .filter(cols -> cols.length > 0)
                    .forEach(cols -> {
                        String id = cols.length > 0 ? cols[0].trim() : "";
                        if (id.isEmpty()) return;

                        String title = cols.length > 1 ? cols[1].trim() : "";
                        String description = cols.length > 2 ? cols[2].trim() : "";
                        String level = cols.length > 3 ? cols[3].trim() : "";
                        String preferredMajor = cols.length > 4 ? cols[4].trim() : "";

                        LocalDate openingDate = LocalDate.now();
                        if (cols.length > 5) {
                            try { openingDate = LocalDate.parse(cols[5].trim()); } catch (Exception ignored) {}
                        }
                        LocalDate closingDate = LocalDate.now();
                        if (cols.length > 6) {
                            try { closingDate = LocalDate.parse(cols[6].trim()); } catch (Exception ignored) {}
                        }

                        String status = cols.length > 7 ? cols[7].trim() : "";
                        String companyName = cols.length > 8 ? cols[8].trim() : "";
                        String representatives = cols.length > 9 ? cols[9].trim() : "";
                        String numberOfSlots = cols.length > 10 ? cols[10].trim() : "";

                        Boolean visibility = false;
                        if (cols.length > 11) {
                            try { visibility = Boolean.parseBoolean(cols[11].trim()); } catch (Exception ignored) {}
                        }

                        try {
                            Internship internship = new Internship(UUID.fromString(id), title, description, level,
                                    preferredMajor, openingDate, closingDate, status, companyName,
                                    representatives, numberOfSlots, visibility);
                            temp.put(id, internship);
                        } catch (IllegalArgumentException ignored) {
                            // skip bad UUID rows
                        }
                    });
        } catch (IOException e) {
            System.err.println("Failed to read internship CSV: " + e.getMessage());
            return; // keep existing in-memory data if read failed
        }

        // Swap in the freshly read data (removes rows deleted from CSV)
        internships.clear();
        internships.putAll(temp);
    }

    public boolean canCreateMoreInternships(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            System.err.println("Error: Company name cannot be null or empty.");
            return false;
        }

        // We must read the file live to get the most up-to-date count.
        if (!Files.exists(internshipPath)) {
            // No file means 0 internships, so they can create.
            // We should ensure the file exists or is created, but for this check, it's fine.
            return true;
        }

        long count = 0;
        try (Stream<String> lines = Files.lines(internshipPath)) {
            // Read the file, skip the header, and count matches
            count = lines.skip(1) // Skip header row
                    .map(line -> line.split(",", -1)) // Split by comma
                    // Uuid,Title,Description,Level,PreferredMajor,OpeningDate,ClosingDate,Status,CompanyName,...
                    .filter(cols -> cols.length > 8) // Ensure the CompanyName column (index 8) exists
                    .filter(cols -> cols[8].trim().equalsIgnoreCase(companyName.trim())) // Check if company names match
                    .count(); // Count the number of matching lines
        } catch (IOException e) {
            System.err.println("Failed to read internship CSV: " + e.getMessage());
            // In case of an error, it's safer to block creation.
            return false;
        }

        // Return true if the current count is less than the maximum allowed.
        return count < maxInternships;
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
            String numberOfSlots
    ) {
        // 1. Generate unique ID and set default values
        UUID uuid = UUID.randomUUID();
        String status = "Pending"; // As per PDF, must be approved by staff
        String visibility = "false";  // Default to not visible, but staff approval is the real gate

        java.util.function.Function<String, String> esc = s -> {
            if (s == null) s = "";
            String out = s.replace("\"", "\"\"");
            if (out.contains(",") || out.contains("\"") || out.contains("\n") || out.contains("\r")) {
                out = "\"" + out + "\"";
            }
            return out;
        };

        // 2. Format the data as a CSV line
        // Columns: Uuid,Title,Description,Level,PreferredMajor,OpeningDate,ClosingDate,Status,CompanyName,Representatives,NumberOfSlots,Visibility
        String csvLine = String.join(",",
                esc.apply(uuid.toString()),
                esc.apply(title),
                esc.apply(description),
                esc.apply(level),
                esc.apply(preferredMajor),
                esc.apply(openingDate),
                esc.apply(closingDate),
                esc.apply(status),
                esc.apply(companyName),
                esc.apply(representativeId),
                esc.apply(numberOfSlots),
                esc.apply(visibility)
        );

        // 3. Append the new line to the CSV file
        try {
            // Check if file exists, if not, create it and add header
            if (!Files.exists(internshipPath)) {
                String header = "UUID,Title,Description,Level,PreferredMajor,OpeningDate,ClosingDate,Status,CompanyName,Representatives,NumberOfSlots,Visibility";
                Files.write(internshipPath, Collections.singletonList(header), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }

            // Append the new internship line
            Files.write(internshipPath, Collections.singletonList(csvLine),
                    StandardOpenOption.CREATE,    // Create the file if it doesn't exist
                    StandardOpenOption.APPEND     // Append to the end of the file
            );
            return true; // Success
        } catch (IOException e) {
            System.err.println("Failed to write internship to CSV: " + e.getMessage());
            return false; // Failure
        }
    }

    public boolean editInternship(
            String companyName,
            int number,
            String newTitle,
            String newDescription,
            String newLevel,
            String newPreferredMajor,
            String newOpeningDate,
            String newClosingDate,
            String newNumberOfSlots
    ) {
        List<Internship> internshipList = viewMyInternships(companyName);
        if (internshipList.isEmpty()) {
            System.err.println("You have no internships listed.");
            return false;
        }
        if (number < 1 || number > internshipList.size()) {
            System.err.println("Error: Invalid internship number.");
            return false;
        }

        Internship internship = internshipList.get(number - 1);

        // Deny edits if approved
        String status = internship.getStatus();
        if (status != null && status.toLowerCase().contains("approved")) {
            System.err.println("Cannot edit internship: it has already been approved.");
            return false;
        }

        Path csv = internshipPath;
        String splitRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        if (!Files.exists(csv)) {
            System.err.println("Internship CSV not found: " + csv);
            return false;
        }

        // Local esc function to preserve CSV quoting rules
        java.util.function.Function<String, String> esc = s -> {
            if (s == null) s = "";
            String out = s.replace("\"", "\"\"");
            if (out.contains(",") || out.contains("\"") || out.contains("\n") || out.contains("\r")) {
                out = "\"" + out + "\"";
            }
            return out;
        };

        try {
            List<String> lines = Files.readAllLines(csv);
            if (lines.isEmpty()) {
                System.err.println("Internship CSV is empty.");
                return false;
            }

            boolean updated = false;
            // Iterate from 1 to preserve header (if header present)
            int start = 1;
            for (int i = start; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line == null || line.trim().isEmpty()) continue;

                String[] cols = line.split(splitRegex, -1);
                if (cols.length == 0) continue;

                String id = cols.length > 0 ? cols[0].trim() : "";
                if (!id.equals(internship.getUUID().toString())) continue;

                // Ensure we have at least 12 columns
                if (cols.length < 12) {
                    String[] nc = Arrays.copyOf(cols, 12);
                    for (int k = cols.length; k < nc.length; k++) nc[k] = "";
                    cols = nc;
                }

                // Update only fields that were provided (non-null and not empty)
                if (newTitle != null && !newTitle.isEmpty()) cols[1] = esc.apply(newTitle);
                if (newDescription != null && !newDescription.isEmpty()) cols[2] = esc.apply(newDescription);
                if (newLevel != null && !newLevel.isEmpty()) cols[3] = esc.apply(newLevel);
                if (newPreferredMajor != null && !newPreferredMajor.isEmpty()) cols[4] = esc.apply(newPreferredMajor);
                if (newOpeningDate != null && !newOpeningDate.isEmpty()) cols[5] = esc.apply(newOpeningDate);
                if (newClosingDate != null && !newClosingDate.isEmpty()) cols[6] = esc.apply(newClosingDate);
                // status (7) must not be changed by this method
                // company name (8) and representatives (9) remain unchanged
                if (newNumberOfSlots != null && !newNumberOfSlots.isEmpty()) cols[10] = esc.apply(newNumberOfSlots);
                // visibility (11) untouched here

                String updatedLine = String.join(",", cols);
                lines.set(i, updatedLine);
                updated = true;
                break; // update the matched row only
            }

            if (updated) {
                Files.write(csv, lines, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                // Refresh in-memory data
                loadInternships(internshipPath);
                return true;
            } else {
                System.err.println("Failed to find matching internship to edit.");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Failed to edit internship in CSV: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteInternship(String companyName, int number) {
        List<Internship> internshipList = viewMyInternships(companyName);
        if (internshipList.isEmpty()) {
            System.err.println("You have no internships listed.");
            return false;
        }

        if (number < 1 || number > internshipList.size()) {
            System.err.println("Error: Invalid internship number.");
            return false;
        }

        Internship internship = internshipList.get(number - 1);

        // Prevent deletion if the internship has been approved
        String status = internship.getStatus();
        if (status != null && status.toLowerCase().contains("approved")) {
            System.err.println("Cannot Delete: Internship has already been approved.");
            return false;
        }

        try {
            // Read all lines from the CSV
            List<String> lines = Files.readAllLines(internshipPath);
            // Remove the specific internship line
            lines = lines.stream()
                    .filter(line -> {
                        String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                        return !(cols.length > 0 && cols[0].trim().equals(internship.getUUID().toString()));
                    })
                    .collect(Collectors.toList());
            // Write back all lines to the CSV
            Files.write(internshipPath, lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to delete internship from CSV: " + e.getMessage());
            return false;
        }
        return true;
    }

    public List<Internship> viewMyInternships(String companyName) {
        loadInternships(internshipPath);
        if (companyName == null || companyName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String target = companyName.trim();
        return internships.values().stream()
                .filter(i -> i.getCompanyName() != null &&
                        i.getCompanyName().trim().equalsIgnoreCase(target))
                .collect(Collectors.toList());
    }

    public boolean toggleInternshipVisibility(String companyName, int number, int option) {


        List<Internship> internshipList = viewMyInternships(companyName);
        if (internshipList.isEmpty()) {
            System.err.println("You have no internships listed.");
            return false;
        }

        if (number < 1 || number > internshipList.size()) {
            System.err.println("Error: Invalid internship number.");
            return false;
        }

        Internship internship = internshipList.get(number - 1);
        boolean newVisibility = (option == 1); // 1 for visible, 2 for not visible
        internship.setVisibility(newVisibility);

        try {
            // Read all lines from the CSV
            List<String> lines = Files.readAllLines(internshipPath);
            // Update the specific internship line
            for (int i = 1; i < lines.size(); i++) { // Start from 1 to skip header
                String line = lines.get(i);
                String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (cols.length > 0 && cols[0].trim().equals(internship.getUUID().toString())) {
                    // Update visibility column (index 11)
                    cols[11] = String.valueOf(newVisibility);
                    // Reconstruct the line
                    String updatedLine = String.join(",", cols);
                    lines.set(i, updatedLine);
                    break;
                }
            }
            // Write back all lines to the CSV
            Files.write(internshipPath, lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to update internship visibility in CSV: " + e.getMessage());
            return false;
        }
        return true;

    }

    public Map<String, List<Application>> getInternshipsWithApplications(String companyName) {
        Path internshipsCsv = Paths.get("data/sample_internship_list.csv");
        Path applicationsCsv = Paths.get("data/sample_application_list.csv");
        String splitRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        // 1. Collect all internship IDs owned by the company
        Map<String, Internship> companyInternships = new HashMap<>();
        if (!Files.exists(internshipsCsv)) return Collections.emptyMap();
        try {
            List<String> lines = Files.readAllLines(internshipsCsv);
            for (int i = 1; i < lines.size(); i++) { // skip header
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                String[] cols = line.split(splitRegex, -1);

                // Re-use logic from loadInternships to create an object, or just get the ID
                String id = cols.length > 0 ? cols[0].trim() : "";
                String companyCol = cols.length > 8 ? cols[8].trim() : "";

                if (!id.isEmpty() && companyCol.equalsIgnoreCase(companyName.trim())) {
                    // Fetch the actual Internship object from the in-memory map or re-parse a minimal one
                    Internship internship = internships.get(id); // Use the loaded map for objects
                    if (internship != null) {
                        companyInternships.put(id, internship);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read internships CSV: " + e.getMessage());
            return Collections.emptyMap();
        }

        if (companyInternships.isEmpty()) return Collections.emptyMap();

        // 2. Read applications and group them by the matched Internship ID
        Map<String, List<Application>> results = companyInternships.keySet().stream()
                .collect(Collectors.toMap(id -> id, id -> new ArrayList<>())); // Initialize map with all company internship IDs

        if (!Files.exists(applicationsCsv)) return results; // Return map with empty lists if no application file

        Set<String> companyInternshipIds = companyInternships.keySet();

        try {
            List<String> lines = Files.readAllLines(applicationsCsv);
            for (int i = 1; i < lines.size(); i++) { // skip header
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                String[] cols = line.split(splitRegex, -1);

                // Detect internship id anywhere in the row (assuming index 8 is the ID in the application CSV for now)
                // NOTE: Application CSV structure is unknown, assuming a key column for Internship ID exists.
                // Based on the 'viewApplications' logic, it looks for the internship ID anywhere in the row.
                String matchedInternshipId = null;
                if (cols.length > 8) { // Assuming the ID is at index 8 of the application CSV
                    String candidate = cols[8].trim();
                    if (companyInternshipIds.contains(candidate)) {
                        matchedInternshipId = candidate;
                    }
                }

                // Fallback to iterating all columns if the ID placement isn't fixed (as per original logic)
                if (matchedInternshipId == null) {
                    for (String col : cols) {
                        String candidate = col.trim();
                        if (companyInternshipIds.contains(candidate)) {
                            matchedInternshipId = candidate;
                            break;
                        }
                    }
                }

                if (matchedInternshipId == null) continue;

                // Simple, defensive parsing to create Application object
                UUID appUuid;
                try {
                    appUuid = UUID.fromString(cols.length > 0 ? cols[0].trim() : UUID.randomUUID().toString());
                } catch (Exception ex) {
                    appUuid = UUID.randomUUID();
                }
                String userId = cols.length > 1 ? cols[1].trim() : "";
                String name = cols.length > 2 ? cols[2].trim() : "";
                String email = cols.length > 3 ? cols[3].trim() : "";
                String major = cols.length > 4 ? cols[4].trim() : "";
                int year = cols.length > 5 ? Integer.parseInt(cols[5].trim()) : -1;
                String submittedDate = cols.length > 6 ? cols[6].trim() : "";
                String status = cols.length > 7 ? cols[7].trim() : "";

                Application app = new Application(appUuid, status, submittedDate, userId, name, email, major, year);
                results.get(matchedInternshipId).add(app);
            }
        } catch (IOException e) {
            System.err.println("Failed to read applications CSV: " + e.getMessage());
            return Collections.emptyMap();
        }

        // Now, results contains Map<Internship UUID, List<Application>>
        return results;
    }

    public boolean updateApplicationStatus(String internshipUUID, String studentUserId, String newStatus) {
        Path applicationsCsv = Paths.get("data/sample_application_list.csv");
        String splitRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        if (internshipUUID == null || studentUserId == null || newStatus == null || !Files.exists(applicationsCsv)) {
            System.err.println("Error: Invalid parameters or application CSV not found.");
            return false;
        }

        java.util.function.Function<String, String> unquote = s -> {
            if (s == null) return "";
            s = s.trim();
            if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
                s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
            }
            return s;
        };

        String targetInternshipId = unquote.apply(internshipUUID);
        String targetUserId = unquote.apply(studentUserId);

        try {
            List<String> lines = Files.readAllLines(applicationsCsv);
            if (lines.isEmpty()) {
                System.err.println("Error: Application CSV is empty.");
                return false;
            }

            // Detect header
            String firstLower = lines.get(0).toLowerCase();
            boolean headerPresent = firstLower.contains("uuid") || firstLower.contains("application") || firstLower.contains("user") || firstLower.contains("status");
            int startIdx = headerPresent ? 1 : 0;

            boolean updated = false;
            for (int i = startIdx; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line == null || line.trim().isEmpty()) continue;

                String[] cols = line.split(splitRegex, -1);
                if (cols.length == 0) continue;

                // user id should be column 1 (defensive)
                String rowUserId = cols.length > 1 ? unquote.apply(cols[1]) : "";
                if (!rowUserId.equals(targetUserId)) continue;

                // check that the internship UUID appears somewhere in the row
                boolean internshipFound = false;
                for (String col : cols) {
                    if (unquote.apply(col).equals(targetInternshipId)) {
                        internshipFound = true;
                        break;
                    }
                }
                if (!internshipFound) continue;

                // Ensure at least 8 columns (status index = 7)
                if (cols.length <= 7) {
                    String[] newCols = Arrays.copyOf(cols, 8);
                    for (int k = cols.length; k < newCols.length; k++) newCols[k] = "";
                    cols = newCols;
                }

                // Preserve quoting style and escape new status
                String origStatus = cols[7] != null ? cols[7] : "";
                boolean origQuoted = origStatus.startsWith("\"") && origStatus.endsWith("\"");
                String escStatus = newStatus.replace("\"", "\"\"");
                if (origQuoted || escStatus.contains(",") || escStatus.contains("\"") || escStatus.contains("\n") || escStatus.contains("\r")) {
                    escStatus = "\"" + escStatus + "\"";
                }
                cols[7] = escStatus;

                // Reconstruct and replace line
                String updatedLine = String.join(",", cols);
                lines.set(i, updatedLine);
                updated = true;
                break; // update only the first matching row
            }

            if (updated) {
                Files.write(applicationsCsv, lines, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                return true;
            } else {
                System.err.println("Error: Matching application not found in CSV.");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Failed to update application status in CSV: " + e.getMessage());
            return false;
        }
    }
}
