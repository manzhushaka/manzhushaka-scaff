package com.manzhushaka.db.crypto;

public interface DbFieldCryptoService {
    String encrypt(String plaintext);

    String decrypt(String ciphertext);
}
