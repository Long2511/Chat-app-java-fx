package com.ouroboros.chatapp.chatapp.serverside;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EncryptionUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding"; // Specify padding mode
    private static final Map<Integer, SecretKey> chatKeys = new ConcurrentHashMap<>();
    private static final Object lockObject = new Object(); // Lock object for synchronization
    private static final String SECRET_SALT = "OUROBOROS_CHAT_APP_SALT_2025"; // Salt for generating deterministic keys

    public static String encrypt(String message, int chatID) throws Exception {
        if (message == null || message.isEmpty()) {
            return message; // Return as is if empty
        }

        SecretKey key = getChatKey(chatID);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedMessage, int chatID) throws Exception {
        if (encryptedMessage == null || encryptedMessage.isEmpty()) {
            return encryptedMessage; // Return as is if empty
        }

        try {
            SecretKey key = getChatKey(chatID);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Decryption failed for chat " + chatID + ": " + e.getMessage());
            // Return the original message if decryption fails
            // This helps with backward compatibility for unencrypted messages
            return encryptedMessage;
        }
    }

    /**
     * Get or create a secret key for a chat
     * @param chatId the chat ID
     * @return the secret key
     */
    public static SecretKey getChatKey(int chatId) {
        // Check if the key exists first
        SecretKey key = chatKeys.get(chatId);
        if (key != null) {
            return key;
        }

        // If not, create it with proper synchronization
        synchronized (lockObject) {
            // Double-check in case another thread created it while we were waiting
            key = chatKeys.get(chatId);
            if (key != null) {
                return key;
            }

            // Generate and store the key (deterministic based on chat ID)
            key = generateDeterministicKey(chatId);
            chatKeys.put(chatId, key);
            return key;
        }
    }

    /**
     * Generate a deterministic key based on the chat ID
     * This ensures all clients generate the same key for the same chat
     * @param chatId the chat ID
     * @return the generated secret key
     */
    private static SecretKey generateDeterministicKey(int chatId) {
        try {
            // Create a seed string combining the chat ID and a salt
            String seed = SECRET_SALT + chatId;

            // Use SHA-256 to create a hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(seed.getBytes(StandardCharsets.UTF_8));

            // Use the first 16 bytes (128 bits) for AES key
            keyBytes = Arrays.copyOf(keyBytes, 16); // AES-128 needs 16 bytes

            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate deterministic chat key", e);
        }
    }

    /**
     * Remove the secret key for a chat
     * @param chatId the chat ID
     */
    public static void removeChatKey(int chatId) {
        chatKeys.remove(chatId);
    }
}

