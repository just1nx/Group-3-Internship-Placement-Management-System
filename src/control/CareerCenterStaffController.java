package control;

import entity.Application;
import entity.CompanyRepresentative;
import entity.Internship;
import entity.Withdrawal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Controller for Career Centre staff operations.
 * <p>
 * Provides company registration approval, internship approval/rejection,
 * withdrawal management and reporting features.
 * </p>
 */
public class CareerCenterStaffController extends BaseController {
    private final Map<String, CompanyRepresentative> companyReps;
    private final Map<String, Internship> internships;
    private final Map<String, List<Withdrawal>> withdrawals;
    private final Map<String, List<Application>> applications;
    // Define the paths to the data files
    private static final Path companyRepPath = Paths.get("data/sample_company_representative_list.csv");
    private static final Path internshipPath = Paths.get("data/sample_internship_list.csv");
    private static final Path withdrawalPath  = Paths.get("data/sample_withdrawal_list.csv");
    private static final Path applicationPath = Paths.get("data/sample_application_list.csv");

    /**
     * Construct controller and load relevant CSV-backed data into memory.
     */
    public CareerCenterStaffController() {
        companyReps = loadCompanyReps(companyRepPath);
        internships = loadInternships(internshipPath);
        withdrawals = loadWithdrawals(withdrawalPath);
        applications = loadApplications(applicationPath);
    }

    /**
     * Get list of company representative registrations currently marked "Pending".
     *
     * @return list of pending CompanyRepresentative objects (may be empty)
     */
    public List<CompanyRepresentative> getPendingRegistrations() {
        return companyReps.values().stream()
                .filter(rep -> rep.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());
    }

    /**
     * Approve a pending company representative registration and persist the change.
     *
     * @param repToApprove the CompanyRepresentative to approve
     * @return true on success, false otherwise
     */
    public boolean approveRegistration(CompanyRepresentative repToApprove) {
        if (repToApprove != null) {
            repToApprove.setStatus("Approved");
            return rewriteCompanyRepCSV(companyRepPath, companyReps); // Write changes to file
        }
        return false; // Rep not found
    }

    /**
     * Reject a company representative registration and persist the change.
     *
     * @param repToReject the CompanyRepresentative to reject
     * @return true on success, false otherwise
     */
    public boolean rejectRegistration(CompanyRepresentative repToReject) {
        if (repToReject != null) {
            repToReject.setStatus("Rejected");
            return rewriteCompanyRepCSV(companyRepPath, companyReps); // Write changes to file
        }
        return false; // Rep not found
    }


    /**
     * Return internships with status "Pending".
     *
     * @return list of pending internships
     */
    public List<Internship> getPendingInternships() {
        return internships.values().stream()
                .filter(internship -> internship.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());
    }

    /**
     * Approve the provided internship and persist changes.
     *
     * @param internshipToApprove internship to approve
     * @return true when persisted successfully
     */
    public boolean approveInternship(Internship internshipToApprove) {
        if (internshipToApprove != null) {
            internshipToApprove.setStatus("Approved");
            return rewriteInternshipCSV(internshipPath, internships); // Write changes to file
        }
        return false; // Internship not found
    }

    /**
     * Reject the provided internship and persist changes.
     *
     * @param internshipToReject internship to reject
     * @return true when persisted successfully
     */
    public boolean rejectInternship(Internship internshipToReject) {
        if (internshipToReject != null) {
            internshipToReject.setStatus("Rejected");
            return rewriteInternshipCSV(internshipPath, internships); // Write changes to file
        }
        return false; // Internship not found
    }


    /**
     * Get withdrawal requests currently marked "Pending".
     *
     * @return list of pending Withdrawal objects
     */
    public List<Withdrawal> getPendingWithdrawals() {
        return withdrawals.values().stream()
                .flatMap(List::stream)
                .filter(withdrawal -> withdrawal.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());
    }

