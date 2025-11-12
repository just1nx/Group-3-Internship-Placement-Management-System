package control;

import entity.CompanyRepresentative;
import entity.Internship;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

public class CompanyRepresentativeController {
    public CompanyRepresentativeController() {
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
        Path csv = Paths.get("data/sample_internship_list.csv");
        // 1. Generate unique ID and set default values
        UUID uuid = UUID.randomUUID();
        String status = "Pending"; // As per PDF, must be approved by staff
        String visibility = "TRUE";  // Default to visible, but staff approval is the real gate

        // 2. Format the data as a CSV line
        // Columns: Uuid,Title,Description,Level,PreferredMajor,OpeningDate,ClosingDate,Status,CompanyName,Representatives,NumberOfSlots,Visibility
        String csvLine = String.join(",",
                escapeCSV(uuid.toString()),
                escapeCSV(title),
                escapeCSV(description),
                escapeCSV(level),
                escapeCSV(preferredMajor),
                escapeCSV(openingDate),
                escapeCSV(closingDate),
                escapeCSV(status),
                escapeCSV(companyName),
                escapeCSV(representativeId),
                escapeCSV(String.valueOf(numberOfSlots)),
                escapeCSV(visibility)
        );

        // 3. Append the new line to the CSV file
        try {
            // Check if file exists, if not, create it and add header
            if (!Files.exists(csv)) {
                String header = "Uuid,Title,Description,Level,PreferredMajor,OpeningDate,ClosingDate,Status,CompanyName,Representatives,NumberOfSlots,Visibility";
                Files.write(csv, Collections.singletonList(header), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }

            // Append the new internship line
            Files.write(csv, Collections.singletonList(csvLine),
                    StandardOpenOption.CREATE,    // Create the file if it doesn't exist
                    StandardOpenOption.APPEND     // Append to the end of the file
            );
            return true; // Success
        } catch (IOException e) {
            System.err.println("Failed to write internship to CSV: " + e.getMessage());
            return false; // Failure
        }
    }

    /**
     * Helper method to escape special characters for CSV format.
     * Wraps strings containing commas or quotes in double quotes.
     *
     * @param s The string to escape.
     * @return A CSV-safe string.
     */
    private String escapeCSV(String s) {
        if (s == null) s = "";
        String out = s.replace("\"", "\"\""); // Escape existing quotes
        if (out.contains(",") || out.contains("\"") || out.contains("\n") || out.contains("\r")) {
            out = "\"" + out + "\""; // Wrap in quotes
        }
        return out;
    }

    public boolean canCreateMoreInternships(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            System.err.println("Error: Company name cannot be null or empty.");
            return false;
        }

        Path csv = Paths.get("data/sample_internship_list.csv");
        // We must read the file live to get the most up-to-date count.
        if (!Files.exists(csv)) {
            System.err.println("Internship CSV not found. Assuming 0 internships: " + csv);
            return true; // No file means 0 internships, so they can create.
        }

        long count = 0;
        try (Stream<String> lines = Files.lines(csv)) {
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
        System.out.println("number: " + count);
        return count < 5;
    }

}

//    public boolean canCreateMoreInternships(String companyName) {
//        if (companyName == null || companyName.trim().isEmpty()) return false;
//
//        Path csv = Paths.get("data/sample_internship_list.csv");
//        if (!Files.exists(csv)) {
//            System.err.println("CSV file not found: " + csv);
//            return false; // Cannot check, assume no limit issue for now or handle as error
//        }
//
//        // We use a Set to store unique Uuids for the given company.
//        Set<String> uniqueInternshipUuids = new HashSet<>();
//
//        // Define the delimiter and the column indices for Uuid and CompanyName
//        final String DELIMITER = ",";
//        final int UUID_INDEX = 0; // Uuid is the 1st column
//        final int COMPANY_NAME_INDEX = 8; // CompanyName is the 9th column (index 8)
//
//        // A maximum limit for the number of internships a company can create
//        final int MAX_INTERNSHIPS = 5;
//
//        try (BufferedReader br = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
//            // Read and skip the header line
//            String line = br.readLine();
//
//            // Iterate over the rest of the lines
//            while ((line = br.readLine()) != null) {
//                // Simple split. Note: A dedicated CSV parser library (like OpenCSV) is
//                // generally recommended for production code to handle complex cases
//                // like commas within quoted fields.
//                String[] values = line.split(DELIMITER);
//
//                // Assuming the CSV structure is consistent and has enough columns
//                if (values.length > COMPANY_NAME_INDEX) {
//                    String uuid = values[UUID_INDEX].trim();
//                    String currentCompanyName = values[COMPANY_NAME_INDEX].trim();
//
//                    // Check if the current row belongs to the target company
//                    if (currentCompanyName.equalsIgnoreCase(companyName.trim())) {
//                        // Add the Uuid. Sets only store unique elements.
//                        uniqueInternshipUuids.add(uuid);
//                        System.out.println("uuid added: " + uuid);
//                    }
//                }
//            }
//        } catch (IOException e) {
//            // Handle file reading errors
//            System.err.println("Error reading CSV file: " + e.getMessage());
//            return false; // Return false on error
//        }
//        System.out.println("Number of unique internship uuids: " + uniqueInternshipUuids.size());
//        // Return true if the count of unique Uuids is less than the limit
//        return uniqueInternshipUuids.size() < MAX_INTERNSHIPS;
//    }