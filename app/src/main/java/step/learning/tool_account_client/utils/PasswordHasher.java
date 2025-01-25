package step.learning.tool_account_client.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256; // 32 bytes * 8
    private static final int SALT_LENGTH = 16; // 16 bytes

    // Генерація випадкової солі
    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    // Хешування пароля з використанням PBKDF2
    public static String hashPassword(String password) {
        byte[] salt = generateSalt();
        byte[] hash = hashPasswordWithSalt(password, salt);

        // Об'єднуємо сіль та хеш
        byte[] hashBytes = new byte[SALT_LENGTH + hash.length];
        System.arraycopy(salt, 0, hashBytes, 0, SALT_LENGTH);
        System.arraycopy(hash, 0, hashBytes, SALT_LENGTH, hash.length);

        // Повертаємо хеш у форматі Base64
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    // Хешування пароля з використанням заданої солі
    private static byte[] hashPasswordWithSalt(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Перевірка пароля
    public static boolean verifyPassword(String password, String hashedPassword) {
        byte[] hashBytes = Base64.getDecoder().decode(hashedPassword);
        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(hashBytes, 0, salt, 0, SALT_LENGTH);

        byte[] hash = hashPasswordWithSalt(password, salt);

        for (int i = 0; i < hash.length; i++) {
            if (hashBytes[i + SALT_LENGTH] != hash[i]) {
                return false;
            }
        }
        return true;
    }
}
