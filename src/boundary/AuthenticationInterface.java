package boundary;

import control.AuthController;
import entity.User;
import entity.Student;
import entity.CareerCenterStaff;
import entity.CompanyRepresentative;

import java.util.Scanner;

// This class represents the login interface of the application.
public class AuthenticationInterface implements CommandLineInterface {
    private Scanner scanner = new Scanner(System.in);
    private AuthController authController = new AuthController();

    @Override
    public void display() {
        // This is the main application loop
        while (true) {
            System.out.println("==========================================");
            System.out.println("   Internship Placement Management System   ");
            System.out.println("==========================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegister(); // Call the new method
                    break;
                case "3":
                    System.out.println("Exiting system. Goodbye!");
                    return; // Exit the display method, which ends the program
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void handleLogin() {
        System.out.print("Enter User ID: ");
        String userID = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        User loggedInUser = authController.login(userID, password);

        if (loggedInUser == null) {
            System.out.println("Login failed. Invalid User ID or Password.");
        } else {
            System.out.println("Login successful! Welcome, " + loggedInUser.getName());
            // Now, show the correct menu based on the user's role
            showUserMenu(loggedInUser);
        }
    }

    private void handleRegister() {
        System.out.println("\n--- Company Representative Registration ---");
        System.out.println("Please enter your details. Your account will require staff approval.");

        // As per PDF, Company Rep ID is their email [cite: 38]
        System.out.print("Enter Email (this will be your User ID): ");
        String email = scanner.nextLine();

        System.out.print("Enter Your Name: ");
        String name = scanner.nextLine();

        // As per PDF, default password is "password"[cite: 40],
        // but for a new registration, it's better to let them set one.
        // Let's follow the spirit of setting a password.
        System.out.print("Enter Password (default is 'password'): ");
        String password = scanner.nextLine();
        if (password.isEmpty()) {
            password = "password"; // Enforce default if they just press enter
        }

        // As per PDF, we need Company Name, Department, and Position [cite: 44]
        System.out.print("Enter Company Name: ");
        String companyName = scanner.nextLine();

        System.out.print("Enter Department: ");
        String department = scanner.nextLine();

        System.out.print("Enter Position: ");
        String position = scanner.nextLine();

        // --- Pass to Controller ---
        // This 'registerCompanyRepresentative' method is in your AuthController
//        try {
//            boolean success = authController.register(
//                    email, name, password, companyName, department, position
//            );
//
//            if (success) {
//                System.out.println("Registration successful!");
//                System.out.println("Your account (ID: " + email + ") is pending approval from a Career Center Staff.");
//                System.out.println("You will be able to log in once approved.");
//            } else {
//                System.out.println("Registration failed. An account with this email may already exist.");
//            }
//        } catch (Exception e) {
//            // Catch any other errors, e.g., validation
//            System.out.println("Registration failed: " + e.getMessage());
//        }
    }

    private void showUserMenu(User user) {
        // Check the *type* of user object
        if (user instanceof Student) {
            CommandLineInterface studentMenu = new StudentInterface((Student) user);

            System.out.println("Student login successful!");

            // studentMenu.display();

        } else if (user instanceof CareerCenterStaff) {
            // CommandLineInterface staffMenu = new CareerCenterStaffInterface((CareerCenterStaff) user);

            // staffMenu.display();
        } else if (user instanceof CompanyRepresentative) {
            // CommandLineInterface repMenu = new CompanyRepresentativeInterface((CompanyRepresentative) user);

            // repMenu.display();
        }
    }
}
