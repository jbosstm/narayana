/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.common.util.propertyservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class PropertiesFactorySax extends AbstractPropertiesFactory {

    /**
     * Reads XML provided in <code>is</code> input stream and looks for property entries.
     * All properties are placed to the properties container <code>p</code>.
     *
     * Allways returns null and puts all properties to the <code>p</code>.
     */
    @Override
    protected Properties loadFromXML(final Properties p, final InputStream is) throws IOException {
        try {
            final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            XMLReader xrdr = parser.getXMLReader();
            xrdr.setFeature("http://xml.org/sax/features/validation", false);
            xrdr.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            final SaxHandler handler = new SaxHandler();

            parser.parse(is, handler);
            p.putAll(handler.getProperties());

        } catch (ParserConfigurationException e) {
            throw new IOException("Could not read xml", e);
        } catch (SAXException e) {
            throw new IOException("Could not read xml", e);
        }

        return null;
    }

}