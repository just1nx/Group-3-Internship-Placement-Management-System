package control;

import entity.CompanyRepresentative;
import entity.Internship;
import entity.Withdrawal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CareerCenterStaffController extends BaseController {
    private final Map<String, CompanyRepresentative> companyReps;
    private final Map<String, Internship> internships;
    private final Map<String, List<Withdrawal>> withdrawals;
    // Define the paths to the data files
    private static final Path companyRepPath = Paths.get("data/sample_company_representative_list.csv");
    private static final Path internshipPath = Paths.get("data/sample_internship_list.csv");
    private static final Path withdrawalPath  = Paths.get("data/sample_withdrawal_list.csv");

    public CareerCenterStaffController() {
        companyReps = loadCompanyReps(companyRepPath);
        internships = loadInternships(internshipPath);
        withdrawals = loadWithdrawals(withdrawalPath);
    }

    // Public Methods for Company Rep Management
    public List<CompanyRepresentative> getPendingRegistrations() {
        return companyReps.values().stream()
                .filter(rep -> rep.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());
    }

    public boolean approveRegistration(CompanyRepresentative repToApprove) {
        if (repToApprove != null) {
            repToApprove.setStatus("Approved");
            return rewriteCompanyRepCSV(companyRepPath, companyReps); // Write changes to file
        }
        return false; // Rep not found
    }

    public boolean rejectRegistration(CompanyRepresentative repToReject) {
        if (repToReject != null) {
            repToReject.setStatus("Rejected");
            return rewriteCompanyRepCSV(companyRepPath, companyReps); // Write changes to file
        }
        return false; // Rep not found
    }


    // Public Methods for Internship Management
    public List<Internship> getPendingInternships() {
        return internships.values().stream()
                .filter(internship -> internship.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());
    }

    public boolean approveInternship(Internship internshipToApprove) {
        if (internshipToApprove != null) {
            internshipToApprove.setStatus("Approved");
            return rewriteInternshipCSV(internshipPath, internships); // Write changes to file
        }
        return false; // Internship not found
    }

    public boolean rejectInternship(Internship internshipToReject) {
        if (internshipToReject != null) {
            internshipToReject.setStatus("Rejected");
            return rewriteInternshipCSV(internshipPath, internships); // Write changes to file
        }
        return false; // Internship not found
    }


    // Public Methods for Withdrawal Management
    public List<Withdrawal> getPendingWithdrawals() {
        return withdrawals.values().stream()
                .flatMap(List::stream)
                .filter(withdrawal -> withdrawal.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());
    }

    public boolean approveWithdrawal(Withdrawal withdrawalToApprove) {
        if (withdrawalToApprove != null) {
            withdrawalToApprove.setStatus("Approved");
            return rewriteWithdrawalCSV(withdrawalPath, withdrawals); // Write changes to file
        }
        return false; // Withdrawal not found
    }

    public boolean rejectWithdrawal(Withdrawal withdrawalToReject) {
        if (withdrawalToReject != null) {
            withdrawalToReject.setStatus("Rejected");
            return rewriteWithdrawalCSV(withdrawalPath, withdrawals); // Write changes to file
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
        Stream<Internship> stream = internships.values().stream();

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
