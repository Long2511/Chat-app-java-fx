package com.ouroboros.chatapp.chatapp.serverside;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EncryptionUtil {
    private static final String ALGORITHM = "AES";
    private static final int    KEY_SIZE  = 256;
    private static final Map<Integer, SecretKey> chatKeys  = new ConcurrentHashMap<>();


    public static String encrypt(String message, int chatID) throws Exception {
        SecretKey key = generateChatKey(chatID);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedMessage, int chatID) throws Exception {
        SecretKey key = getChatKeyKey(chatID);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }



    /**
     * Generate a new secret key for a chat
     * @param chatId the chat ID
     * @return the generated secret key
     */
    public static SecretKey generateChatKey(int chatId) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE); // Using 256-bit key for strong encryption
            SecretKey key = keyGen.generateKey();
            chatKeys.put(chatId, key);
            return key;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate chat key", e);
        }
    }

    /**
     * Remove the secret key for a chat
     * @param chatId the chat ID
     */
    public static void removeChatKey(int chatId) {
        chatKeys.remove(chatId);
    }

    public static SecretKey getChatKeyKey(int chatId) {
        return chatKeys.computeIfAbsent(chatId, EncryptionUtil::generateChatKey);
    }
} 