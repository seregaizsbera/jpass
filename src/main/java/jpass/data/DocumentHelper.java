/*
 * JPass
 *
 * Copyright (c) 2009-2017 Gabor Bata
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jpass.data;

import static jpass.util.StringUtils.stripString;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import javax.xml.bind.JAXBException;

import jpass.crypt.io.CryptInputStream;
import jpass.crypt.io.CryptOutputStream;
import jpass.xml.bind.Entries;
import jpass.xml.converter.JAXBConverter;

/**
 * Helper class for reading and writing (encrypted) XML documents.
 *
 * @author Gabor_Bata
 *
 */
@SuppressWarnings("ClassCanBeRecord")
public final class DocumentHelper {

    /**
     * File name to read/write.
     */
    private final String fileName;

    /**
     * Key for encryption.
     */
    private final char[] key;

    /**
     * Converter between JAXB objects and streams representing XMLs
     */
    private static final JAXBConverter<Entries> CONVERTER = new JAXBConverter<>(Entries.class,
            "resources/schemas/entries.xsd");

    /**
     * Creates a DocumentHelper instance.
     *
     * @param fileName file name
     * @param key key for encryption
     */
    private DocumentHelper(String fileName, char[] key) {
        this.fileName = fileName;
        this.key = key;
    }

    /**
     * Creates a document helper with no encryption.
     *
     * @param fileName file name
     * @return a new DocumentHelper object
     */
    public static DocumentHelper newInstance(String fileName) {
        return new DocumentHelper(fileName, null);
    }

    /**
     * Creates a document helper with encryption.
     *
     * @param fileName file name
     * @param key key for encryption
     * @return a new DocumentHelper object
     */
    public static DocumentHelper newInstance(String fileName, char[] key) {
        return new DocumentHelper(fileName, key);
    }

    /**
     * Reads and XML file to an {@link Entries} object.
     *
     * @return the document
     * @throws FileNotFoundException if file is not exists
     * @throws IOException when I/O error occurred
     * @throws DocumentProcessException when file format or password is incorrect
     */
    public Entries readDocument() throws IOException, DocumentProcessException {
        Entries entries;
        try (InputStream inputStream = makeInputStream()) {
            entries = CONVERTER.unmarshal(inputStream);
        } catch (JAXBException e) {
            throw new DocumentProcessException(stripString(e.getLinkedException() == null ? e.getMessage() : e
                    .getLinkedException().getMessage()));
        }
        return entries;
    }

    private InputStream makeInputStream() throws IOException {
        InputStream inputStream;
        if (this.key == null) {
            inputStream = new FileInputStream(this.fileName);
        } else {
            try {
                inputStream = new CryptInputStream(new FileInputStream(this.fileName), this.key);
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }
        return inputStream;
    }

    /**
     * Writes a document into an XML file.
     *
     * @param document the document
     * @throws DocumentProcessException when document format is incorrect
     * @throws IOException when I/O error occurred
     */
    public void writeDocument(final Entries document) throws DocumentProcessException, IOException {
        try (OutputStream outputStream = makeOutputStream()) {
            CONVERTER.marshal(document, outputStream, this.key == null);
        } catch (JAXBException e) {
            throw new DocumentProcessException(stripString(e.getLinkedException() == null ? e.getMessage() : e
                    .getLinkedException().getMessage()));
        } catch (RuntimeException e) {
            throw new DocumentProcessException(e.getMessage());
        }
    }

    private OutputStream makeOutputStream() throws IOException {
        OutputStream outputStream;
        if (this.key == null) {
            outputStream = new FileOutputStream(this.fileName);
        } else {
            try {
                outputStream = new CryptOutputStream(new FileOutputStream(this.fileName), this.key);
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }
        return outputStream;
    }
}
