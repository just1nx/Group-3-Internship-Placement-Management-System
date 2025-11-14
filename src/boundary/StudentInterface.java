package boundary;

import control.StudentController;
import entity.Student;

import java.util.Scanner;

public class StudentInterface implements CommandLineInterface {
    private final Scanner scanner = new Scanner(System.in);
    private final StudentController studentController = new StudentController();
    private final Student student;

    public StudentInterface(Student student) {
        this.student = student;
    }

    @Override
    public void display() {
        boolean running = true;
        while (running) {
            System.out.println("\n==========================================");
            System.out.println("Student Menu - Welcome, " + student.getName());
            System.out.println("==========================================");
            System.out.println("1. View and Apply for Internships");
            System.out.println("2. View, Accept and Withdraw Applications");
            System.out.println("3. Logout");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleViewInternships();
                    break;
                case "2":
                    handleViewApplications();
                    break;
                case "3":
                    running = false; // Exits the while loop
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        // Loop has ended
        System.out.println("Logging out... Returning to main menu.");
    }

    private void handleViewInternships() {
        // System.out.println("\n--- View Available Internships ---");
        // // 1. Call studentController.getAvailableInternships()
        // //    (This method in the controller will handle filtering by year, major, and visibility)
        // // 2. Display the list of internships
        // // 3. Prompt user to "Apply" or "Go Back"
        // // 4. If Apply:
        // //    - Check student.getNumberOfApplications() < 3
        // //    - Call studentController.applyForInternship(internshipId)
        // System.out.println("... (To be implemented: Show filtered internship list) ...");

        studentController.applyForInternship(this.student);
    }

    /**
     * Placeholder method to handle viewing, accepting, or withdrawing applications.
     * This will call your StudentController.
     */
    private void handleViewApplications() {
        System.out.println("\n--- View My Applications ---");
        // 1. Call studentController.getMyApplications()
        // 2. Display the list with their status ("Pending", "Successful", "Unsuccessful")
        // 3. If any are "Successful", prompt to "Accept"
        //    - If Accept: call studentController.acceptOffer(applicationId)
        // 4. Prompt to "Withdraw"
        //    - If Withdraw: call studentController.requestWithdrawal(applicationId)
        System.out.println("... (To be implemented: Show list of applied internships) ...");
    }
}
