package jpass.crypt.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CryptOutputStreamTest {

    @Test
    void write() throws IOException, GeneralSecurityException {
        @SuppressWarnings("SpellCheckingInspection")
        char[] passwd = "tpsxuc9w".toCharArray();
        var inputString = "Сара́тов — город на юго-востоке европейской части России, административный центр" +
                " Саратовской области и Саратовского района, в который не входит...";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (
                OutputStream crypt = new CryptOutputStream(output, passwd);
                Writer writer = new OutputStreamWriter(crypt, StandardCharsets.UTF_8)
        ) {
            writer.write(inputString);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        StringBuilder buf2 = new StringBuilder();
        try (
                InputStream input = new ByteArrayInputStream(output.toByteArray());
                InputStream crypt = new CryptInputStream(input, passwd);
                Reader reader = new InputStreamReader(crypt, StandardCharsets.UTF_8)
        ) {
            char[] buf1 = new char[1024];
            for (int cnt = reader.read(buf1); cnt >= 0; cnt = reader.read(buf1)) {
                buf2.append(buf1, 0, cnt);
            }
        }
        var actual = buf2.toString();
        assertEquals(inputString, actual);
    }
}