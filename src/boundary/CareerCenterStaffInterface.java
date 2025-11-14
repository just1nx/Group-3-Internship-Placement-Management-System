package boundary;

import control.CareerCenterStaffController;
import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.Internship;

import java.util.List;
import java.util.Scanner;

public class CareerCenterStaffInterface implements CommandLineInterface {
    private final Scanner scanner = new Scanner(System.in);
    private final CareerCenterStaffController staffController = new CareerCenterStaffController();
    private final CareerCenterStaff staff;

    public CareerCenterStaffInterface(CareerCenterStaff staff) {
        this.staff = staff;
    }

    @Override
    public void display() {
        boolean running = true;
        while (running) {
            System.out.println("\n==========================================");
            System.out.println("Career Center Staff Menu - Welcome, " + staff.getName());
            System.out.println("==========================================");
            System.out.println("1. Approve/Reject Company Representative Accounts");
            System.out.println("2. Approve/Reject Internship Opportunities");
            System.out.println("3. Approve/Reject Student Withdrawal Requests");
            System.out.println("4. Generate Internship Reports");
            System.out.println("5. View All Internships (with filters)");
            System.out.println("6. Logout");
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
                    handleViewAllInternships();
                    break;
                case "6":
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
                success = staffController.approveRegistration(repToManage.getUserID());
                if (success) {
                    System.out.println("Registration approved.");
                } else {
                    System.out.println("Failed to approve registration.");
                }
                break;
            case "2":
                success = staffController.rejectRegistration(repToManage.getUserID());
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
        String internshipId = internshipToManage.getUUID().toString();

        // 5. Call controller method
        switch (action) {
            case "1":
                success = staffController.approveInternship(internshipId);
                if (success) {
                    System.out.println("Internship approved.");
                } else {
                    System.out.println("Failed to approve internship.");
                }
                break;
            case "2":
                success = staffController.rejectInternship(internshipId);
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

    /**
     * Placeholder method to handle approving/rejecting student withdrawal requests.
     * This will call your CareerCenterStaffController.
     */
    private void handleWithdrawalRequests() {
        System.out.println("\n--- Approve/Reject Student Withdrawal Requests ---");
        // 1. Call staffController.getPendingWithdrawals()
        // 2. Display a list of pending withdrawal requests.
        // 3. Prompt staff to select a request.
        // 4. Ask to "Approve" (1) or "Reject" (2).
        // 5. Call staffController.approveWithdrawal(requestId) or staffController.rejectWithdrawal(requestId)
        System.out.println("... (To be implemented: Show list of pending withdrawals) ...");
    }

    /**
     * Placeholder method to generate comprehensive reports.
     * This will call your CareerCenterStaffController.
     */
    private void handleGenerateReports() {
        System.out.println("\n--- Generate Internship Reports ---");
        // 1. Prompt staff for filters (by Status, Major, Company, etc.).
        // 2. Call staffController.generateReport(filters)
        // 3. Display the generated report (e.g., list of internships or summary statistics).
        System.out.println("... (To be implemented: Report generation with filters) ...");
    }

    /**
     * Placeholder method to view and filter all internships in the system.
     * This will call your CareerCenterStaffController.
     */
    private void handleViewAllInternships() {
        System.out.println("\n--- View All Internships ---");
        // 1. Prompt staff for filters (Status, Major, Level, Closing Date, etc.).
        // 2. Call staffController.viewAllInternships(filters)
        // 3. Display the list of internships.
        // 4. (PDF: "User filter settings are saved when they switch menu pages."
        //    This implies the 'filters' object should be stored in this class)
        System.out.println("... (To be implemented: Show filterable list of all internships) ...");
    }
}