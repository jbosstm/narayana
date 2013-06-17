/* JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General public  License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General public  License for more details.
 * You should have received a copy of the GNU Lesser General public  License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.blacktie.jatmibroker.nbf;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.xerces.xs.PSVIProvider;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.xml.sax.SAXException;

public class NBFParser {
    private NBFHandlers handler;
    private SAXParser saxParser;
    private Schema schema;

    private static final Logger log = LogManager.getLogger(NBFParser.class);

    public NBFParser(String xsdFilename) throws ConfigurationException {
        try {
            // Obtain a new instance of a SAXParserFactory.
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setFeature("http://apache.org/xml/features/validation/schema", true);

            File file = new File(xsdFilename);
            if (file.exists()) {
                schema = schemaFactory.newSchema(file);
            } else {
                throw new ConfigurationException("Could not find " + xsdFilename);
            }

            factory.setSchema(schema);
            saxParser = factory.newSAXParser();
            PSVIProvider p = (PSVIProvider) saxParser.getXMLReader();

            handler = new NBFHandlers(p);

        } catch (SAXException e) {
            log.error("Could not create a SAXParser: " + e.getMessage(), e);
            throw new ConfigurationException("Could not create a SAXParser: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            log.error("Could not create a SAXParser: " + e.getMessage(), e);
            throw new ConfigurationException("Could not create a SAXParser: " + e.getMessage());
        } catch (Throwable e) {
            log.error("Could not create a SAXParser: " + e.getMessage(), e);
            throw new ConfigurationException("Could not create a SAXParser: " + e.getMessage());
        }
    }

    public void setId(String id) {
        handler.setId(id);
    }

    public void setIndex(int index) {
        handler.setIndex(index);
    }

    public String getType() {
        return handler.getType();
    }

    public String getValue() {
        return handler.getValue();
    }

    public boolean parse(byte[] buffer) throws ConfigurationException {
        boolean result = false;

        try {
            schema.newValidator().validate(new StreamSource(new ByteArrayInputStream(buffer)));
            saxParser.parse(new ByteArrayInputStream(buffer), handler);
            result = true;
        } catch (Throwable e) {
            log.error("Parser buffer failed with " + e.getMessage(), e);
            throw new ConfigurationException("Parser buffer failed with " + e.getMessage());
        }

        return result;
    }
}
