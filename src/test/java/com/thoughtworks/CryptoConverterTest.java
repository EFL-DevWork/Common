package com.thoughtworks;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CryptoConverterTest {


    CryptoConverter converter = new CryptoConverter("3s6v9y$B&E)H@McQfTjWnZq4t7w!z%C*", "AES");

    @Test
    void invalidAlgorithm() {
        Assertions.assertThatExceptionOfType(RuntimeException.class).as("invalid algorithm for encryption").isThrownBy(() -> {
            new CryptoConverter("3s6v9y$B&E)H@McQfTjWnZq4t7w!z%C*", "unknown value").encrypt("test-data");
        });
    }

    @Test
    void shouldEncryptAndDecryptData() {
        String data = "testdata";
        String encryptedData = converter.encrypt(data);
        Assertions.assertThat(data).isEqualTo(converter.decrypt(encryptedData));
    }

    @Test
    void encryptFail(){
        Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(()-> {
            converter.encrypt(null);
        });
    }

    @Test
    void decryptFail() {
        Assertions.assertThatExceptionOfType(RuntimeException.class).as("invalid encrypted data while decrypting").isThrownBy(() -> {
            converter.decrypt("SyHlXvtleuLjasd1nroKa+SA==");
        });
    }

    @Test
    void hash() {
        Assertions.assertThat(converter.getbCryptPasswordEncoder().matches("testdata", converter.hash("testdata"))).isTrue();
    }

    @Test
    void getbCryptPasswordEncoder() {
        Assertions.assertThat(converter.getbCryptPasswordEncoder()).isNotNull();
    }

}