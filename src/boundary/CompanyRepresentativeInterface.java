package boundary;

import control.CompanyRepresentativeController;
import entity.Application;
import entity.CompanyRepresentative;
import entity.Internship;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Command-line interface for company representatives.
 * <p>
 * Representatives can create, edit, delete and view internships, manage applications,
 * toggle visibility and set filters for their listings.
 * </p>
 */
public class CompanyRepresentativeInterface implements CommandLineInterface {
    /**
     * Scanner for user input.
     */
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Controller that encapsulates company representative operations.
     */
    private final CompanyRepresentativeController companyRepController = new CompanyRepresentativeController();

    /**
     * The authenticated company representative using this interface.
     */
    private final CompanyRepresentative companyRep;

    /**
     * Filters applied when viewing internships: status values.
     */
    private final List<String> statusFilters = new ArrayList<>();

    /**
     * Filters applied when viewing internships: level values.
     */
    private final List<String> levelFilters = new ArrayList<>();

    /**
     * Filters applied when viewing internships: preferred major values.
     */
    private final List<String> majorFilters = new ArrayList<>();

    /**
     * Create an interface bound to a specific company representative.
     *
     * @param companyRep authenticated company representative
     */
    public CompanyRepresentativeInterface(CompanyRepresentative companyRep) {
        this.companyRep = companyRep;
    }

