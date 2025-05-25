package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.STATUS;
import com.ouroboros.chatapp.chatapp.datatype.User;

public class UserService {
    public static STATUS Login(String username, String password) {
        // TODO: Implement the login logic here.
        return STATUS.SUCCESS;
    }
    public static STATUS Register(String username, String password) {
        // TODO: Implement the registration logic here.
        return STATUS.SUCCESS;
    }
    public static STATUS Logout(String username) {
        // TODO: Implement the logout logic here.
        return STATUS.SUCCESS;
    }
    public static STATUS DeleteAccount(String username) {
        // TODO: Implement the account deletion logic here.
        return STATUS.SUCCESS;
    }

    public static User SearchUser(String username) {
        // TODO: Implement the user search logic here.
        // This is a stub method. In a real application, this would query the database.
        return null;
    }
}
