package jpass.data;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class DocumentHelperTest {

    @Test
    void newInstance() throws IOException, DocumentProcessException {
        var dh1 = DocumentHelper.newInstance("src/test/resources/test.jpass", "1234".toCharArray());
        assertNotNull(dh1);
        var document1 = dh1.readDocument();
        assertEquals(2, document1.getEntry().size());
        var f = Files.createTempFile("unit-test-", ".jpass");
        var dh2 = DocumentHelper.newInstance(f.toString(), "4321".toCharArray());
        dh2.writeDocument(document1);
        var dh3 = DocumentHelper.newInstance(f.toString(), "4321".toCharArray());
        var document2 = dh3.readDocument();
        Files.deleteIfExists(f);
        assertEquals(document1.getEntry().size(), document2.getEntry().size());
    }
}