    /**
     * Displays the representative menu, notification banner (if any) and processes user actions until logout.
     * <p>
     * Shows notifications about rejected internships at startup. Notifications require acknowledgment
     * before proceeding to the main menu.
     * </p>
     */
    @Override
    public void display() {
        boolean running = true;
        List<String> notifications = companyRepController.checkNotifications(companyRep);

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
            System.out.println("\n=========================================================");
            System.out.println("Company Representative Menu - Welcome, " + companyRep.getName() + ", " + companyRep.getCompanyName());
            System.out.println("=========================================================");
            System.out.println("1. Create Internship Opportunity");
            System.out.println("2. Edit Internship Opportunity");
            System.out.println("3. Delete Internship Opportunity");
            System.out.println("4. View My Internship Opportunities");
            System.out.println("5. View My Applications by Internship");
            System.out.println("6. Manage Applications for an Internship");
            System.out.println("7. Set Internship Filters");
            System.out.println("8. Toggle Internship Visibility");
            System.out.println("9. Logout");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleCreateInternship();
                    break;
                case "2":
                    handleEditInternship();
                    break;
                case "3":
                    handleDeleteInternship();
                    break;
                case "4":
                    handleViewMyInternships();
                    break;
                case "5":
                    handleViewApplications();
                    break;
                case "6":
                    handleManageApplications();
                    break;
                case "7":
                    handleSetFilters();
                    break;
                case "8":
                    handleToggleVisibility();
                    break;
                case "9":
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
     * Helper that prompts for and validates internship level selection.
     *
     * @param current current value to keep when input is empty; null if creating new
     * @return chosen level string or null if the user cancelled
     */
    private String promptForLevel(String current) {
        while (true) {
            if (current == null) {
                System.out.print("Enter Internship Level (1: Basic, 2: Intermediate, 3: Advanced, 0: Cancel): ");
            } else {
                System.out.print("Level (1: Basic, 2: Intermediate, 3: Advanced) (current: " + current + " ): ");
            }
            String levelChoice = scanner.nextLine();
            if (levelChoice.isEmpty() && current != null)
                return current;
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
     * Prompts for a date string using pattern yyyy-MM-dd and validates the input.
     *
     * @param prompt  text to show prior to input
     * @param current current value to keep when input is empty; null if creating new
     * @return valid date string or null when cancelled
     */
    private String promptForDate(String prompt, String current) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            if (current == null) {
                System.out.print(prompt + "or 0 to cancel: ");
            } else {
                System.out.print(prompt);
            }
            String dateInput = scanner.nextLine();
            if (dateInput.isEmpty() && current != null) {
                return current;
            }

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
     * Prompts for number of slots (1-10) and validates input.
     *
     * @param current current value to keep when input is empty; null if creating new
     * @return chosen number of slots, or -1 if the user cancels
     */
    private int promptForSlots(String current) {
        while (true) {
            if (current == null) {
                System.out.print("Enter Number of Slots (1-10, or 0 to Cancel): ");
            } else {
                System.out.print("Number of Slots (1-10) (current: " + current + " ): ");
            }
            String slotInput = scanner.nextLine();

            if (slotInput.isEmpty() && current != null) {
                return Integer.parseInt(current);
            }

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
     * Prompts for a numeric internship selection and validates it against the provided maximum.
     *
     * @param maxSize maximum valid index
     * @return selected index (1..maxSize) or -1 when cancelled
     */
    private int promptForInternshipNumber(int maxSize) {
        while (true) {
            System.out.print("Enter the internship number (1-" + maxSize + ", or 0 to Cancel): ");
            String numberStr = scanner.nextLine();

            if (numberStr.equals("0")) {
                System.out.println("Cancelled Operation.");
                return -1;
            }

            try {
                int number = Integer.parseInt(numberStr);
                // Validate against the max size passed in
                if (number >= 1 && number <= maxSize) {
                    return number;
                } else {
                    System.out.println("Invalid input. Please enter a valid internship number (1-" + maxSize + ").");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Prompts for a student/application number to manage and validates input.
     *
     * @param max maximum valid number
     * @return selected number or -1 when cancelled
     */
    private int promptForStudentNumber(int max) {
        while (true) {
            System.out.print("Enter the application number to manage (1-" + max + ", or 0 to Cancel): ");
            String number = scanner.nextLine();

            if (number.equals("0")) {
                System.out.println("Cancelled Operation.");
                return -1;
            }

            try {
                int studentNum = Integer.parseInt(number);
                if (studentNum >= 1 && studentNum <= max) {
                    return studentNum;
                } else {
                    System.out.println("Invalid input. Please enter a valid number (1-" + max + ").");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Presents status options for applications and returns the chosen status string.
     *
     * @return "Successful", "Unsuccessful" or null if cancelled
     */
    private String promptForNewApplicationStatus() {
        while (true) {
            System.out.println("\nSet New Status:");
            System.out.println("1. Approved");
            System.out.println("2. Rejected");
            System.out.println("0. Cancel");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    return "Successful";
                case "2":
                    return "Unsuccessful";
                case "0":
                    System.out.println("Cancelled status change.");
                    return null;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 0.");
            }
        }
    }

    /**
     * Prompts for a visibility choice (on/off) and returns the numeric option or -1 if cancelled.
     *
     * @return 1 (on), 2 (off), or -1 if cancelled
     */
    private int promptForOption() {
        while (true) {
            System.out.print("Enter visibility preference (1: On, 2: Off, 0: Cancel): ");
            String option = scanner.nextLine();

            if (option.equals("0")) {
                System.out.println("Cancelled Operation.");
                return -1;
            }

            try {
                int option2 = Integer.parseInt(option);
                if (option2 == 1 || option2 == 2) {
                    return option2;
                } else {
                    System.out.println("Invalid input. Please enter 1 or 2.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Nicely prints a list of internships to the console for the representative.
     *
     * @param myInternships list of internships to display
     */
    private void displayInternshipList(List<Internship> myInternships) {
        if (myInternships.isEmpty()) {
            System.out.println("\n--- No internships found for " + companyRep.getCompanyName() + " ---");
            return;
        }

        System.out.println("\n--- My Internships ---");
        int index = 1;
        for (Internship internship : myInternships) {
            System.out.println("\n--- Internship #" + index++ + " ---");
            System.out.println("UUID: " + internship.getUUID());
            System.out.println("Title: " + internship.getTitle());
            System.out.println("Description: " + internship.getDescription());
            System.out.println("Preferred Major: " + internship.getPreferredMajor());
            System.out.println("Level: " + internship.getLevel());
            System.out.println("Slots: " + internship.getNumberOfSlots());
            System.out.println("Application Period: " + internship.getOpeningDate() + " -> " + internship.getClosingDate());
            System.out.println("Status: " + internship.getStatus());
            String visibilityDisplay = internship.isVisible() ? "ON" : "OFF";
            System.out.println("Visibility: " + visibilityDisplay);
            System.out.println("\n==============================================");
        }
    }

    /**
     * Guides the creation of a new internship opportunity.
     * <p>
     * Checks if the company has reached the maximum internship limit (5) before proceeding.
     * Collects all required details interactively and creates a pending internship.
     * </p>
     */
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

        String level = promptForLevel(null); // Helper method for validation
        if (level == null) return; // User cancelled

        System.out.print("Enter Preferred Major (e.g., Computer Science): ");
        String preferredMajor = scanner.nextLine();

        String openingDate = promptForDate("Enter Application Opening Date (YYYY-MM-DD) ", null);
        if (openingDate == null) return; // User cancelled

        String closingDate = promptForDate("Enter Application Closing Date (YYYY-MM-DD) ", null);
        if (closingDate == null) return; // User cancelled

        int slots = promptForSlots(null); // Helper method for validation
        if (slots == -1) return; // User cancelled

        // Step 3: Get automatic details
        String companyName = companyRep.getCompanyName();
        String representativeID = companyRep.getUserID();

        // Step 4: Call controller to create the internship
         boolean success = companyRepController.createInternship(
             title, description, level, preferredMajor,
             openingDate, closingDate, companyName, representativeID, slots
         );

         if (success) {
             System.out.println("Internship opportunity created successfully!");
             System.out.println("It is now pending approval from Career Center Staff.");
         } else {
             System.out.println("Failed to create internship. Please try again.");
         }
    }

    /**
     * Guides editing an existing internship. Only pending internships may be edited.
     * <p>
     * Displays internships matching current filters and allows the user to select one.
     * Only fields with non-empty input are updated; empty input keeps current values.
     * </p>
     */
    private void handleEditInternship() {
        System.out.println("\n--- Edit Internship Details ---");
        System.out.println("\nActive Filters:");
        System.out.println("  Status: " + (statusFilters.isEmpty() ? "[Any]" : statusFilters));
        System.out.println("  Level: " + (levelFilters.isEmpty() ? "[Any]" : levelFilters));
        System.out.println("  Major: " + (majorFilters.isEmpty() ? "[Any]" : majorFilters));
        System.out.println("---------------------------------");

        List<Internship> myInternships = companyRepController.viewMyInternships(companyRep.getCompanyName(), statusFilters, levelFilters, majorFilters);

        if (myInternships.isEmpty()) {
            System.out.println("You have no internships to edit.");
            return;
        }

        // Show internships
        displayInternshipList(myInternships);

        int number = promptForInternshipNumber(myInternships.size());
        if (number == -1) return;

        Internship selected = myInternships.get(number - 1);

        // Deny edits unless internship is still pending
        String status = selected.getStatus();
        if (status != null && !status.toLowerCase().contains("pending")) {
            System.err.println("Cannot edit internship unless it is pending.");
            return;
        }

        System.out.println("\nEnter new values or leave blank to keep current. Enter `0` to cancel.\n");

        System.out.print("Title (current: " + (selected.getTitle() == null ? "" : selected.getTitle()) + "): ");
        String title = scanner.nextLine();
        if (title.equals("0")) { System.out.println("Cancelled."); return; }
        if (title.isEmpty()) title = null;

        System.out.print("Description (current: " + (selected.getDescription() == null ? "" : selected.getDescription()) + "): ");
        String description = scanner.nextLine();
        if (description.equals("0")) { System.out.println("Cancelled."); return; }
        if (description.isEmpty()) description = null;

        String level = promptForLevel(selected.getLevel());
        if (level == null) return;

        System.out.print("Preferred Major (current: " + (selected.getPreferredMajor() == null ? "" : selected.getPreferredMajor()) + "): ");
        String preferredMajor = scanner.nextLine();
        if (preferredMajor.equals("0")) { System.out.println("Cancelled."); return; }
        if (preferredMajor.isEmpty()) preferredMajor = null;

        String openingDate = promptForDate("Opening Date (YYYY-MM-DD) (current: " + selected.getOpeningDate() +"): ", selected.getOpeningDate().toString());
        if (openingDate == null) { System.out.println("Cancelled."); return; }

        String closingDate = promptForDate("Closing Date (YYYY-MM-DD) (current: " + selected.getClosingDate() +"): ", selected.getClosingDate().toString());
        if (closingDate == null) { System.out.println("Cancelled."); return; }

        int slots = promptForSlots(String.valueOf(selected.getNumberOfSlots()));
        if (slots == -1) { System.out.println("Cancelled."); return; }

        boolean success = companyRepController.editInternship(
                selected.getUUID().toString(),
                title,
                description,
                level,
                preferredMajor,
                openingDate,
                closingDate,
                slots
        );

        if (success) {
            System.out.println("Internship updated successfully.");
            handleViewMyInternships();
        } else {
            System.err.println("Failed to update internship. Please try again.");
        }
    }

    /**
     * Deletes a selected internship after confirmation.
     * <p>
     * Only pending internships can be deleted. Displays filtered internships and prompts
     * for selection before deletion.
     * </p>
     */
    private void handleDeleteInternship() {
        System.out.println("\n--- Delete Internship Opportunity ---");
        System.out.println("\nActive Filters:");
        System.out.println("  Status: " + (statusFilters.isEmpty() ? "[Any]" : statusFilters));
        System.out.println("  Level: " + (levelFilters.isEmpty() ? "[Any]" : levelFilters));
        System.out.println("  Major: " + (majorFilters.isEmpty() ? "[Any]" : majorFilters));
        System.out.println("---------------------------------");

        List<Internship> myInternships = companyRepController.viewMyInternships(companyRep.getCompanyName(), statusFilters, levelFilters, majorFilters);

        if (myInternships.isEmpty()) {
            System.out.println("You have no internships to delete.");
            return;
        }

        // Show internships
        displayInternshipList(myInternships);

        int number = promptForInternshipNumber(myInternships.size());
        if (number == -1) return;

        Internship selected = myInternships.get(number - 1);

        boolean success = companyRepController.deleteInternship(selected.getUUID().toString());

        if (success) {
            handleViewMyInternships();
            System.out.println("Internship deleted successfully.");
        } else {
            System.err.println("Failed to delete internship. Please try again.");
        }
    }

    /**
     * Displays the representative's internships with the currently set filters.
     */
    private void handleViewMyInternships() {
        System.out.println("\nActive Filters:");
        System.out.println("  Status: " + (statusFilters.isEmpty() ? "[Any]" : statusFilters));
        System.out.println("  Level: " + (levelFilters.isEmpty() ? "[Any]" : levelFilters));
        System.out.println("  Major: " + (majorFilters.isEmpty() ? "[Any]" : majorFilters));
        System.out.println("---------------------------------");

        List<Internship> myInternships = companyRepController.viewMyInternships(companyRep.getCompanyName(), statusFilters, levelFilters, majorFilters);
        displayInternshipList(myInternships);
    }

    /**
     * Shows applications grouped by internship for the representative's company.
     * <p>
     * Displays all company internships matching current filters along with their applications.
     * Shows application details including student name, major, year, submission date and status.
     * </p>
     */
    private void handleViewApplications() {
        System.out.println("\n--- View Applications by Internship ---");
        System.out.println("==========================================");
        System.out.println("\nActive Filters:");
        System.out.println("  Status: " + (statusFilters.isEmpty() ? "[Any]" : statusFilters));
        System.out.println("  Level: " + (levelFilters.isEmpty() ? "[Any]" : levelFilters));
        System.out.println("  Major: " + (majorFilters.isEmpty() ? "[Any]" : majorFilters));
        System.out.println("---------------------------------");

        // Use the new controller method to get structured data: Map<Internship UUID, List<Application>>
        Map<String, List<Application>> applicationsByInternship =
                companyRepController.getInternshipsWithApplications(companyRep.getCompanyName());

        if (applicationsByInternship.isEmpty()) {
            System.out.println("No internships found for " + companyRep.getCompanyName() + ", or unable to read application data.");
            return;
        }

        // Get the list of the company's internship objects to iterate in order
        List<Internship> myInternships = companyRepController.viewMyInternships(companyRep.getCompanyName(), statusFilters, levelFilters, majorFilters);

        // 1. Iterate through each internship posted by the company
        int index = 1;
        for (Internship internship : myInternships) {
            String internshipUUID = internship.getUUID().toString();
            List<Application> applications = applicationsByInternship.getOrDefault(internshipUUID, List.of());

            // A. Print Internship Details
            System.out.println("\n-----------------------------------------");
            System.out.println("Internship #" + index++ + " - " + internship.getTitle());
            System.out.println("UUID: " + internship.getUUID());
            System.out.println("Status: " + internship.getStatus());
            System.out.println("-----------------------------------------");

            // B. Print Applications for This Internship
            if (applications.isEmpty()) {
                System.out.println("--> No applications received yet for this posting.");
            } else {
                System.out.println("APPLICATIONS (" + applications.size() + " Total):");
                int appIndex = 1;
                for (Application app : applications) {
                    System.out.printf("   %d. Name: %s | Major: %s | Year: %d | Submitted: %s | Status: %s%n",
                            appIndex++,
                            app.getName(),
                            app.getMajor(),
                            app.getYear(),
                            app.getSubmittedDate(),
                            app.getStatus()
                    );
                }
            }
        }

        System.out.println("\n==========================================");
    }

    /**
     * Allows the representative to manage (approve/reject) individual applications.
     * <p>
     * First prompts to select an internship, then displays its applications and allows
     * updating individual application statuses to "Successful" or "Unsuccessful".
     * </p>
     */
    private void handleManageApplications() {
        System.out.println("\n--- Manage Applications by Internship ---");
        System.out.println("\nActive Filters:");
        System.out.println("  Status: " + (statusFilters.isEmpty() ? "[Any]" : statusFilters));
        System.out.println("  Level: " + (levelFilters.isEmpty() ? "[Any]" : levelFilters));
        System.out.println("  Major: " + (majorFilters.isEmpty() ? "[Any]" : majorFilters));
        System.out.println("---------------------------------");

        List<Internship> myInternships = companyRepController.viewMyInternships(companyRep.getCompanyName(), statusFilters, levelFilters, majorFilters);

        if (myInternships.isEmpty()) {
            System.out.println("You have no internships posted to manage applications for.");
            return;
        }

        displayInternshipList(myInternships);

        int number = promptForInternshipNumber(myInternships.size());
        if (number == -1) return;

        Internship selectedInternship = myInternships.get(number - 1);
        String internshipUUID = selectedInternship.getUUID().toString();

        Map<String, List<Application>> applicationsByInternship =
                companyRepController.getInternshipsWithApplications(companyRep.getCompanyName());

        List<Application> applications = applicationsByInternship.getOrDefault(internshipUUID, List.of());

        System.out.println("\n--- Applications for: " + selectedInternship.getTitle() + " ---");
        if (applications.isEmpty()) {
            System.out.println("No applications received for this internship.");
            return;
        }

        System.out.println("APPLICATIONS (" + applications.size() + " Total):");
        int appIndex = 1;
        for (Application app : applications) {
            System.out.printf("   %d. Name: %s | Major: %s | Year: %d | Submitted: %s | Status: %s%n",
                    appIndex++,
                    app.getName() != null ? app.getName() : "",
                    app.getMajor() != null ? app.getMajor() : "",
                    app.getYear(),
                    app.getSubmittedDate() != null ? app.getSubmittedDate() : "",
                    app.getStatus() != null ? app.getStatus() : ""
            );
        }
        System.out.println("-------------------------------------------------");

        int studentNumber = promptForStudentNumber(applications.size());
        if (studentNumber == -1) return;

        Application selectedApplication = applications.get(studentNumber - 1);

        String newStatus = promptForNewApplicationStatus();
        if (newStatus == null) return;

        // Call updated controller method: (internshipUUID, studentUserId, newStatus)
        boolean success = companyRepController.updateApplicationStatus(internshipUUID, selectedApplication.getUserId(), newStatus);

        if (success) {
            Map<String, List<Application>> refreshed = companyRepController.getInternshipsWithApplications(companyRep.getCompanyName());
            List<Application> refreshedApps = refreshed.getOrDefault(internshipUUID, List.of());
            Optional<Application> updatedApp = refreshedApps.stream()
                    .filter(a -> a.getUserId() != null && a.getUserId().equals(selectedApplication.getUserId()))
                    .findFirst();

            if (updatedApp.isPresent()) {
                System.out.println("\nSuccess: Application status for " + (updatedApp.get().getName() != null ? updatedApp.get().getName() : updatedApp.get().getUserId()) +
                        " updated to '" + updatedApp.get().getStatus() + "'.");
            } else {
                System.out.println("\nSuccess: Application status updated.");
            }
        } else {
            System.err.println("\nError: Failed to update application status. Please check controller implementation.");
        }
    }

    /**
     * Toggles the visibility of an approved internship posting.
     * <p>
     * Only approved internships can have visibility toggled. Prompts for internship
     * selection and visibility preference (on/off).
     * </p>
     */
    private void handleToggleVisibility() {
        System.out.println("\n--- Toggle Internship Visibility ---");
        System.out.println("\nActive Filters:");
        System.out.println("  Status: " + (statusFilters.isEmpty() ? "[Any]" : statusFilters));
        System.out.println("  Level: " + (levelFilters.isEmpty() ? "[Any]" : levelFilters));
        System.out.println("  Major: " + (majorFilters.isEmpty() ? "[Any]" : majorFilters));
        System.out.println("---------------------------------");

        List<Internship> myInternships = companyRepController.viewMyInternships(companyRep.getCompanyName(), statusFilters, levelFilters, majorFilters);

        if (myInternships.isEmpty()) {
            System.out.println("You have no internships.");
            return;
        }

        // Show internships to the user
        displayInternshipList(myInternships);

        int number = promptForInternshipNumber(myInternships.size());
        if (number == -1) return;

        Internship selected = myInternships.get(number - 1);
        String status = selected.getStatus();

        // Only allow toggling if the internship is approved
        if (status == null || !status.toLowerCase().contains("approved")) {
            System.err.println("Cannot change visibility: Internship must be approved before toggling visibility.\n");
            return;
        }

        int option = promptForOption();
        if (option == -1) return;

        boolean success = companyRepController.toggleInternshipVisibility(selected.getUUID().toString(), option);

        if (success) {
            handleViewMyInternships();
            System.out.println("Internship visibility updated successfully.");
        } else {
            System.err.println("Please try again.");
        }
    }

    /**
     * Manages a filter list (add/remove/clear) used by this interface.
     *
     * @param filterName name shown to the user
     * @param filterList list to mutate
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
     * Allows the representative to set status/level/major filters for listing views.
     */
    private void handleSetFilters() {
        System.out.println("\n--- Set Internship Filters ---");
        // Manage filters relevant to the company rep
        manageFilterList("Status", statusFilters);
        manageFilterList("Level", levelFilters);
        manageFilterList("Major", majorFilters);
        System.out.println("\nAll filters updated successfully.");
    }
}
