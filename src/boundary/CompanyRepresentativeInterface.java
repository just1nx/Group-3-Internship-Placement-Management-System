package boundary;

import control.CompanyRepresentativeController;
import entity.Application;
import entity.CompanyRepresentative;
import entity.Internship;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

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
            System.out.println("\n=========================================================");
            System.out.println("Company Representative Menu - Welcome, " + companyRep.getName() + ", " + companyRep.getCompanyName());
            System.out.println("=========================================================");
            System.out.println("1. Create Internship Opportunity");
            System.out.println("2. Edit Internship Opportunity");
            System.out.println("3. Delete Internship Opportunity");
            System.out.println("4. View My Internship Opportunities");
            System.out.println("5. View My Applications by Internship");
            System.out.println("6. Manage Applications for an Internship");
            System.out.println("7. Toggle Internship Visibility");
            System.out.println("8. Logout");
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
                    handleToggleVisibility();
                    break;
                case "8":
                    running = false; // Exits the while loop
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        // Loop has ended
        System.out.println("Logging out... Returning to main menu.");
    }

    // Helper methods
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

    private int promptForSlots(String current) {
        while (true) {
            if (current == null) {
                System.out.print("Enter Number of Slots (1-10, or 0 to Cancel): ");
            } else {
                System.out.print("Number of Slots (1-10) (current: " + current + " ): ");
            }
            System.out.print("Enter Number of Slots (1-10, or 0 to Cancel): ");
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
                    return "Approved";
                case "2":
                    return "Rejected";
                case "0":
                    System.out.println("Cancelled status change.");
                    return null;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 0.");
            }
        }
    }

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

    private void handleEditInternship() {
        System.out.println("\n--- Edit Internship Details ---");

        List<Internship> myInternships = companyRepController.viewMyInternships(companyRep.getCompanyName());

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

    private void handleDeleteInternship() {
        System.out.println("\n--- Delete Internship Opportunity ---");

        List<Internship> myInternships = companyRepController.viewMyInternships(companyRep.getCompanyName());

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

    private void handleViewMyInternships() {
        List<Internship> myInternships = companyRepController.viewMyInternships(companyRep.getCompanyName());
        displayInternshipList(myInternships);
    }

    private void handleViewApplications() {
        System.out.println("\n--- View Applications by Internship ---");
        System.out.println("==========================================");

        // Use the new controller method to get structured data: Map<Internship UUID, List<Application>>
        Map<String, List<Application>> applicationsByInternship =
                companyRepController.getInternshipsWithApplications(companyRep.getCompanyName());

        if (applicationsByInternship.isEmpty()) {
            System.out.println("No internships found for " + companyRep.getCompanyName() + ", or unable to read application data.");
            return;
        }

        // Get the list of the company's internship objects to iterate in order
        List<Internship> myInternships = companyRepController.viewMyInternships(companyRep.getCompanyName());

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

    private void handleManageApplications() {
        System.out.println("\n--- Manage Applications by Internship ---");

        List<Internship> myInternships = companyRepController.viewMyInternships(companyRep.getCompanyName());

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

    private void handleToggleVisibility() {
        System.out.println("\n--- Toggle Internship Visibility ---");

        List<Internship> myInternships = companyRepController.viewMyInternships(companyRep.getCompanyName());

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
}
