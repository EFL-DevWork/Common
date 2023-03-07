package com.thoughtworks;

import org.keycloak.common.util.Base64;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class CryptoConverter {

    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    public static final int GCM_TAG_LENGTH = 16;
    public static final int GCM_IV_LENGTH = 12;
    private final String keyString;
    private final String keySpecAlgorithm;
    private final SecureRandom secureRandom;

    public CryptoConverter(String keyString, String keySpecAlgorithm) {
        this.keyString = keyString;
        this.keySpecAlgorithm = keySpecAlgorithm;
        bCryptPasswordEncoder = new BCryptPasswordEncoder();
        secureRandom = new SecureRandom();
    }

    public String encrypt(String attribute) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            SecretKey secretKey = generateSecretKey(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            //Encrypt the data
            byte [] encryptedData = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            //Concatenate everything and return the final data
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + encryptedData.length);
            byteBuffer.putInt(iv.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);
            return Base64.encodeBytes(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String encryptedAttribute) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.decode(encryptedAttribute));
            int noonceSize = byteBuffer.getInt();
            if (noonceSize < GCM_IV_LENGTH || noonceSize >= GCM_TAG_LENGTH) {
                throw new IllegalArgumentException("Nonce size is incorrect. Make sure that the incoming data is an AES encrypted file.");
            }
            byte[] iv = new byte[noonceSize];
            byteBuffer.get(iv);
            SecretKey secretKey = generateSecretKey(iv);
            byte[] cipherBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SecretKey generateSecretKey(byte [] iv) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(keyString.toCharArray(), iv, 65536, 128); // AES-128
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] key = secretKeyFactory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, keySpecAlgorithm);
    }

    public String hash(String data) {
        return bCryptPasswordEncoder.encode(data);
    }

    public BCryptPasswordEncoder getbCryptPasswordEncoder() {
        return bCryptPasswordEncoder;
    }
}
