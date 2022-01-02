package jpass.crypt.io;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptInputStream extends InputStream {
    private final InputStream input;

    @SuppressWarnings({"java:S2674", "ResultOfMethodCallIgnored"})
    public CryptInputStream(InputStream in, char[] pass) throws GeneralSecurityException, IOException {
        in.skip(CipherSpecifications.FILE_HEADER.length);
        byte[] salt = new byte[CipherSpecifications.SALT_SIZE_BYTES];
        in.read(salt);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(CipherSpecifications.KEY_GENERATION_ALGORITHM);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(pass, salt, CipherSpecifications.KEY_GENERATION_ITERATION_COUNT, CipherSpecifications.KEY_SIZE_BITS);
        SecretKey key = secretKeyFactory.generateSecret(pbeKeySpec);
        SecretKeySpec secretKey = new SecretKeySpec(key.getEncoded(), CipherSpecifications.KEY_ALGORITHM);
        byte[] iv = new byte[CipherSpecifications.IV_LENGTH_BYTES];
        in.read(iv);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(CipherSpecifications.AUTHENTICATION_TAG_LENGTH_BITS, iv);
        Cipher cipher = Cipher.getInstance(CipherSpecifications.CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
        this.input = new GZIPInputStream(new CipherInputStream(in, cipher));
    }

    @Override
    public int read() throws IOException {
        return input.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return input.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return input.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return input.skip(n);
    }

    @Override
    public int available() throws IOException {
        return input.available();
    }

    @Override
    public void close() throws IOException {
        input.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        input.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        input.reset();
    }

    @Override
    public boolean markSupported() {
        return input.markSupported();
    }
}
