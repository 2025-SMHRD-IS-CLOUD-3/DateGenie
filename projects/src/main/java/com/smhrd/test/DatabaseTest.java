package com.smhrd.test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseTest {
    public static void main(String[] args) {
        Connection conn = null;
        
        try {
            // Database connection info
            String url = "jdbc:oracle:thin:@project-db-campus.smhrd.com:1524:xe";
            String username = "campus_24IS_CLOUD3_p2_4";
            String password = "smhrd4";
            
            System.out.println("=== Database Connection Test ===");
            System.out.println("URL: " + url);
            System.out.println("Username: " + username);
            
            // Load Oracle driver
            Class.forName("oracle.jdbc.driver.OracleDriver");
            
            // Connect to database
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("SUCCESS: Connected to database!");
            
            // Check if USER_EMAIL_AUTH table exists
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, "CAMPUS_24IS_CLOUD3_P2_4", "USER_EMAIL_AUTH", null);
            
            if (tables.next()) {
                System.out.println("SUCCESS: USER_EMAIL_AUTH table EXISTS!");
                
                // Count records
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM CAMPUS_24IS_CLOUD3_P2_4.USER_EMAIL_AUTH");
                if (rs.next()) {
                    System.out.println("Table has " + rs.getInt(1) + " records");
                }
                rs.close();
                stmt.close();
            } else {
                System.out.println("ERROR: USER_EMAIL_AUTH table DOES NOT EXIST!");
                System.out.println("\nPlease run the following SQL commands:");
                System.out.println("----------------------------------------");
                System.out.println("CREATE TABLE CAMPUS_24IS_CLOUD3_P2_4.USER_EMAIL_AUTH (");
                System.out.println("    ID NUMBER PRIMARY KEY,");
                System.out.println("    EMAIL VARCHAR2(255) NOT NULL,");
                System.out.println("    VERIFICATION_TOKEN VARCHAR2(255) UNIQUE NOT NULL,");
                System.out.println("    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,");
                System.out.println("    EXPIRES_AT TIMESTAMP NOT NULL,");
                System.out.println("    VERIFIED NUMBER(1,0) DEFAULT 0");
                System.out.println(");");
                System.out.println("");
                System.out.println("CREATE SEQUENCE CAMPUS_24IS_CLOUD3_P2_4.USER_EMAIL_AUTH_SEQ");
                System.out.println("    START WITH 1 INCREMENT BY 1 CACHE 20;");
                System.out.println("----------------------------------------");
            }
            tables.close();
            
            // Check if USER_INFO table exists
            ResultSet userTable = metaData.getTables(null, "CAMPUS_24IS_CLOUD3_P2_4", "USER_INFO", null);
            if (userTable.next()) {
                System.out.println("SUCCESS: USER_INFO table EXISTS!");
                
                // Count users
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM CAMPUS_24IS_CLOUD3_P2_4.USER_INFO");
                if (rs.next()) {
                    System.out.println("USER_INFO has " + rs.getInt(1) + " users");
                }
                rs.close();
                stmt.close();
            } else {
                System.out.println("ERROR: USER_INFO table DOES NOT EXIST!");
            }
            userTable.close();
            
        } catch (Exception e) {
            System.out.println("ERROR: Database connection failed!");
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}