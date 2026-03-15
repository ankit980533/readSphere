#!/bin/bash

# Test BCrypt password matching using Java

cd "$(dirname "$0")"

cat > TestBCrypt.java << 'EOF'
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        String dbHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        
        System.out.println("Testing BCrypt password matching:");
        System.out.println("Raw password: " + rawPassword);
        System.out.println("DB hash: " + dbHash);
        System.out.println("Match result: " + encoder.matches(rawPassword, dbHash));
        System.out.println();
        System.out.println("Generating new hash for 'admin123':");
        System.out.println(encoder.encode(rawPassword));
    }
}
EOF

# Compile and run with Spring Security on classpath
mvn exec:java -Dexec.mainClass="TestBCrypt" -Dexec.classpathScope=compile -q 2>/dev/null || {
    echo "Failed to run test. Trying alternative method..."
    
    # Try using jshell if available
    if command -v jshell &> /dev/null; then
        echo "Using jshell..."
        jshell --class-path ~/.m2/repository/org/springframework/security/spring-security-crypto/6.2.0/spring-security-crypto-6.2.0.jar << 'JSHELL'
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String rawPassword = "admin123";
String dbHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
System.out.println("Match: " + encoder.matches(rawPassword, dbHash));
System.out.println("New hash: " + encoder.encode(rawPassword));
/exit
JSHELL
    fi
}

rm -f TestBCrypt.java TestBCrypt.class
