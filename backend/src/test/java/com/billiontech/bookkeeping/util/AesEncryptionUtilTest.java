package com.billiontech.bookkeeping.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AesEncryptionUtilTest {

    // 32-character key for AES-256
    private static final String KEY = "01234567890123456789012345678901";

    @Test
    void encryptAndDecrypt_shouldRecoverOriginalValue() {
        String original = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.sample-qbo-access-token";
        String encrypted = AesEncryptionUtil.encrypt(original, KEY);
        String decrypted = AesEncryptionUtil.decrypt(encrypted, KEY);
        assertEquals(original, decrypted);
    }

    @Test
    void encrypt_shouldProduceDifferentCiphertextEachTime() {
        String original = "same-plaintext-value";
        String encrypted1 = AesEncryptionUtil.encrypt(original, KEY);
        String encrypted2 = AesEncryptionUtil.encrypt(original, KEY);

        // GCM uses a random IV so identical plaintext produces different ciphertext
        assertNotEquals(encrypted1, encrypted2);

        // Both should decrypt to the original
        assertEquals(original, AesEncryptionUtil.decrypt(encrypted1, KEY));
        assertEquals(original, AesEncryptionUtil.decrypt(encrypted2, KEY));
    }

    @Test
    void decrypt_withWrongKey_shouldThrow() {
        String encrypted = AesEncryptionUtil.encrypt("secret-token", KEY);
        String wrongKey = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

        assertThrows(AesEncryptionUtil.EncryptionException.class,
                () -> AesEncryptionUtil.decrypt(encrypted, wrongKey));
    }

    @Test
    void encryptAndDecrypt_emptyString_shouldWork() {
        String encrypted = AesEncryptionUtil.encrypt("", KEY);
        assertEquals("", AesEncryptionUtil.decrypt(encrypted, KEY));
    }

    @Test
    void encryptAndDecrypt_longToken_shouldWork() {
        String longToken = "A".repeat(2048);
        String encrypted = AesEncryptionUtil.encrypt(longToken, KEY);
        assertEquals(longToken, AesEncryptionUtil.decrypt(encrypted, KEY));
    }
}
