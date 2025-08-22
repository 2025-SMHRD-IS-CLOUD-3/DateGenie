package com.smhrd.test;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import com.smhrd.model.EmailVerification;
import com.smhrd.model.EmailVerificationDAO;
import com.smhrd.util.EmailConfig;

public class EmailVerificationTest {
    public static void main(String[] args) {
        System.out.println("=== Email Verification System Test ===");
        
        // Test 1: Email Configuration
        System.out.println("\n1. Testing Email Configuration:");
        EmailConfig.printConfig();
        
        // Test 2: Database table and sequence test
        System.out.println("\n2. Testing Database Insert:");
        
        EmailVerificationDAO dao = new EmailVerificationDAO();
        
        // Create test email verification
        String testEmail = "test@example.com";
        String testToken = "test-token-123456789";
        
        // Set expiry time (24 hours from now)
        long expiryMillis = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24);
        Timestamp expiresAt = new Timestamp(expiryMillis);
        
        EmailVerification testVerification = new EmailVerification(testEmail, testToken, expiresAt);
        
        try {
            System.out.println("Attempting to insert test verification...");
            System.out.println("Email: " + testEmail);
            System.out.println("Token: " + testToken);
            System.out.println("Expires at: " + expiresAt);
            
            int result = dao.insertVerification(testVerification);
            
            if (result > 0) {
                System.out.println("SUCCESS: Email verification inserted! Result: " + result);
                
                // Try to find it back
                EmailVerification found = dao.findByToken(testToken);
                if (found != null) {
                    System.out.println("SUCCESS: Token found in database!");
                    System.out.println("Found ID: " + found.getId());
                    System.out.println("Found Email: " + found.getEmail());
                    System.out.println("Found Token: " + found.getVerificationToken());
                } else {
                    System.out.println("ERROR: Token not found after insert!");
                }
                
                // Clean up test data
                System.out.println("Cleaning up test data...");
                dao.deleteByEmail(testEmail);
                System.out.println("Test data cleaned up");
                
            } else {
                System.out.println("ERROR: Failed to insert email verification. Result: " + result);
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Exception during test:");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Test Complete ===");
    }
}