package app;

import boundary.CommandLineInterface;
import boundary.AuthenticationInterface;

public class InternshipManagementSystem {
    public static void main(String[] args) {
        CommandLineInterface authInterface = new AuthenticationInterface();

        authInterface.display();
    }
}
