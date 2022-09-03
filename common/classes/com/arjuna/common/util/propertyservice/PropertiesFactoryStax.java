/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.arjuna.common.util.propertyservice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public class PropertiesFactoryStax extends AbstractPropertiesFactory {

    @Override
    protected Properties loadFromXML(Properties p, InputStream is) throws IOException {
        try {
            final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            inputFactory.setXMLResolver(new XMLResolver() {
                @Override
                public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace)
                        throws XMLStreamException {
                    return new ByteArrayInputStream(new byte[0]);
                }
            });
            XMLStreamReader parser = inputFactory.createXMLStreamReader(is);
            /*
             * xml looks like this <entry key="CoreEnvironmentBean.nodeIdentifier">1</entry>
             */
            int event = -1;
            while (true) {
                if (event == XMLStreamConstants.END_DOCUMENT) {
                    parser.close();
                    break;
                }
                if (event == XMLStreamConstants.START_ELEMENT && parser.getAttributeCount() > 0) {
                    String key = parser.getAttributeValue(0);
                    StringBuffer buffer = new StringBuffer();
                    event = parser.next();
                    for (; event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.COMMENT; event = parser.next()) {
                        if (event != XMLStreamConstants.COMMENT) {
                            String nextText = parser.getText();
                            buffer.append(nextText);
                        }
                    }
                    if (key != null) {
                        String value = buffer.toString();
                        p.put(key, value);
                    }
                } else {
                    event = parser.next();
                }
            }
        } catch (XMLStreamException e) {
            throw new IOException("Could not read xml", e);
        }
        return null;
    }

}
