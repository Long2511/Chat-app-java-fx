package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.STATUS;

public class AuthHandler {

    public static STATUS RequestLogin(String email, String password) {
        // Compare with data from the database
        // return STATUS.SUCCESS; // or STATUS.FAILURE based on the comparison

        return STATUS.SUCCESS; // Placeholder for successful login
    }

    public static STATUS RequestRegister(String username, String email, String password) {
        // Create new account in the database
        // return STATUS.SUCCESS; // or STATUS.FAILURE based on the operation result

        return STATUS.SUCCESS; // Placeholder for successful registration
    }
}
