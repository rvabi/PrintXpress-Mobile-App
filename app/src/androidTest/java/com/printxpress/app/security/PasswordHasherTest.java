package com.printxpress.app.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PasswordHasherTest {
    // Created with the original PBKDF2-HMAC-SHA256 format and a fixed salt.
    private static final String EXISTING_SHA256 = "pbkdf2_sha256$120000$AAECAwQFBgcICQoLDA0ODw==$L1WUSS9BGea5x0hSxbxv2wWaYhQ1PcnV/90i7QmAk3k=";
    private static final String EXISTING_UNICODE_SHA256 = "pbkdf2_sha256$120000$AAECAwQFBgcICQoLDA0ODw==$/iOVNqnPxakd2jLrpv4cWQkXqbXPO4J50Dv9+ViGyvI=";

    private byte[] salt(int offset) {
        byte[] result = new byte[16];
        for (int i = 0; i < result.length; i++) result[i] = (byte) (i + offset);
        return result;
    }

    @Test public void samePasswordAndSaltVerifyButWrongPasswordDoesNot() {
        String stored = PasswordHasher.hashWithSalt("Compat123".toCharArray(), salt(0), "pbkdf2_sha1");
        assertEquals(stored, PasswordHasher.hashWithSalt("Compat123".toCharArray(), salt(0), "pbkdf2_sha1"));
        assertTrue(PasswordHasher.verify("Compat123".toCharArray(), stored));
        assertFalse(PasswordHasher.verify("Wrong123".toCharArray(), stored));
    }

    @Test public void differentSaltsProduceDifferentHashes() {
        String first = PasswordHasher.hashWithSalt("Compat123".toCharArray(), salt(0), "pbkdf2_sha1");
        String second = PasswordHasher.hashWithSalt("Compat123".toCharArray(), salt(1), "pbkdf2_sha1");
        assertNotEquals(first, second);
        assertTrue(PasswordHasher.verify("Compat123".toCharArray(), second));
    }

    @Test public void existingSha256RecordStillVerifiesOnThisApi() {
        assertEquals(EXISTING_SHA256, PasswordHasher.hashWithSalt("Compat123".toCharArray(), salt(0), "pbkdf2_sha256"));
        assertTrue(PasswordHasher.verify("Compat123".toCharArray(), EXISTING_SHA256));
        assertFalse(PasswordHasher.verify("Wrong123".toCharArray(), EXISTING_SHA256));
    }

    @Test public void existingUnicodeSha256RecordStillVerifiesOnThisApi() {
        char[] password = "Caf\u00e9Print123".toCharArray();
        assertEquals(EXISTING_UNICODE_SHA256, PasswordHasher.hashWithSalt(password, salt(0), "pbkdf2_sha256"));
        assertTrue(PasswordHasher.verify(password, EXISTING_UNICODE_SHA256));
        assertFalse(PasswordHasher.verify("CafePrint123".toCharArray(), EXISTING_UNICODE_SHA256));
    }

    @Test public void newHashUsesTheSupportedPlatformAlgorithm() {
        String stored = PasswordHasher.hash("Compat123".toCharArray());
        String expectedPrefix = Build.VERSION.SDK_INT >= 26 ? "pbkdf2_sha256$" : "pbkdf2_sha1$";
        assertTrue(stored.startsWith(expectedPrefix));
        assertTrue(PasswordHasher.verify("Compat123".toCharArray(), stored));
        assertFalse(PasswordHasher.verify("Wrong123".toCharArray(), stored));
    }
}
