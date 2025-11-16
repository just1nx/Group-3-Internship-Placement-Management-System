package boundary;

import control.CareerCenterStaffController;
import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.Internship;
import entity.Withdrawal;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CareerCenterStaffInterface implements CommandLineInterface {
    private final Scanner scanner = new Scanner(System.in);
    private final CareerCenterStaffController staffController = new CareerCenterStaffController();
    private final CareerCenterStaff staff;

    // Fields to store filter settings
    private final List<String> statusFilters = new ArrayList<>();
    private final List<String> levelFilters = new ArrayList<>();
    private final List<String> companyFilters = new ArrayList<>();
    private final List<String> majorFilters = new ArrayList<>();

    public CareerCenterStaffInterface(CareerCenterStaff staff) {
        this.staff = staff;
    }

    @Override
    public void display() {
        boolean running = true;
        while (running) {
            System.out.println("\n====================================================");
            System.out.println("Career Center Staff Menu - Welcome, " + staff.getName());
            System.out.println("====================================================");
            System.out.println("1. Approve/Reject Company Representative Accounts");
            System.out.println("2. Approve/Reject Internship Opportunities");
            System.out.println("3. Approve/Reject Student Withdrawal Requests");
            System.out.println("4. Generate Internship Reports");
            System.out.println("5. Set Internship Filters");
            System.out.println("6. View All Internships (with filters)");
            System.out.println("7. Logout");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleApproveRegistrations();
                    break;
                case "2":
                    handleApproveInternships();
                    break;
                case "3":
                    handleWithdrawalRequests();
                    break;
                case "4":
                    handleGenerateReports();
                    break;
                case "5":
                    handleSetFilters(); // New method
                    break;
                case "6":
                    handleViewAllInternships(); // Updated method
                    break;
                case "7":
                    running = false; // Exits the while loop
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        // Loop has ended
        System.out.println("Logging out... Returning to main menu.");
    }

    private void handleApproveRegistrations() {
        System.out.println("\n--- Approve/Reject Company Registrations ---");

        // 1. Get pending registrations from controller
        List<CompanyRepresentative> pendingReps = staffController.getPendingRegistrations();

        if (pendingReps.isEmpty()) {
            System.out.println("There are no pending company representative registrations.");
            return;
        }

        // 2. Display a numbered list
        System.out.println("Pending Registrations:");
        for (int i = 0; i < pendingReps.size(); i++) {
            CompanyRepresentative rep = pendingReps.get(i);
            System.out.printf("%d. %s (%s) - %s%n",
                    i + 1,
                    rep.getName(),
                    rep.getUserID(), // This is their email
                    rep.getCompanyName());
        }
        System.out.println("0. Cancel");

        // 3. Prompt staff to select one
        CompanyRepresentative repToManage = null;
        while (true) {
            System.out.print("Select a registration to manage (or 0 to cancel): ");
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 0) {
                    return; // User cancelled
                }
                if (choice > 0 && choice <= pendingReps.size()) {
                    repToManage = pendingReps.get(choice - 1);
                    break; // Valid selection
                } else {
                    System.out.println("Invalid number. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        // 4. Ask to Approve or Reject
        System.out.printf("Action for %s (%s):%n", repToManage.getName(), repToManage.getCompanyName());
        System.out.println("1. Approve");
        System.out.println("2. Reject");
        System.out.println("0. Cancel");
        System.out.print("Enter your choice: ");

        String action = scanner.nextLine();
        boolean success = false;

        // 5. Call controller method
        switch (action) {
            case "1":
                success = staffController.approveRegistration(repToManage);
                if (success) {
                    System.out.println("Registration approved.");
                } else {
                    System.out.println("Failed to approve registration.");
                }
                break;
            case "2":
                success = staffController.rejectRegistration(repToManage);
                if (success) {
                    System.out.println("Registration rejected.");
                } else {
                    System.out.println("Failed to reject registration.");
                }
                break;
            case "0":
                System.out.println("Action cancelled.");
                break;
            default:
                System.out.println("Invalid choice. Action cancelled.");
        }
    }

    private void handleApproveInternships() {
        System.out.println("\n--- Approve/Reject Internship Opportunities ---");

        // 1. Get pending internships from controller
        List<Internship> pendingInternships = staffController.getPendingInternships();

        if (pendingInternships.isEmpty()) {
            System.out.println("There are no pending internship opportunities.");
            return;
        }

        // 2. Display a numbered list
        System.out.println("Pending Internships:");
        for (int i = 0; i < pendingInternships.size(); i++) {
            Internship internship = pendingInternships.get(i);
            System.out.printf("%d. %s - %s (Rep: %s)%n",
                    i + 1,
                    internship.getTitle(),
                    internship.getCompanyName(),
                    internship.getRepresentatives()); // This is the rep ID
        }
        System.out.println("0. Cancel");

        // 3. Prompt staff to select one
        Internship internshipToManage = null;
        while (true) {
            System.out.print("Select an internship to manage (or 0 to cancel): ");
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 0) {
                    return; // User cancelled
                }
                if (choice > 0 && choice <= pendingInternships.size()) {
                    internshipToManage = pendingInternships.get(choice - 1);
                    break; // Valid selection
                } else {
                    System.out.println("Invalid number. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        // 4. Ask to Approve or Reject
        System.out.printf("Action for %s (%s):%n", internshipToManage.getTitle(), internshipToManage.getCompanyName());
        System.out.println("1. Approve");
        System.out.println("2. Reject");
        System.out.println("0. Cancel");
        System.out.print("Enter your choice: ");

        String action = scanner.nextLine();
        boolean success = false;

        // 5. Call controller method
        switch (action) {
            case "1":
                success = staffController.approveInternship(internshipToManage);
                if (success) {
                    System.out.println("Internship approved.");
                } else {
                    System.out.println("Failed to approve internship.");
                }
                break;
            case "2":
                success = staffController.rejectInternship(internshipToManage);
                if (success) {
                    System.out.println("Internship rejected.");
                } else {
                    System.out.println("Failed to reject internship.");
                }
                break;
            case "0":
                System.out.println("Action cancelled.");
                break;
            default:
                System.out.println("Invalid choice. Action cancelled.");
        }
    }

    private void handleWithdrawalRequests() {
        System.out.println("\n--- Approve/Reject Student Withdrawal Requests ---");

        // 1. Get pending withdrawals from controller
        List<Withdrawal> pendingWithdrawals = staffController.getPendingWithdrawals();

        if (pendingWithdrawals.isEmpty()) {
            System.out.println("There are no pending withdrawal requests.");
            return;
        }

        // 2. Display a numbered list
        System.out.println("Pending Withdrawal Requests:");
        for (int i = 0; i < pendingWithdrawals.size(); i++) {
            Withdrawal w = pendingWithdrawals.get(i);
            System.out.printf("%d. %s (Student ID: %s) - From Internship: %s%n",
                    i + 1,
                    w.getName(),
                    w.getUserId(),
                    w.getUUID() // This is the Internship UUID
            );
        }
        System.out.println("0. Cancel");

        // 3. Prompt staff to select one
        Withdrawal withdrawalToManage = null;
        while (true) {
            System.out.print("Select a request to manage (or 0 to cancel): ");
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 0) {
                    return; // User cancelled
                }
                if (choice > 0 && choice <= pendingWithdrawals.size()) {
                    withdrawalToManage = pendingWithdrawals.get(choice - 1);
                    break; // Valid selection
                } else {
                    System.out.println("Invalid number. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        // 4. Ask to Approve or Reject
        System.out.printf("Action for %s (Student ID: %s):%n", withdrawalToManage.getName(), withdrawalToManage.getUserId());
        System.out.println("1. Approve Withdrawal");
        System.out.println("2. Reject Withdrawal");
        System.out.println("0. Cancel");
        System.out.print("Enter your choice: ");

        String action = scanner.nextLine();
        boolean success = false;

        // 5. Call controller method
        switch (action) {
            case "1":
                success = staffController.approveWithdrawal(withdrawalToManage);
                if (success) {
                    System.out.println("Withdrawal request approved."); // <-- Updated message
                } else {
                    System.out.println("Failed to approve withdrawal.");
                }
                break;
            case "2":
                success = staffController.rejectWithdrawal(withdrawalToManage);
                if (success) {
                    System.out.println("Withdrawal request rejected."); // <-- Updated message
                } else {
                    System.out.println("Failed to reject withdrawal.");
                }
                break;
            case "0":
                System.out.println("Action cancelled.");
                break;
            default:
                System.out.println("Invalid choice. Action cancelled.");
        }
    }

    private void handleGenerateReports() {
        System.out.println("\n--- Generating Internship Reports ---");

        // Call controller to get the report string
        String report = staffController.generateReportString();

        // Display the generated report
        System.out.println(report);

        // Pause for user to read
        System.out.println("========================================");
        System.out.print("\nPress Enter to return to the menu...");
        scanner.nextLine();
    }

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

    private void handleSetFilters() {
        System.out.println("\n--- Set Internship Filters ---");

        // Manage each filter category one by one
        manageFilterList("Status", statusFilters);
        manageFilterList("Level", levelFilters);
        manageFilterList("Company", companyFilters);
        manageFilterList("Major", majorFilters);

        System.out.println("\nAll filters updated successfully.");
    }

    private void handleViewAllInternships() {
        System.out.println("\n--- View All Internships ---");

        // 1. Display the currently active filters
        System.out.println("Active Filters:");
        System.out.println("  Status: " + (statusFilters.isEmpty() ? "[Any]" : statusFilters));
        System.out.println("  Level: " + (levelFilters.isEmpty() ? "[Any]" : levelFilters));
        System.out.println("  Company: " + (companyFilters.isEmpty() ? "[Any]" : companyFilters));
        System.out.println("  Major: " + (majorFilters.isEmpty() ? "[Any]" : majorFilters));
        System.out.println("---------------------------------");

        // 2. Call the controller with the saved filter lists
        List<Internship> filteredInternships = staffController.viewAllInternships(
                statusFilters, levelFilters, companyFilters, majorFilters
        );

        // 3. Display the results
        if (filteredInternships.isEmpty()) {
            System.out.println("No internships found matching the current filters.");
            return;
        }

        System.out.println("Found " + filteredInternships.size() + " internship(s):");
        int index = 1;
        for (Internship internship : filteredInternships) {
            System.out.println("\n--- Internship #" + index++ + " ---");
            System.out.println("Title: " + internship.getTitle() + " @ " + internship.getCompanyName());
            System.out.println("Status: " + internship.getStatus() + " | Level: " + internship.getLevel());
            System.out.println("Major: " + internship.getPreferredMajor() + " | Slots: " + internship.getNumberOfSlots());
            System.out.println("Application Period: " + internship.getOpeningDate() + " -> " + internship.getClosingDate());
            String visibilityDisplay = internship.isVisible() ? "ON" : "OFF";
            System.out.println("Visibility: " + visibilityDisplay);
        }
        System.out.println("==============================================");
    }
}
