package boundary;

/**
 * Simple interface for command-line interactive components in the application.
 * Implementations should present a menu or interactive session when display() is invoked.
 */
public interface CommandLineInterface {
    /**
     * Start the interactive display loop for this interface.
     * Implementations should return when the user chooses to exit or log out.
     */
    void display();
}
