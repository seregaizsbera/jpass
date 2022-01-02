package jpass.crypt.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptOutputStream extends OutputStream {
    private final OutputStream output;

    public CryptOutputStream(OutputStream out, char[] pass) throws GeneralSecurityException, IOException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[CipherSpecifications.SALT_SIZE_BYTES];
        random.nextBytes(salt);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(CipherSpecifications.KEY_GENERATION_ALGORITHM);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(pass, salt, CipherSpecifications.KEY_GENERATION_ITERATION_COUNT, CipherSpecifications.KEY_SIZE_BITS);
        SecretKey key = secretKeyFactory.generateSecret(pbeKeySpec);
        SecretKeySpec secretKey = new SecretKeySpec(key.getEncoded(), CipherSpecifications.KEY_ALGORITHM);
        byte[] iv = new byte[CipherSpecifications.IV_LENGTH_BYTES];
        random.nextBytes(iv);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(CipherSpecifications.AUTHENTICATION_TAG_LENGTH_BITS, iv);
        Cipher cipher = Cipher.getInstance(CipherSpecifications.CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
        out.write(CipherSpecifications.FILE_HEADER);
        out.write(salt);
        out.write(iv);
        this.output = new GZIPOutputStream(new CipherOutputStream(out, cipher));
    }

    @Override
    public void write(int b) throws IOException {
        output.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        output.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        output.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }

    @Override
    public void close() throws IOException {
        output.close();
    }
    
    public static void main(String[] args) {
        try {
            Path file = Files.createTempFile("ciphered-", ".txt");
            @SuppressWarnings("SpellCheckingInspection")
            char[] passwd = "tpsxuc9w".toCharArray();
            try (
                OutputStream output = Files.newOutputStream(file);
                OutputStream crypt = new CryptOutputStream(output, passwd);
                Writer writer = new OutputStreamWriter(crypt, StandardCharsets.UTF_8);
                PrintWriter out = new PrintWriter(writer)
            ) {
                out.printf("%s%n", "Сара́тов — город на юго-востоке европейской части России, административный центр" +
                        " Саратовской области и Саратовского района, в который не входит...");
            }
            try (
                InputStream input = Files.newInputStream(file);
                InputStream crypt = new CryptInputStream(input, passwd);
                Reader reader = new InputStreamReader(crypt, StandardCharsets.UTF_8);
                BufferedReader in = new BufferedReader(reader)
            ) {
                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    System.out.printf("%s", line);
                }
            }
            Files.deleteIfExists(file);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
