package boundary;

import control.StudentController;
import entity.Application;
import entity.Internship;
import entity.Student;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command-line interface for students.
 * <p>
 * Students can view and apply for internships, manage their applications,
 * set listing filters, and request withdrawals.
 * </p>
 */
public class StudentInterface implements CommandLineInterface {
    private final Scanner scanner = new Scanner(System.in);
    private final StudentController studentController = new StudentController();
    private final Student student;

    private final List<String> levelFilters = new ArrayList<>();
    private final List<String> companyFilters = new ArrayList<>();

    /**
     * Construct a student interface bound to the given student.
     *
     * @param student authenticated student
     */
    public StudentInterface(Student student) {
        this.student = student;
    }

    /**
     * Displays the student menu, shows any notifications, and processes user choices until logout.
     */
    @Override
    public void display() {
        boolean running = true;
        // Check for notifications, if there are updates on withdrawal request or application status, should return a list of strings to display
        List<String> notifications = studentController.checkNotifications(student);

        if (!notifications.isEmpty()) {
            System.out.println("\n---You have " + notifications.size() + " notification(s)");
            int notificationCount = 1; // Initialize counter
            for (String notification : notifications) {
                System.out.println(notificationCount + ". " + notification); // Print with number and dot
                notificationCount++; // Increment counter
            }
            System.out.println("---------------------\n");

            // Student must acknowledge before proceeding
            System.out.print("Press Enter to acknowledge");
            scanner.nextLine();
        }

        while (running) {
            System.out.println("\n==========================================");
            System.out.println("Student Menu - Welcome, " + student.getName());
            System.out.println("==========================================");
            System.out.println("1. View and Apply for Internships");
            System.out.println("2. Manage My Applications");
            System.out.println("3. Set Filters"); // <-- REVISED
            System.out.println("4. Logout"); // <-- REVISED
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleViewAndApplyInternships();
                    break;
                case "2":
                    handleManageApplications();
                    break;
                case "3":
                    handleSetFilters();
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        // Loop has ended
        System.out.println("Logging out... Returning to main menu.");
    }

    /**
     * Lists internships available to the student (respecting filters) and permits applying.
     */
    private void handleViewAndApplyInternships() {
        System.out.println("\n--- View Available Internships ---");

        System.out.println("Active Filters:");
        System.out.println("  Level: " + (levelFilters.isEmpty() ? "[Any]" : levelFilters));
        System.out.println("  Company: " + (companyFilters.isEmpty() ? "[Any]" : companyFilters));
        System.out.println("---------------------------------");

        // Get filtered list of internships
        List<Internship> available = studentController.getAvailableInternships(student, levelFilters, companyFilters);

        if (available.isEmpty()) {
            System.out.println("No internships are currently available that match your profile or active filters.");
            return;
        }

        // Display them in a numbered list
        System.out.println("Internships matching your profile:");
        for (int i = 0; i < available.size(); i++) {
            Internship internship = available.get(i);
            System.out.printf("%d. %s @ %s\n", (i + 1), internship.getTitle(), internship.getCompanyName());
            System.out.printf("   Level: %s | Major: %s | Slots: %d\n",
                    internship.getLevel(), internship.getPreferredMajor(), internship.getNumberOfSlots());
        }
        System.out.println("0. Go Back");

        // Prompt user to apply
        System.out.print("\nSelect an internship to apply for (or 0 to go back): ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        if (choice == 0) {
            return;
        }

        if (choice > 0 && choice <= available.size()) {
            Internship selectedInternship = available.get(choice - 1);

            // Check application eligibility
            if (!studentController.canApply(student)) {
                System.out.println("Application failed: You have already reached the maximum of 3 applications or accepted an offer.");
                return;
            }

            // Call controller to submit application
            boolean success = studentController.applyForInternship(student, selectedInternship);
            if (success) {
                System.out.println("Application submitted successfully!");
            } else {
                System.out.println("Failed to submit application. Please try again.");
            }
        } else {
            System.out.println("Invalid choice.");
        }
    }

    /**
     * Generic helper to manage a named filter list (add/remove/clear).
     *
     * @param filterName display name of the category
     * @param filterList list storing current filter values
     */
    private void manageFilterList(String filterName, List<String> filterList) {
        while (true) {
            System.out.println("\n--- Managing '" + filterName + "' Filters ---");
            if (filterList.isEmpty()) {
                System.out.println("Current filters: [None]");
            } else {
                System.out.println("Current filters: " + filterList);
            }
            System.out.println("1. Add a filter value");
            System.out.println("2. Remove a filter value");
            System.out.println("3. Clear all filters for this category");
            System.out.println("0. Done with this category");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter value to add: ");
                    String valueToAdd = scanner.nextLine();
                    if (valueToAdd != null && !valueToAdd.isEmpty()) {
                        filterList.add(valueToAdd);
                        System.out.println("'" + valueToAdd + "' added.");
                    }
                    break;
                case "2":
                    if (filterList.isEmpty()) {
                        System.out.println("No filters to remove.");
                        break;
                    }
                    System.out.println("Select value to remove:");
                    for (int i = 0; i < filterList.size(); i++) {
                        System.out.println((i + 1) + ". " + filterList.get(i));
                    }
                    System.out.println("0. Cancel");
                    System.out.print("Enter number: ");
                    try {
                        int index = Integer.parseInt(scanner.nextLine());
                        if (index > 0 && index <= filterList.size()) {
                            String removed = filterList.remove(index - 1);
                            System.out.println("'" + removed + "' removed.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input.");
                    }
                    break;
                case "3":
                    filterList.clear();
                    System.out.println("All filters for '" + filterName + "' cleared.");
                    break;
                case "0":
                    return; // Exit this helper
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Guides the interactive setting of student-relevant filters.
     */
    private void handleSetFilters() {
        System.out.println("\n--- Set Internship Filters ---");
        // Only manage filters relevant to the student
        manageFilterList("Level", levelFilters);
        manageFilterList("Company", companyFilters);
        System.out.println("\nAll filters updated successfully.");
    }

    /**
     * Shows all applications for the student, grouped by status, and provides actions to accept offers or request withdrawals.
     */
    private void handleManageApplications() {
        System.out.println("\n--- Manage My Applications ---");

        Map<Application, Internship> myAppsMap = studentController.getMyApplications(student);
        if (myAppsMap.isEmpty()) {
            System.out.println("You have not submitted any applications.");
            return;
        }

        // Get the set of pending withdrawal requests
        Set<String> pendingWithdrawals = studentController.getPendingWithdrawalRequests(student);

        // Sort applications into lists
        // Find all the application lists for display
        Optional<Map.Entry<Application, Internship>> acceptedOffer = myAppsMap.entrySet().stream()
                .filter(e -> "Accepted".equalsIgnoreCase(e.getKey().getStatus()))
                .findFirst();

        List<Map.Entry<Application, Internship>> successfulOffers = myAppsMap.entrySet().stream()
                .filter(e -> "Successful".equalsIgnoreCase(e.getKey().getStatus()))
                .sorted(Comparator.comparing(e -> e.getValue().getTitle()))
                .collect(Collectors.toList());

        List<Map.Entry<Application, Internship>> pendingApps = myAppsMap.entrySet().stream()
                .filter(e -> "Pending".equalsIgnoreCase(e.getKey().getStatus()))
                .sorted(Comparator.comparing(e -> e.getValue().getTitle()))
                .collect(Collectors.toList());

        // Display all applications, checking against the pending withdrawal set
        System.out.println("Your applications:");

        if (acceptedOffer.isPresent()) {
            Map.Entry<Application, Internship> entry = acceptedOffer.get();
            String status = getStatusWithWithdrawal(entry.getKey(), pendingWithdrawals); // Show withdrawal status
            System.out.println("\n[ Your Accepted Internship ]");
            System.out.printf("  * %s @ %s (Status: %s)\n",
                    entry.getValue().getTitle(),
                    entry.getValue().getCompanyName(),
                    status);
        }

        if (!successfulOffers.isEmpty()) {
            System.out.println("\n[ Successful Offers (Can Accept) ]");
            int i = 1;
            for (Map.Entry<Application, Internship> entry : successfulOffers) {
                String status = getStatusWithWithdrawal(entry.getKey(), pendingWithdrawals);
                System.out.printf("  %d. %s @ %s (Status: %s)\n",
                        i++, entry.getValue().getTitle(), entry.getValue().getCompanyName(), status);
            }
        }

        if (!pendingApps.isEmpty()) {
            System.out.println("\n[ Pending Applications ]");
            int i = 1;
            for (Map.Entry<Application, Internship> entry : pendingApps) {
                String status = getStatusWithWithdrawal(entry.getKey(), pendingWithdrawals);
                System.out.printf("  %d. %s @ %s (Status: %s)\n",
                        i++, entry.getValue().getTitle(), entry.getValue().getCompanyName(), status);
            }
        }

        // Show action menu
        System.out.println("\n----------------------------------");
        System.out.println("Actions:");
        System.out.println("1. Accept a 'Successful' Offer");
        System.out.println("2. Request Withdrawal from an Application (Pending or Successful)");
        System.out.println("0. Go Back");
        System.out.print("Enter your choice: ");
        String choice = scanner.nextLine();

        List<Map.Entry<Application, Internship>> actionableApps = myAppsMap.entrySet().stream()
                .filter(e -> {
                    String status = e.getKey().getStatus();
                    return "Pending".equalsIgnoreCase(status) ||
                            "Successful".equalsIgnoreCase(status) ||
                            "Accepted".equalsIgnoreCase(status); // <-- Added "Accepted"
                })
                .sorted(Comparator.comparing(e -> e.getKey().getStatus()))
                .collect(Collectors.toList());

        switch (choice) {
            case "1":
                handleAcceptOffer(successfulOffers);
                break;
            case "2":
                handleRequestWithdrawal(actionableApps, pendingWithdrawals); // Pass set
                break;
            case "0":
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    /**
     * Helper that returns the application status annotated if a withdrawal is pending.
     *
     * @param app                application to check
     * @param pendingWithdrawals set of application UUIDs which have pending withdrawals
     * @return display status (e.g. "Pending", "Successful (Withdrawal Requested)")
     */
    private String getStatusWithWithdrawal(Application app, Set<String> pendingWithdrawals) {
        String status = app.getStatus();
        if (pendingWithdrawals.contains(app.getUUID().toString())) {
            return status + " (Withdrawal Requested)";
        }
        return status;
    }

    /**
     * Guides the user through accepting a 'Successful' offer.
     *
     * @param successfulOffers list of successful offer entries (Application + Internship)
     */
    private void handleAcceptOffer(List<Map.Entry<Application, Internship>> successfulOffers) {
        if (successfulOffers.isEmpty()) {
            System.out.println("You have no successful offers to accept.");
            return;
        }

        System.out.println("\n--- Accept an Offer ---");
        System.out.println("Select the offer to accept:");
        for (int i = 0; i < successfulOffers.size(); i++) {
            Map.Entry<Application, Internship> entry = successfulOffers.get(i);
            System.out.printf("%d. %s @ %s\n", (i + 1), entry.getValue().getTitle(), entry.getValue().getCompanyName());
        }
        System.out.println("0. Cancel");
        System.out.print("Enter choice: ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice == 0) return;

        if (choice > 0 && choice <= successfulOffers.size()) {
            Application appToAccept = successfulOffers.get(choice - 1).getKey();
            boolean success = studentController.acceptOffer(student, appToAccept);
            if (success) {
                System.out.println("Offer accepted successfully!");
                System.out.println("All other pending applications have been automatically withdrawn.");
            } else {
                System.out.println("Failed to accept offer. You may have already accepted another internship.");
            }
        } else {
            System.out.println("Invalid choice.");
        }
    }

    /**
     * Guides the user through submitting a withdrawal request for an eligible application.
     *
     * @param actionableApps   list of candidate applications (Application + Internship)
     * @param pendingWithdrawals set of UUID strings representing withdrawals already pending
     */
    private void handleRequestWithdrawal(List<Map.Entry<Application, Internship>> actionableApps, Set<String> pendingWithdrawals) {
        if (actionableApps.isEmpty()) {
            System.out.println("You have no applications eligible for withdrawal.");
            return;
        }

        System.out.println("\n--- Request Withdrawal ---");
        System.out.println("Select the application to withdraw from:");
        for (int i = 0; i < actionableApps.size(); i++) {
            Map.Entry<Application, Internship> entry = actionableApps.get(i);
            String status = getStatusWithWithdrawal(entry.getKey(), pendingWithdrawals); // Show full status
            System.out.printf("%d. %s @ %s (Status: %s)\n",
                    (i + 1), entry.getValue().getTitle(), entry.getValue().getCompanyName(), status);
        }
        System.out.println("0. Cancel");
        System.out.print("Enter choice: ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice == 0) return;

        if (choice > 0 && choice <= actionableApps.size()) {
            Application appToWithdraw = actionableApps.get(choice - 1).getKey();

            if (pendingWithdrawals.contains(appToWithdraw.getUUID().toString())) {
                System.out.println("You have already submitted a withdrawal request for this application.");
                return;
            }

            if ("Successful".equalsIgnoreCase(appToWithdraw.getStatus())) {
                System.out.println("You are about to withdraw a 'Successful' offer.");
                System.out.print("Are you sure? (y/n): ");
                String confirm = scanner.nextLine();
                if (!"y".equalsIgnoreCase(confirm)) {
                    System.out.println("Withdrawal cancelled.");
                    return;
                }
            }

            if ("Accepted".equalsIgnoreCase(appToWithdraw.getStatus())) {
                System.out.println("You are about to withdraw a 'Accepted' offer.");
                System.out.print("Are you sure? (y/n): ");
                String confirm = scanner.nextLine();
                if (!"y".equalsIgnoreCase(confirm)) {
                    System.out.println("Withdrawal cancelled.");
                    return;
                }
            }

            boolean success = studentController.requestWithdrawal(appToWithdraw);
            if (success) {
                System.out.println("Withdrawal request submitted.");
                System.out.println("Your request is now pending approval from Career Center Staff.");
            } else {
                System.out.println("Failed to submit withdrawal request.");
            }
        } else {
            System.out.println("Invalid choice.");
        }
    }
}
