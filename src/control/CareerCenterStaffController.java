package control;

import entity.CompanyRepresentative;
import entity.Internship;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CareerCenterStaffController {
    private final List<CompanyRepresentative> companyReps =  new ArrayList<>();
    private final List<Internship> internships = new ArrayList<>();
    // Define the paths to the data files
    private static final Path companyRepPath = Paths.get("data/sample_company_representative_list.csv");
    private static final Path internshipPath = Paths.get("data/sample_internship_list.csv");


    public CareerCenterStaffController() {
        loadCompanyReps();
        loadInternships();
    }

    // --- Private Helper Methods to Load Data ---
    private void loadCompanyReps() {
        if (!Files.exists(companyRepPath)) {
            System.err.println("Company representatives CSV not found: " + companyRepPath);
            return;
        }

        try (Stream<String> lines = Files.lines(companyRepPath)) {
            lines.skip(1) // Skip header
                    .map(line -> line.split(",", -1))
                    .filter(cols -> cols.length > 0 && !cols[0].trim().isEmpty())
                    .forEach(cols -> {
                        String id = cols[0].trim();
                        String name = cols.length > 1 ? cols[1].trim() : "";
                        String companyName = cols.length > 2 ? cols[2].trim() : "";
                        String department = cols.length > 3 ? cols[3].trim() : "";
                        String position = cols.length > 4 ? cols[4].trim() : "";
                        String email = cols.length > 5 ? cols[5].trim() : "";
                        String status = cols.length > 6 ? cols[6].trim() : "Pending";
                        String pw = cols.length > 7 && !cols[7].trim().isEmpty() ? cols[7].trim() : "password";

                        CompanyRepresentative companyRep = new CompanyRepresentative(id, name, pw, email, companyName, department, position, status);
                        companyReps.add(companyRep);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read company representatives CSV: " + e.getMessage());
        }
    }

    private void loadInternships() {
        if (!Files.exists(internshipPath)) {
            System.err.println("Internship CSV not found: " + internshipPath);
            return;
        }

        try (Stream<String> lines = Files.lines(internshipPath)) {
            lines.skip(1) // Skip header
                    .map(line -> line.split(",", -1))
                    .filter(cols -> cols.length > 0 && !cols[0].trim().isEmpty())
                    .forEach(cols -> {
                        UUID id = UUID.fromString(cols[0].trim());
                        String title = cols.length > 1 ? cols[1].trim() : "";
                        String description = cols.length > 2 ? cols[2].trim() : "";
                        String level = cols.length > 3 ? cols[3].trim() : "";
                        String preferredMajor = cols.length > 4 ? cols[4].trim() : "";
                        LocalDate openingDate = LocalDate.parse(cols[5].trim()); // Assumes valid format
                        LocalDate closingDate = LocalDate.parse(cols[6].trim()); // Assumes valid format
                        String status = cols.length > 7 ? cols[7].trim() : "Pending";
                        String companyName = cols.length > 8 ? cols[8].trim() : "";
                        String representatives = cols.length > 9 ? cols[9].trim() : "";
                        String numberOfSlots = cols.length > 10 ? cols[10].trim() : "0";
                        boolean visibility = cols.length > 11 && Boolean.parseBoolean(cols[11].trim());

                        Internship internship = new Internship(id, title, description, level, preferredMajor, openingDate, closingDate, status, companyName, representatives, numberOfSlots, visibility);
                        internships.add(internship);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read internship CSV: " + e.getMessage());
        }
    }

    private boolean rewriteCompanyRepCSV() {
        List<String> lines = new ArrayList<>();
        // Add header
        lines.add("CompanyRepID,Name,CompanyName,Department,Position,Email,Status,Password");

        // Add data lines from in-memory list
        for (CompanyRepresentative rep : companyReps) {
            lines.add(String.join(",",
                    escapeCSV(rep.getUserID()),
                    escapeCSV(rep.getName()),
                    escapeCSV(rep.getCompanyName()),
                    escapeCSV(rep.getDepartment()),
                    escapeCSV(rep.getPosition()),
                    escapeCSV(rep.getEmail()),
                    escapeCSV(rep.getStatus()),
                    escapeCSV(rep.getPasswordHash())
            ));
        }

        // Write to file, overwriting existing content
        try {
            Files.write(companyRepPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to rewrite company rep CSV: " + e.getMessage());
            return false;
        }
    }

    private boolean rewriteInternshipCSV() {
        List<String> lines = new ArrayList<>();
        // Add header
        lines.add("UUID,Title,Description,Level,PreferredMajor,OpeningDate,ClosingDate,Status,CompanyName,Representatives,NumberOfSlots,Visibility");

        // Add data lines from in-memory list
        for (Internship internship : internships) {
            lines.add(String.join(",",
                    escapeCSV(internship.getUUID().toString()),
                    escapeCSV(internship.getTitle()),
                    escapeCSV(internship.getDescription()),
                    escapeCSV(internship.getLevel()),
                    escapeCSV(internship.getPreferredMajor()),
                    escapeCSV(internship.getOpeningDate().toString()), // Format date to string
                    escapeCSV(internship.getClosingDate().toString()), // Format date to string
                    escapeCSV(internship.getStatus()),
                    escapeCSV(internship.getCompanyName()),
                    escapeCSV(internship.getRepresentatives()),
                    escapeCSV(internship.getNumberOfSlots()),
                    escapeCSV(String.valueOf(internship.isVisible()).toUpperCase()) // Format boolean to string
            ));
        }

        // Write to file, overwriting existing content
        try {
            Files.write(internshipPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to rewrite internship CSV: " + e.getMessage());
            return false;
        }
    }

    private String escapeCSV(String s) {
        if (s == null) s = "";
        String out = s.replace("\"", "\"\""); // Escape existing quotes
        if (out.contains(",") || out.contains("\"") || out.contains("\n") || out.contains("\r")) {
            out = "\"" + out + "\""; // Wrap in quotes
        }
        return out;
    }


    // Public Methods for Company Rep Management
    public List<CompanyRepresentative> getPendingRegistrations() {
        return companyReps.stream()
                .filter(rep -> rep.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());
    }

    public boolean approveRegistration(String repId) {
        CompanyRepresentative repToUpdate = null;
        for (CompanyRepresentative rep : companyReps) {
            if (rep.getUserID().equalsIgnoreCase(repId)) {
                repToUpdate = rep;
                break;
            }
        }

        if (repToUpdate != null) {
            repToUpdate.setStatus("Approved");
            return rewriteCompanyRepCSV(); // Write changes to file
        }
        return false; // Rep not found
    }

    public boolean rejectRegistration(String repId) {
        CompanyRepresentative repToUpdate = null;
        for (CompanyRepresentative rep : companyReps) {
            if (rep.getUserID().equalsIgnoreCase(repId)) {
                repToUpdate = rep;
                break;
            }
        }

        if (repToUpdate != null) {
            repToUpdate.setStatus("Rejected");
            return rewriteCompanyRepCSV(); // Write changes to file
        }
        return false; // Rep not found
    }


    // Public Methods for Internship Management
    public List<Internship> getPendingInternships() {
        return internships.stream()
                .filter(internship -> internship.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());
    }

    public boolean approveInternship(String internshipId) {
        Internship internshipToUpdate = null;
        for (Internship internship : internships) {
            if (internship.getUUID().toString().equalsIgnoreCase(internshipId)) {
                internshipToUpdate = internship;
                break;
            }
        }

        if (internshipToUpdate != null) {
            internshipToUpdate.setStatus("Approved");
            return rewriteInternshipCSV(); // Write changes to file
        }
        return false; // Internship not found
    }

    public boolean rejectInternship(String internshipId) {
        Internship internshipToUpdate = null;
        for (Internship internship : internships) {
            if (internship.getUUID().toString().equalsIgnoreCase(internshipId)) {
                internshipToUpdate = internship;
                break;
            }
        }

        if (internshipToUpdate != null) {
            internshipToUpdate.setStatus("Rejected");
            return rewriteInternshipCSV(); // Write changes to file
        }
        return false; // Internship not found
    }

    // --- Stubs for other methods ---
    public void getPendingWithdrawals() {
        System.out.println("... (To be implemented: Logic to get pending withdrawals) ...");
        // This will likely involve reading a new 'sample_withdrawal_requests.csv'
    }

    public void generateReport() {
        System.out.println("... (To be implemented: Logic to generate reports) ...");
        // This will involve loading internships and filtering them
    }

    public List<Internship> viewAllInternships() {
        System.out.println("... (To be implemented: Logic to view all internships) ...");
        // This will just call loadInternships()
        return internships;
    }
}