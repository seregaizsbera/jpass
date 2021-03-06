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
package jpass.xml.converter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class for conversion between JAXB objects and streams representing XMLs.
 *
 * @author Gabor_Bata
 *
 * @param <T> the type of object to marshal/unmarshal
 */
@SuppressWarnings("ClassCanBeRecord")
public class JAXBConverter<T> {
    private final Class<T> documentClass;

    public JAXBConverter(Class<T> documentClass) {
        this.documentClass = documentClass;
    }

    /**
     * Marshals the given object to the given output stream.
     *
     * @param document the JAXB object which represents the XML document
     * @param outputStream the output stream
     * @param formattedOutput formatting of the output XML
     * @throws JAXBException if any error occurred
     */
    public void marshal(T document, OutputStream outputStream, boolean formattedOutput) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(this.documentClass);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.marshal(document, outputStream);
    }

    /**
     * Unmarshalls the given input stream to a JAXB generated class.
     *
     * @param inputStream the input stream
     * @return the JAXB generated object
     * @throws JAXBException if any error occurred
     */
    @SuppressWarnings("unchecked")
    public T unmarshal(InputStream inputStream) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(this.documentClass);
        Unmarshaller u = jc.createUnmarshaller();
        return (T) u.unmarshal(inputStream);
    }
}
