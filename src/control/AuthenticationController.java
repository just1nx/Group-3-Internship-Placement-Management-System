package control;

import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.Student;
import entity.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.*;
import java.util.regex.Pattern;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Arrays;

/**
 * Controller responsible for authentication-related operations.
 * <p>
 * Loads user data from CSV sources and provides helper methods for validating
 * credentials, registering company representatives, and changing passwords.
 * Uses PBKDF2 with HMAC-SHA256 for secure password hashing.
 * </p>
 */
public class AuthenticationController extends BaseController {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int PBKDF2_KEY_LENGTH = 256; // bits

    private final Map<String, Student> students;
    private final Map<String, CompanyRepresentative> companyReps;
    private final Map<String, CareerCenterStaff> staffs;

    private static final Path studentPath = Paths.get("data/sample_student_list.csv");
    private static final Path companyRepPath = Paths.get("data/sample_company_representative_list.csv");
    private static final Path staffPath = Paths.get("data/sample_staff_list.csv");

    private static final Pattern Email_PATTERN = Pattern.compile("^[A-Za-z0-9+_.\\-]+@[A-Za-z0-9.\\-]+$");

    /**
     * Create a controller and pre-load users from CSV files.
     */
    public AuthenticationController() {
        students = loadStudents(studentPath);
        companyReps = loadCompanyReps(companyRepPath);
        staffs = loadStaffs(staffPath);
    }

    /**
     * Validate an email string against the controller's email pattern.
     *
     * @param email the email to validate
     * @return true when the email matches the expected format
     */
    public boolean isValidEmail(String email) {
        return Email_PATTERN.matcher(email).matches();
    }

    /**
     * Check whether a student id exists in the loaded student map.
     *
     * @param studentId the student identifier to check
     * @return true if the student id is present
     */
    public boolean isValidStudentId(String studentId) {
        return students.containsKey(studentId);
    }

    /**
     * Check whether a company representative email exists in the loaded map.
     *
     * @param email the company representative email to check
     * @return true if the email is present as a company representative id
     */
    public boolean isValidCompanyRepEmail(String email) {
        return companyReps.containsKey(email);
    }

    /**
     * Check whether a career centre staff id exists in the loaded staff map.
     *
     * @param staffId the staff identifier to check
     * @return true if the staff id is present
     */
    public boolean isValidStaffId(String staffId) {
        return staffs.containsKey(staffId);
    }

    /**
     * Attempt to authenticate a user by id/email and password.
     * <p>
     * For company representatives, only accounts with "Approved" status are allowed to log in.
     * Password verification uses PBKDF2 hashing. Error messages are printed to stderr on failure.
     * </p>
     *
     * @param userID   the login identifier (student id, staff id, or company rep email)
     * @param password plain-text password to verify against stored hash
     * @return the authenticated User instance on success, or null on failure
     */
    public User login(String userID, String password) {
        if ((isValidStudentId(userID) && verifyPassword(password, students.get(userID).getPasswordHash()))) {
            return students.get(userID);
        } else if (isValidCompanyRepEmail(userID) && verifyPassword(password, companyReps.get(userID).getPasswordHash())) {
            if (Objects.equals(companyReps.get(userID).getStatus(), "Approved")) {
                return companyReps.get(userID);
            } else {
                System.err.println("Account not approved by staff yet.");
                return null;
            }
        } else if (isValidStaffId(userID) && verifyPassword(password, staffs.get(userID).getPasswordHash())) {
            return staffs.get(userID);
        } else {
            System.err.println("Invalid credentials.");
            return null;
        }
    }

    /**
     * Register a new company representative account (initial status "Pending").
     * The new account is put into the in-memory map and the company CSV is rewritten.
     * Password is securely hashed using PBKDF2 before storage.
     *
     * @param email       account email (also used as id)
     * @param name        representative name
     * @param password    plain-text password (will be hashed before storage)
     * @param companyName company name
     * @param department  department name
     * @param position    position/title
     * @return true when the CSV rewrite succeeded; false otherwise
     */
    public Boolean register(String email, String name, String password, String companyName, String department, String position) {
        CompanyRepresentative companyRep = new CompanyRepresentative(email, name, hashPassword(password), companyName, department, position, email, "Pending");
        companyReps.put(email, companyRep);
        return rewriteCompanyRepCSV(companyRepPath, companyReps);
    }

    /**
     * Change the password for the given logged-in user.
     * <p>
     * Updates the in-memory object with a newly hashed password and persists
     * the change to the corresponding CSV.
     * </p>
     *
     * @param loggedInUser the user object whose password will be changed
     * @param newPassword  the new plain-text password (will be hashed before storage)
     * @return true if the password update and persistence succeeded, false otherwise
     */
    public boolean changePassword(User loggedInUser, String newPassword) {
        // Update the password in the in-memory user object
        // This also updates the object within the 'students', 'companyReps', or 'staff' map
        loggedInUser.setPasswordHash(hashPassword(newPassword));

        // Determine user type and call the appropriate write method
        switch (loggedInUser) {
            case Student student -> {
                return rewriteStudentCSV(studentPath, students);
            }
            case CompanyRepresentative companyRepresentative -> {
                return rewriteCompanyRepCSV(companyRepPath, companyReps);
            }
            case CareerCenterStaff staff -> {
                return rewriteStaffCSV(staffPath, staffs);
            }
            default -> {
                // Handle unknown user types
                System.err.println("Password change failed: Unknown user type.");
                return false;
            }
        }
    }

    /**
     * Hash a plain-text password using PBKDF2 with HMAC-SHA256.
     * <p>
     * Generates a random 16-byte salt and applies 65,536 iterations to produce
     * a 256-bit hash. The result is formatted as "iterations:salt:hash" with
     * salt and hash Base64-encoded.
     * </p>
     *
     * @param password the plain-text password to hash
     * @return formatted hash string containing iterations, salt and hash
     */
    protected String hashPassword(String password) {
        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH);
        return PBKDF2_ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verify a plain-text password against a stored hash.
     * <p>
     * Extracts the iterations and salt from the stored hash, recomputes the hash
     * for the provided password, and performs a constant-time comparison.
     * </p>
     *
     * @param password the plain-text password to verify
     * @param stored   the stored hash string in "iterations:salt:hash" format
     * @return true if the password matches the stored hash, false otherwise
     */
    protected boolean verifyPassword(String password, String stored) {
        if (stored == null || password == null) return false;
        String[] parts = stored.split(":");
        if (parts.length != 3) return false;
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] hash = Base64.getDecoder().decode(parts[2]);
        byte[] testHash = pbkdf2(password.toCharArray(), salt, iterations, hash.length * 8);
        return Arrays.equals(hash, testHash);
    }

    /**
     * Generate a PBKDF2 hash using the specified parameters.
     * <p>
     * Uses PBKDF2 with HMAC-SHA256 algorithm to derive a key from the password
     * and salt with the given iteration count and key length.
     * </p>
     *
     * @param password       password characters to hash
     * @param salt           salt bytes for the hash
     * @param iterations     number of PBKDF2 iterations
     * @param keyLengthBits  desired key length in bits
     * @return byte array containing the derived hash
     * @throws RuntimeException if hashing fails due to algorithm unavailability or other errors
     */
    protected byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            KeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("Error while hashing a password: " + e.getMessage(), e);
        }
    }
}
