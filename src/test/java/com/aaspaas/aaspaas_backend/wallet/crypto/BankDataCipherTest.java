package com.aaspaas.aaspaas_backend.wallet.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class BankDataCipherTest {

    private BankDataCipher cipher;

    @BeforeEach
    void setUp() {
        cipher = new BankDataCipher();
        byte[] key = new byte[32];
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) (i + 1);
        }
        ReflectionTestUtils.setField(cipher, "base64Key", Base64.getEncoder().encodeToString(key));
        cipher.init();
    }

    @Test
    void encryptThenDecryptReturnsOriginal() {
        String enc = cipher.encrypt("123456789012");
        assertNotEquals("123456789012", enc);
        assertEquals("123456789012", cipher.decrypt(enc));
    }

    @Test
    void sameInputGivesDifferentCiphertext() {
        assertNotEquals(cipher.encrypt("123456789012"), cipher.encrypt("123456789012"));
    }

    @Test
    void fingerprintIsStableAndDoesNotRevealNumber() {
        String f1 = cipher.fingerprint("123456789012");
        assertEquals(f1, cipher.fingerprint("123456789012"));
        assertNotEquals(f1, cipher.fingerprint("123456789013"));
        assertFalse(f1.contains("123456789012"));
    }

    @Test
    void tamperedCiphertextIsRejected() {
        byte[] raw = Base64.getDecoder().decode(cipher.encrypt("123456789012"));
        raw[raw.length - 1] ^= 0x01;
        String tampered = Base64.getEncoder().encodeToString(raw);
        assertThrows(IllegalStateException.class, () -> cipher.decrypt(tampered));
    }

    @Test
    void missingOrShortKeyFailsFast() {
        BankDataCipher bad = new BankDataCipher();
        ReflectionTestUtils.setField(bad, "base64Key", "");
        assertThrows(IllegalStateException.class, bad::init);

        BankDataCipher shortKey = new BankDataCipher();
        ReflectionTestUtils.setField(shortKey, "base64Key",
                Base64.getEncoder().encodeToString(new byte[16]));
        assertThrows(IllegalStateException.class, shortKey::init);
    }
}
