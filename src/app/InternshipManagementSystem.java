package app;

import boundary.CommandLineInterface;
import boundary.AuthenticationInterface;

/**
 * Entry point for the Internship Placement Management System.
 * <p>
 * Initializes the authentication command-line interface and starts the
 * interactive display loop.
 * </p>
  */
public class InternshipManagementSystem {
    /**
     * Main method. Creates the authentication interface and starts the UI.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        CommandLineInterface authInterface = new AuthenticationInterface();

        authInterface.display();
    }
}
