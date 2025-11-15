package control;

import entity.CompanyRepresentative;
import entity.Internship;
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

public class CareerCenterStaffController {
    private final List<CompanyRepresentative> companyReps =  new ArrayList<>();
    private final List<Internship> internships = new ArrayList<>();
    private final List<Withdrawal> withdrawals = new ArrayList<>();
    // Define the paths to the data files
    private static final Path companyRepPath = Paths.get("data/sample_company_representative_list.csv");
    private static final Path internshipPath = Paths.get("data/sample_internship_list.csv");
    private static final Path withdrawalPath  = Paths.get("data/sample_withdrawal_list.csv");

    public CareerCenterStaffController() {
        loadCompanyReps(companyRepPath);
        loadInternships(internshipPath);
        loadWithdrawals(withdrawalPath);
    }

    // --- Private Helper Methods to Load Data ---
    private void loadCompanyReps(Path csvPath) {
        if (!Files.exists(csvPath)) {
            System.err.println("Company representatives CSV not found: " + csvPath);
            return;
        }

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1) // Skip header
                    .map(line -> line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1))
                    .filter(cols -> cols.length == 8)
                    .forEach(cols -> {
                        String id = unquote(cols[0]);
                        String name = unquote(cols[1]);
                        String companyName = unquote(cols[2]);
                        String department = unquote(cols[3]);
                        String position = unquote(cols[4]);
                        String email = unquote(cols[5]);
                        String status = unquote(cols[6]);
                        String pw = unquote(cols[7]);
                        pw = pw.isEmpty() ? "password" : pw;

                        CompanyRepresentative companyRep = new CompanyRepresentative(id, name, pw, email, companyName, department, position, status);
                        companyReps.add(companyRep);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read company representatives CSV: " + e.getMessage());
        }
    }

    private void loadInternships(Path csvPath) {
        if (!Files.exists(internshipPath)) {
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
                        internships.add(internship);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read internship CSV: " + e.getMessage());
        }
    }

    private void loadWithdrawals(Path csvPath) {
        if (!Files.exists(csvPath)) {
            System.err.println("Withdrawal CSV not found: " + csvPath);
            return;
        }

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1) // skip header
                    .map(line -> line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1))
                    .filter(cols -> cols.length == 8) // Must have 8 columns
                    .forEach(cols -> {
                        // This is the Internship's UUID
                        String internshipId = unquote(cols[0]);
                        UUID withUuid = UUID.fromString(internshipId);

                        String userId = unquote(cols[1]);
                        String name = unquote(cols[2]);
                        String email = unquote(cols[3]);
                        String major = unquote(cols[4]);
                        int year = Integer.parseInt(unquote(cols[5]));
                        String submittedDate = unquote(cols[6]);
                        String status = unquote(cols[7]);

                        Withdrawal withdrawal = new Withdrawal(withUuid, status, submittedDate, userId, name, email, major, year);
                        withdrawals.add(withdrawal);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read withdrawal CSV: " + e.getMessage());
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
                    escapeCSV(String.valueOf(internship.getNumberOfSlots())),
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

    private boolean rewriteWithdrawalCSV() {
        List<String> lines = new ArrayList<>();
        // Add header
        lines.add("UUID,UserId,Name,Email,Major,Year,SubmittedDate,Status");

        // Add data lines from in-memory map
        for (Withdrawal withdrawal : withdrawals) {
            lines.add(String.join(",",
                    escapeCSV(withdrawal.getUUID().toString()), // 0: UUID (which is the InternshipUUID)
                    escapeCSV(withdrawal.getUserId()),          // 1: UserId
                    escapeCSV(withdrawal.getName()),            // 2: Name
                    escapeCSV(withdrawal.getEmail()),           // 3: Email
                    escapeCSV(withdrawal.getMajor()),           // 4: Major
                    escapeCSV(String.valueOf(withdrawal.getYear())), // 5: Year
                    escapeCSV(withdrawal.getSubmittedDate()),     // 6: SubmittedDate
                    escapeCSV(withdrawal.getStatus())             // 7: Status
            ));
        }

        // Write to file, overwriting existing content
        try {
            Files.write(withdrawalPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to rewrite withdrawal CSV: " + e.getMessage());
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


    // Public Methods for Company Rep Management
    public List<CompanyRepresentative> getPendingRegistrations() {
        return companyReps.stream()
                .filter(rep -> rep.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());
    }

    public boolean approveRegistration(CompanyRepresentative repToApprove) {
        if (repToApprove != null) {
            repToApprove.setStatus("Approved");
            return rewriteCompanyRepCSV(); // Write changes to file
        }
        return false; // Rep not found
    }

    public boolean rejectRegistration(CompanyRepresentative repToReject) {
        if (repToReject != null) {
            repToReject.setStatus("Rejected");
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

    public boolean approveInternship(Internship internshipToApprove) {
        if (internshipToApprove != null) {
            internshipToApprove.setStatus("Approved");
            return rewriteInternshipCSV(); // Write changes to file
        }
        return false; // Internship not found
    }

    public boolean rejectInternship(Internship internshipToReject) {
        if (internshipToReject != null) {
            internshipToReject.setStatus("Rejected");
            return rewriteInternshipCSV(); // Write changes to file
        }
        return false; // Internship not found
    }


    // Public Methods for Withdrawal Management
    public List<Withdrawal> getPendingWithdrawals() {
        return withdrawals.stream()
                .filter(withdrawal -> withdrawal.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());
    }

    public boolean approveWithdrawal(Withdrawal withdrawalToApprove) {
        if (withdrawalToApprove != null) {
            withdrawalToApprove.setStatus("Approved");
            return rewriteWithdrawalCSV(); // Write changes to file
        }
        return false; // Withdrawal not found
    }

    public boolean rejectWithdrawal(Withdrawal withdrawalToReject) {
        if (withdrawalToReject != null) {
            withdrawalToReject.setStatus("Rejected");
            return rewriteWithdrawalCSV(); // Write changes to file
        }
        return false; // Withdrawal not found
    }

    public void generateReport() {
        System.out.println("... (To be implemented: Logic to generate reports) ...");
        // This will involve loading internships and filtering them
    }

    public List<Internship> viewAllInternships(List<String> statusFilters, List<String> levelFilters,
                                               List<String> companyFilters, List<String> majorFilters) {

        // Start with a stream of all internships
        Stream<Internship> stream = internships.stream();

        // Apply status filter if provided
        if (statusFilters != null && !statusFilters.isEmpty()) {
            // Normalize the filter list for case-insensitive comparison
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

        // Apply company filter if provided
        if (companyFilters != null && !companyFilters.isEmpty()) {
            Set<String> normalizedFilters = companyFilters.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            stream = stream.filter(i -> i.getCompanyName() != null &&
                    normalizedFilters.contains(i.getCompanyName().toLowerCase()));
        }

        // Apply major filter if provided
        if (majorFilters != null && !majorFilters.isEmpty()) {
            Set<String> normalizedFilters = majorFilters.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            stream = stream.filter(i -> i.getPreferredMajor() != null &&
                    normalizedFilters.contains(i.getPreferredMajor().toLowerCase()));
        }

        // Collect the results from the stream into a list
        List<Internship> filteredList = stream.collect(Collectors.toList());

        // Sort the list by title (case-insensitive)
        filteredList.sort(Comparator.comparing(Internship::getTitle, String.CASE_INSENSITIVE_ORDER));

        return filteredList;
    }
}
