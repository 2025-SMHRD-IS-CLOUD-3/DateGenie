package com.smhrd.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Security utility class for password hashing and validation
 * Uses SHA-256 with salt for secure password storage
 */
public class SecurityUtils {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 32;
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * Generate a random salt for password hashing
     * @return Base64 encoded salt string
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hash a password with salt using SHA-256
     * @param password Plain text password
     * @param salt Salt string
     * @return Hashed password
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            
            // Combine password and salt
            String saltedPassword = password + salt;
            byte[] hashedBytes = md.digest(saltedPassword.getBytes("UTF-8"));
            
            // Convert to Base64 string
            return Base64.getEncoder().encodeToString(hashedBytes);
            
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    
    /**
     * Verify a password against stored hash and salt
     * @param password Plain text password to verify
     * @param storedHash Stored hashed password
     * @param salt Salt used for hashing
     * @return true if password matches
     */
    public static boolean verifyPassword(String password, String storedHash, String salt) {
        try {
            String hashedPassword = hashPassword(password, salt);
            return hashedPassword.equals(storedHash);
        } catch (Exception e) {
            System.err.println("Password verification failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate password strength
     * @param password Password to validate
     * @return true if password meets security requirements
     */
    public static boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8 || password.length() > 64) {
            return false;
        }
        
        // Check for whitespace
        if (password.contains(" ")) {
            return false;
        }
        
        // Check for at least 3 of 4 character types
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
        
        int criteriaCount = 0;
        if (hasLower) criteriaCount++;
        if (hasUpper) criteriaCount++;
        if (hasDigit) criteriaCount++;
        if (hasSpecial) criteriaCount++;
        
        return criteriaCount >= 3;
    }
    
    /**
     * Validate email format
     * @param email Email to validate
     * @return true if email format is valid
     */
    public static boolean isEmailValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Basic email regex pattern
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex) && email.length() <= 320; // RFC 5321 limit
    }
    
    /**
     * Validate nickname format
     * @param nickname Nickname to validate
     * @return true if nickname format is valid
     */
    public static boolean isNicknameValid(String nickname) {
        if (nickname == null) {
            return false;
        }
        
        String trimmed = nickname.trim();
        if (trimmed.length() < 2 || trimmed.length() > 20) {
            return false;
        }
        
        // Allow Korean, English, numbers, and underscore only
        return trimmed.matches("^[A-Za-z0-9_가-힣]+$");
    }
    
    /**
     * Sanitize input to prevent injection attacks
     * @param input Input string to sanitize
     * @return Sanitized string
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        return input.trim()
                   .replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("'", "&#x27;")
                   .replaceAll("&", "&amp;");
    }
}