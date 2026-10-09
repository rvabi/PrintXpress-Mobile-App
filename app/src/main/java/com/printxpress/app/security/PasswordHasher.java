package com.printxpress.app.security;

import android.util.Base64;
import android.os.Build;
import java.security.SecureRandom;
import java.security.GeneralSecurityException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {
    private static final int ITERATIONS = 120000;
    private static final int KEY_BITS = 256;
    private static final String SHA256 = "pbkdf2_sha256";
    private static final String SHA1 = "pbkdf2_sha1";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() { }

    public static String hash(char[] password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return hashWithSalt(password, salt, Build.VERSION.SDK_INT >= 26 ? SHA256 : SHA1);
    }

    // Kept package-private so tests can check both formats with a known salt.
    static String hashWithSalt(char[] password, byte[] salt, String algorithm) {
        if (!SHA256.equals(algorithm) && !SHA1.equals(algorithm)) throw new IllegalArgumentException("Unknown password algorithm");
        byte[] derived = derive(password, salt, ITERATIONS, algorithm);
        return algorithm + "$" + ITERATIONS + "$" + encode(salt) + "$" + encode(derived);
    }

    public static boolean verify(char[] password, String stored) {
        if (stored == null) return false;
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || (!SHA256.equals(parts[0]) && !SHA1.equals(parts[0]))) return false;
        try {
            int count = Integer.parseInt(parts[1]);
            if (count < 10000 || count > 1000000) return false;
            byte[] expected = Base64.decode(parts[3], Base64.NO_WRAP);
            byte[] salt = Base64.decode(parts[2], Base64.NO_WRAP);
            if (salt.length != 16 || expected.length != KEY_BITS / 8) return false;
            byte[] actual = derive(password, salt, count, parts[0]);
            if (expected.length != actual.length) return false;
            int diff = 0;
            for (int i = 0; i < expected.length; i++) diff |= expected[i] ^ actual[i];
            return diff == 0;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations, String algorithm) {
        // Android supplies PBKDF2-HMAC-SHA1 from API 10 and SHA256 from API 26.
        // The local SHA256 implementation lets API 24/25 verify older SHA256 records.
        if (SHA256.equals(algorithm) && Build.VERSION.SDK_INT < 26) return deriveSha256Compat(password, salt, iterations);
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            String factory = SHA256.equals(algorithm) ? "PBKDF2WithHmacSHA256" : "PBKDF2WithHmacSHA1";
            return SecretKeyFactory.getInstance(factory).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Password hashing unavailable", ex);
        } finally {
            spec.clearPassword();
        }
    }

    // PBKDF2's HMAC block loop for a 256-bit SHA256 key (one 32-byte block).
    private static byte[] deriveSha256Compat(char[] password, byte[] salt, int iterations) {
        byte[] passwordBytes = new String(password).getBytes(StandardCharsets.UTF_8);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(passwordBytes, "HmacSHA256"));
            mac.update(salt);
            byte[] block = mac.doFinal(new byte[]{0, 0, 0, 1});
            byte[] result = block.clone();
            for (int i = 1; i < iterations; i++) {
                block = mac.doFinal(block);
                for (int j = 0; j < result.length; j++) result[j] ^= block[j];
            }
            Arrays.fill(block, (byte) 0);
            return result;
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Password hashing unavailable", ex);
        } finally {
            Arrays.fill(passwordBytes, (byte) 0);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