    /**
     * Approve a withdrawal request and persist the change.
     *
     * @param withdrawalToApprove the Withdrawal to approve
     * @return true on success
     */
    public boolean approveWithdrawal(Withdrawal withdrawalToApprove) {
        if (withdrawalToApprove != null) {
            withdrawalToApprove.setStatus("Approved");
            return rewriteWithdrawalCSV(withdrawalPath, withdrawals); // Write changes to file
        }
        return false; // Withdrawal not found
    }

    /**
     * Reject a withdrawal request and persist the change.
     *
     * @param withdrawalToReject the Withdrawal to reject
     * @return true on success
     */
    public boolean rejectWithdrawal(Withdrawal withdrawalToReject) {
        if (withdrawalToReject != null) {
            withdrawalToReject.setStatus("Rejected");
            return rewriteWithdrawalCSV(withdrawalPath, withdrawals); // Write changes to file
        }
        return false; // Withdrawal not found
    }

    /**
     * Generate a human-readable report string summarizing system-wide and per-internship metrics.
     *
     * @return formatted report String
     */
    public String generateReportString() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("   Internship Placement System Report   \n");
        sb.append("========================================\n");

        // --- 1. System-Wide Summary ---
        long totalApprovedCompanies = companyReps.values().stream()
                .filter(r -> "Approved".equalsIgnoreCase(r.getStatus()))
                .count();

        long totalInternships = internships.size();

        // Group internships by status
        Map<String, Long> internshipsByStatus = internships.values().stream()
                .collect(Collectors.groupingBy(
                        i -> i.getStatus() != null ? i.getStatus() : "Unknown",
                        Collectors.counting()
                ));

        // Get total application and withdrawal counts
        long totalApplications = applications.values().stream()
                .mapToLong(List::size)
                .sum();

        long totalWithdrawals = withdrawals.values().stream()
                .mapToLong(List::size)
                .sum();

        sb.append("\n--- System-Wide Summary ---\n");
        sb.append(String.format("Total Approved Companies: %d\n", totalApprovedCompanies));
        sb.append(String.format("Total Internships:        %d\n", totalInternships));
        internshipsByStatus.forEach((status, count) ->
                sb.append(String.format("  - %s: %d\n", status, count))
        );
        sb.append(String.format("Total Applications:       %d\n", totalApplications));
        sb.append(String.format("Total Withdrawal Requests: %d\n", totalWithdrawals));

        // --- 2. Per-Internship Breakdown ---
        sb.append("\n\n--- Per-Internship Breakdown ---\n");
        if (internships.isEmpty()) {
            sb.append("No internships found in the system.\n");
        }

        // Sort internships by title for a clean report
        List<Internship> sortedInternships = internships.values().stream()
                .sorted(Comparator.comparing(Internship::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        for (Internship internship : sortedInternships) {
            String id = internship.getUUID().toString();

            // Get app count for this internship
            List<Application> appsForThis = applications.getOrDefault(id, Collections.emptyList());
            int appCount = appsForThis.size();

            // Get withdrawal count for this internship
            List<Withdrawal> withdrawalsForThis = withdrawals.getOrDefault(id, Collections.emptyList());
            int withdrawalCount = withdrawalsForThis.size();

            // Calculate percentage of all applications
            double percentage = (totalApplications == 0) ? 0.0 : ((double) appCount / totalApplications) * 100.0;

            sb.append("\n----------------------------------------\n");
            sb.append(String.format("Internship: %s\n", internship.getTitle()));
            sb.append(String.format("Company:    %s\n", internship.getCompanyName()));
            sb.append(String.format("Status:     %s\n", internship.getStatus()));
            sb.append(String.format("  - Applications Received: %d\n", appCount));
            sb.append(String.format("  - Withdrawal Requests:   %d\n", withdrawalCount));
            sb.append(String.format("  - %% of Total System Apps: %.1f%%\n", percentage));
        }

        return sb.toString();
    }

    /**
     * View all internships applying optional filters.
     *
     * @param statusFilters  list of statuses to include (null/empty = include all)
     * @param levelFilters   list of levels to include (null/empty = include all)
     * @param companyFilters list of companies to include (null/empty = include all)
     * @param majorFilters   list of preferred majors to include (null/empty = include all)
     * @return filtered and sorted list of internships
     */
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
