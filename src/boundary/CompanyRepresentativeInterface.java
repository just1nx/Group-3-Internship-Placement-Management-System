package boundary;

import control.CompanyRepresentativeController;
import entity.CompanyRepresentative;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class CompanyRepresentativeInterface implements CommandLineInterface {
    private final Scanner scanner = new Scanner(System.in);
    private final CompanyRepresentativeController companyRepController = new CompanyRepresentativeController();
    private final CompanyRepresentative companyRep;

    public CompanyRepresentativeInterface(CompanyRepresentative companyRep) {
        this.companyRep = companyRep;
    }

    @Override
    public void display() {
        boolean running = true;
        while (running) {
            System.out.println("\n==========================================");
            System.out.println("Company Representative Menu - Welcome, " + companyRep.getName() + ", " + companyRep.getCompanyName());
            System.out.println("==========================================");
            System.out.println("1. Create Internship Opportunity");
            System.out.println("2. View My Internship Opportunities");
            System.out.println("3. Manage Applications for an Internship");
            System.out.println("4. Toggle Internship Visibility");
            System.out.println("5. Logout");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleCreateInternship();
                    break;
                case "2":
                    handleViewMyInternships();
                    break;
                case "3":
                    handleManageApplications();
                    break;
                case "4":
                    handleToggleVisibility();
                    break;
                case "5":
                    running = false; // Exits the while loop
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        // Loop has ended
        System.out.println("Logging out... Returning to main menu.");
    }

    private void handleCreateInternship() {
        System.out.println("\n--- Create New Internship Opportunity ---");

        // Step 1: Check if rep can create more internships (max 5)
        boolean canCreate = companyRepController.canCreateMoreInternships(companyRep.getCompanyName());
        if (!canCreate) {
            System.out.println("Error: You have already created the maximum (5) of internship opportunities.");
            return;
        }

        // Step 2: Collect details from user
        System.out.print("Enter Internship Title: ");
        String title = scanner.nextLine();

        System.out.print("Enter Internship Description: ");
        String description = scanner.nextLine();

        String level = promptForLevel(); // Helper method for validation
        if (level == null) return; // User cancelled

        System.out.print("Enter Preferred Major (e.g., Computer Science): ");
        String preferredMajor = scanner.nextLine();

        String openingDate = promptForDate("Enter Application Opening Date (YYYY-MM-DD): ");
        if (openingDate == null) return; // User cancelled

        String closingDate = promptForDate("Enter Application Closing Date (YYYY-MM-DD): ");
        if (closingDate == null) return; // User cancelled

        int slots = promptForSlots(); // Helper method for validation
        if (slots == -1) return; // User cancelled

        // Step 3: Get automatic details
        String companyName = companyRep.getCompanyName();
        String representativeID = companyRep.getUserID();

        // Step 4: Call controller to create the internship
         boolean success = companyRepController.createInternship(
             title, description, level, preferredMajor,
             openingDate, closingDate, companyName, representativeID, Integer.toString(slots)
         );

         if (success) {
             System.out.println("Internship opportunity created successfully!");
             System.out.println("It is now pending approval from Career Center Staff.");
         } else {
             System.out.println("Failed to create internship. Please try again.");
         }
    }

    /**
     * Helper method to prompt for and validate the internship level.
     *
     * @return A valid level ("Basic", "Intermediate", "Advanced") or null if cancelled.
     */
    private String promptForLevel() {
        while (true) {
            System.out.print("Enter Internship Level (1: Basic, 2: Intermediate, 3: Advanced, 0: Cancel): ");
            String levelChoice = scanner.nextLine();
            switch (levelChoice) {
                case "1":
                    return "Basic";
                case "2":
                    return "Intermediate";
                case "3":
                    return "Advanced";
                case "0":
                    System.out.println("Cancelled creation.");
                    return null;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, 3, or 0.");
            }
        }
    }

    /**
     * Helper method to prompt for and validate the application date.
     *
     * @param prompt The message to display to the user.
     * @return A valid date string in YYYY-MM-DD format or null if cancelled.
     */
    private String promptForDate(String prompt) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.print(prompt + " (or 0 to Cancel): ");
            String dateInput = scanner.nextLine();

            if (dateInput.equals("0")) {
                System.out.println("Cancelled creation.");
                return null;
            }

            try {
                // Try parsing the date to ensure it's valid
                LocalDate.parse(dateInput, dtf);
                return dateInput; // Return the valid date string
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    /**
     * Helper method to prompt for and validate the number of slots (1-10).
     *
     * @return A valid number of slots (1-10) or -1 if cancelled.
     */
    private int promptForSlots() {
        while (true) {
            System.out.print("Enter Number of Slots (1-10, or 0 to Cancel): ");
            String slotInput = scanner.nextLine();

            if (slotInput.equals("0")) {
                System.out.println("Cancelled creation.");
                return -1;
            }

            try {
                int slots = Integer.parseInt(slotInput);
                if (slots >= 1 && slots <= 10) {
                    return slots;
                } else {
                    System.out.println("Invalid input. Number of slots must be between 1 and 10.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Placeholder method to view all internships created by this representative.
     * This will call your CompanyRepresentativeController.
     */
    private void handleViewMyInternships() {
        System.out.println("\n--- View My Internship Opportunities ---");
        // 1. Call controller.getInternshipsByRep(companyRep.getUserID())
        // 2. Display the list of internships with their details and status ("Pending", "Approved", "Rejected", "Filled")
        System.out.println("... (To be implemented: Show list of created internships) ...");
    }

    /**
     * Placeholder method to view and manage applications for a specific internship.
     * This will call your CompanyRepresentativeController.
     */
    private void handleManageApplications() {
        System.out.println("\n--- Manage Applications ---");
        // 1. Call handleViewMyInternships() to show a list of their approved internships.
        // 2. Ask rep to select an internship to manage.
        // 3. Call controller.getApplicationsForInternship(internshipId)
        // 4. Display list of students who applied (Name, Major, Year).
        // 5. Prompt rep to "Approve" or "Reject" an application.
        // 6. Call controller.approveApplication(applicationId) or controller.rejectApplication(applicationId)
        System.out.println("... (To be implemented: Show applications for a chosen internship) ...");
    }

    /**
     * Placeholder method to toggle the visibility of an internship.
     * This will call your CompanyRepresentativeController.
     */
    private void handleToggleVisibility() {
        System.out.println("\n--- Toggle Internship Visibility ---");
        // 1. Call handleViewMyInternships() to show a list of their approved internships.
        // 2. Ask rep to select an internship.
        // 3. Show its current visibility ("On" or "Off").
        // 4. Ask to toggle.
        // 5. Call controller.toggleVisibility(internshipId)
        System.out.println("... (To be implemented: Select internship and toggle on/off) ...");
    }
}
