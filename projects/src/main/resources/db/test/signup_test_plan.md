# Database Integration Test Plan for Signup Functionality

## Test Environment Setup

### Prerequisites
1. Oracle database connection active
2. USER_INFO table with SALT column added
3. Application server running (Tomcat)
4. All security enhancements deployed

### Test Database Setup
```sql
-- Ensure table structure includes salt column
DESC USER_INFO;

-- Clear test data
DELETE FROM USER_INFO WHERE EMAIL LIKE '%test%';
COMMIT;
```

## Test Cases

### 1. Password Security Tests

#### Test 1.1: Password Hashing Verification
- **Input**: Valid signup with password "Test123!"
- **Expected**: Password stored as hash, salt generated and stored
- **Validation**: 
  - Check PW column contains hash (not plain text)
  - Check SALT column contains Base64 encoded salt
  - Verify login works with original password

#### Test 1.2: Salt Uniqueness
- **Input**: Multiple users with same password
- **Expected**: Different salts and hashes for each user
- **Validation**: No duplicate salts in database

### 2. Input Validation Tests

#### Test 2.1: Email Validation
- **Valid**: `test@example.com`, `user.name+tag@domain.co.kr`
- **Invalid**: `invalid-email`, `@domain.com`, `test@`
- **Expected**: Invalid emails rejected with proper error message

#### Test 2.2: Password Strength Validation
- **Valid**: `Test123!`, `MySecure$Pass2024`
- **Invalid**: `12345678`, `password`, `TEST123`
- **Expected**: Weak passwords rejected

#### Test 2.3: Nickname Validation
- **Valid**: `테스트`, `TestUser`, `User_123`
- **Invalid**: `A`, `Very_Long_Nickname_Over_Twenty`, `User@123`
- **Expected**: Invalid nicknames rejected

### 3. Database Integration Tests

#### Test 3.1: Successful Registration
- **Input**: Valid email, password, nickname
- **Expected**: 
  - Record inserted with hashed password and salt
  - JOIN_DATE set to current date
  - Success response returned

#### Test 3.2: Duplicate Email Prevention
- **Input**: Register user with existing email
- **Expected**: Registration rejected with appropriate message

#### Test 3.3: Transaction Integrity
- **Input**: Simulate database error during registration
- **Expected**: No partial data saved, proper error handling

### 4. Security Tests

#### Test 4.1: Input Sanitization
- **Input**: HTML/Script injection attempts
- **Expected**: Malicious input sanitized or rejected

#### Test 4.2: SQL Injection Prevention
- **Input**: SQL injection payloads in form fields
- **Expected**: Queries execute safely with parameterized inputs

### 5. Login Integration Tests

#### Test 5.1: Hashed Password Login
- **Setup**: Register user with new hashed password system
- **Input**: Login with original plain text password
- **Expected**: Successful login with password verification

#### Test 5.2: Legacy Password Migration
- **Setup**: User with old plain text password
- **Input**: Login attempt
- **Expected**: Login fails (requires password reset)

## Test Execution Commands

### Manual Testing URLs
```
Registration: http://localhost:5500/projects/signup.html
Login: http://localhost:5500/projects/login.html
```

### Database Verification Queries
```sql
-- Check password hashing
SELECT EMAIL, 
       CASE WHEN LENGTH(PW) > 20 THEN 'HASHED' ELSE 'PLAIN' END as PW_STATUS,
       CASE WHEN SALT IS NOT NULL THEN 'HAS_SALT' ELSE 'NO_SALT' END as SALT_STATUS
FROM USER_INFO;

-- Verify no duplicate salts
SELECT SALT, COUNT(*) FROM USER_INFO GROUP BY SALT HAVING COUNT(*) > 1;

-- Check recent registrations
SELECT EMAIL, NICKNAME, JOIN_DATE 
FROM USER_INFO 
WHERE JOIN_DATE >= TRUNC(SYSDATE)
ORDER BY JOIN_DATE DESC;
```

## Success Criteria

### Security ✓
- [ ] Passwords stored as hashes (not plain text)
- [ ] Unique salt generated for each user
- [ ] Input validation prevents injection attacks
- [ ] Server-side validation matches frontend rules

### Functionality ✓
- [ ] New users can register successfully
- [ ] Email uniqueness enforced
- [ ] Registered users can login
- [ ] Session management works correctly

### Database ✓
- [ ] Data integrity maintained
- [ ] Transaction rollback on errors
- [ ] Proper error logging
- [ ] Performance within acceptable limits (<2s registration)

### User Experience ✓
- [ ] Clear error messages for validation failures
- [ ] Successful registration redirects to login
- [ ] No data loss on validation errors
- [ ] Responsive feedback during processing

## Known Issues & Limitations

1. **Legacy Password Migration**: Existing users with plain text passwords need password reset
2. **Database Schema**: Requires manual execution of migration script
3. **Error Handling**: Some database connection errors may not be user-friendly
4. **Performance**: Password hashing adds ~100ms to registration time (acceptable)

## Deployment Checklist

- [ ] Database migration script executed
- [ ] Security utility classes deployed
- [ ] Updated model classes deployed  
- [ ] Enhanced controllers deployed
- [ ] MyBatis mappings updated
- [ ] Application server restarted
- [ ] Test plan executed successfully