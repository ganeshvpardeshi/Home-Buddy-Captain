package com.example.home_buddy_captain.initial_connection;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHashingSecurity {
//    RETURNS THE HASHED PASSWORD
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12)); // 12 = work factor
    }

//    CHECKS THE USER ENTERED PASSWORD AND HASHED PASSWORD.
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
