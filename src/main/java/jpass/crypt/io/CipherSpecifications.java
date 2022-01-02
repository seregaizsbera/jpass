package jpass.crypt.io;

public final class CipherSpecifications {
    
    public static final int IV_LENGTH_BYTES = 12;

    public static final int AUTHENTICATION_TAG_LENGTH_BITS = 128;

    public static final String CIPHER = "AES/GCM/NoPadding";
    
    public static final int KEY_SIZE_BITS = 256;
    
    public static final String KEY_GENERATION_ALGORITHM = "PBKDF2WithHmacSHA256";

    public static final String KEY_ALGORITHM = "AES";

    public static final int KEY_GENERATION_ITERATION_COUNT = 65535;
    
    public static final int SALT_SIZE_BYTES = 8;
    
    static final byte[] FILE_HEADER = {0x23, 0x57, 0x79, (byte) 0xCF};

    private CipherSpecifications() {
        throw new AssertionError("Class CipherSpecifications can't be instantiated");
    }
}
