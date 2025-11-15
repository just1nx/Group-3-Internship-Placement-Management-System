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
            System.out.println("\n==========================================");
            System.out.println("Company Representative Menu - Welcome, " + companyRep.getName() + ", " + companyRep.getCompanyName());
            System.out.println("==========================================");
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

    private int promptForNumber() {
        while (true) {
            System.out.print("Enter the internship number (or 0 to Cancel): ");
            String number = scanner.nextLine();

            if (number.equals("0")) {
                System.out.println("Cancelled Operation.");
                return -1;
            }

            try {
                int number2 = Integer.parseInt(number);
                if (number2 >= 1 &&  number2 <= companyRepController.viewMyInternships(companyRep.getCompanyName()).size()) {
                    return number2;
                } else {
                    System.out.println("Invalid input. Please enter a valid internship number.");
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
        handleViewMyInternships();

        int number = promptForNumber();
        if (number == -1) return;

        // Re-fetch to ensure up-to-date selection
        List<Internship> refreshed = companyRepController.viewMyInternships(companyRep.getCompanyName());
        if (number < 1 || number > refreshed.size()) {
            System.out.println("Invalid internship selection.");
            return;
        }

        Internship selected = refreshed.get(number - 1);
        String status = selected.getStatus();
        if (status == null || status.toLowerCase().contains("approved")) {
            System.err.println("Cannot Edit: Internship has already been approved.");
            return;
        }

        System.out.println("Enter new values or leave blank to keep current. Enter `0` to cancel.");

        System.out.print("Title (current: " + (selected.getTitle() == null ? "" : selected.getTitle()) + "): ");
        String title = scanner.nextLine();
        if (title.equals("0")) { System.out.println("Cancelled."); return; }
        if (title.isEmpty()) title = null;

        System.out.print("Description (current: " + (selected.getDescription() == null ? "" : selected.getDescription()) + "): ");
        String description = scanner.nextLine();
        if (description.equals("0")) { System.out.println("Cancelled."); return; }
        if (description.isEmpty()) description = null;

        // TODO: Use promptForLevel() to reduce code length
        // Level: allow 1/2/3 or blank
        String level = null;
        while (true) {
            System.out.print("Level (1: Basic, 2: Intermediate, 3: Advanced) (current: " + (selected.getLevel() == null ? "" : selected.getLevel()) + ", leave blank to keep): ");
            String lvl = scanner.nextLine();
            if (lvl.equals("0")) { System.out.println("Cancelled."); return; }
            if (lvl.isEmpty()) { level = null; break; }
            switch (lvl) {
                case "1": level = "Basic"; break;
                case "2": level = "Intermediate"; break;
                case "3": level = "Advanced"; break;
                default:
                    System.out.println("Invalid choice. Enter 1, 2, 3, blank to keep, or 0 to cancel.");
                    continue;
            }
            break;
        }

        System.out.print("Preferred Major (current: " + (selected.getPreferredMajor() == null ? "" : selected.getPreferredMajor()) + "): ");
        String preferredMajor = scanner.nextLine();
        if (preferredMajor.equals("0")) { System.out.println("Cancelled."); return; }
        if (preferredMajor.isEmpty()) preferredMajor = null;

        // TODO: Use promptForDate() to reduce code length
        // Dates: blank keep, 0 cancel, otherwise validate YYYY-MM-DD
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String openingDate = null;
        while (true) {
            System.out.print("Opening Date (YYYY-MM-DD) (current: " + (selected.getOpeningDate() == null ? "" : selected.getOpeningDate()) + "): ");
            String od = scanner.nextLine();
            if (od.equals("0")) { System.out.println("Cancelled."); return; }
            if (od.isEmpty()) { openingDate = null; break; }
            try {
                LocalDate.parse(od, dtf);
                openingDate = od;
                break;
            } catch (Exception e) {
                System.out.println("Invalid date format. Use YYYY-MM-DD, leave blank to keep, or 0 to cancel.");
            }
        }

        // TODO: Use promptForDate() to reduce code length
        String closingDate = null;
        while (true) {
            System.out.print("Closing Date (YYYY-MM-DD) (current: " + (selected.getClosingDate() == null ? "" : selected.getClosingDate()) + "): ");
            String cd = scanner.nextLine();
            if (cd.equals("0")) { System.out.println("Cancelled."); return; }
            if (cd.isEmpty()) { closingDate = null; break; }
            try {
                LocalDate.parse(cd, dtf);
                closingDate = cd;
                break;
            } catch (Exception e) {
                System.out.println("Invalid date format. Use YYYY-MM-DD, leave blank to keep, or 0 to cancel.");
            }
        }

        // TODO: Use promptForSlots() to reduce code length
        String slots = null;
        while (true) {
            System.out.print("Number of Slots (1-10) (current: " + selected.getNumberOfSlots() + "): ");
            String s = scanner.nextLine();
            if (s.equals("0")) { System.out.println("Cancelled."); return; }
            if (s.isEmpty()) { slots = null; break; }
            try {
                int val = Integer.parseInt(s);
                if (val >= 1 && val <= 10) { slots = Integer.toString(val); break; }
                else System.out.println("Invalid input. Must be 1-10, blank to keep, or 0 to cancel.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a number, blank to keep, or 0 to cancel.");
            }
        }

        boolean success = companyRepController.editInternship(
                companyRep.getCompanyName(),
                number,
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

        handleViewMyInternships();

        int number = promptForNumber();
        if (number == -1) return;

        boolean success = companyRepController.deleteInternship(companyRep.getCompanyName(), number);
        if (success) {
            handleViewMyInternships();
            System.out.println("Internship deleted successfully.");
        } else {
            System.err.println("Please try again.\n");
        }
    }

    private void handleViewMyInternships() {
        List<Internship> myInternships = companyRepController.viewMyInternships(companyRep.getCompanyName());

        if (myInternships.isEmpty()) {
            System.out.println("\n--- No internships found for " + companyRep.getCompanyName() + " ---");
            return;
        }
        System.out.println("\n--- My Internships ---");
        // 2. Iterate and print the details
        int index = 1;
        for (Internship internship : myInternships) {
            // Custom output formatting
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

        handleViewMyInternships();

        int internshipNumber = promptForNumber();
        if (internshipNumber == -1) return;

        if (internshipNumber < 1 || internshipNumber > myInternships.size()) {
            System.out.println("Invalid internship selection.");
            return;
        }

        Internship selectedInternship = myInternships.get(internshipNumber - 1);
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

        List<Internship> internshipList = companyRepController.viewMyInternships(companyRep.getCompanyName());
        if (internshipList.isEmpty()) {
            System.out.println("You have no internships.");
            return;
        }

        // Show internships to the user
        handleViewMyInternships();

        int number = promptForNumber();
        if (number == -1) {
            return; // User cancelled
        }

        // Re-fetch to ensure we have up-to-date data and a matching index
        List<Internship> refreshedList = companyRepController.viewMyInternships(companyRep.getCompanyName());
        if (number < 1 || number > refreshedList.size()) {
            System.out.print("Invalid internship selection.");
            return;
        }

        Internship selected = refreshedList.get(number - 1);
        String status = selected.getStatus();

        // Only allow toggling if the internship is approved
        if (status == null || !status.toLowerCase().contains("approved")) {
            System.err.println("Cannot change visibility: Internship must be approved before toggling visibility.\n");
            return;
        }

        int option = promptForOption();
        if (option == -1) {
            return; // User cancelled
        }

        boolean success = companyRepController.toggleInternshipVisibility(companyRep.getCompanyName(), number, option);
        if (success) {
            handleViewMyInternships();
            System.out.println("Internship visibility updated successfully.");
        } else {
            System.err.println("Please try again.");
        }
    }
}
