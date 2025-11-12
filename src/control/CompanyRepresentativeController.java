package control;

import entity.Internship;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
                        String preferredMajor = cols.length > 4 ? cols[4].trim() : "";
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
        String visibility = "TRUE";  // Default to visible, but staff approval is the real gate

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

    // TODO: Add other methods for the Company Representative:
    // public List<Internship> viewMyInternships(...)
    // public boolean updateInternshipVisibility(...)
    // public List<Application> viewApplicationsForInternship(...)
    // public boolean approveApplication(...)
    // public boolean rejectApplication(...)
}
