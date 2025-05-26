package com.ouroboros.chatapp.chatapp.serverside;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Key;
import java.util.Date;

public class Utils {
    private static final String JWT_SECRET = DatabaseUtils.getEnvVar("JWT_SECRET");
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

    public static String readPacket(BufferedReader in) {
        StringBuilder packet = new StringBuilder();
        String line;
        try {
            while ((line = in.readLine()) != null) {
                if (line.isEmpty()) {
                    break; // End of packet
                }
                packet.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packet.toString().trim(); // Return the complete packet as a string
    }

    public static String createJwtToken(long userId, String username, String email) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Token valid for 1 day
                .signWith(SECRET_KEY)
                .compact();
    }
}
