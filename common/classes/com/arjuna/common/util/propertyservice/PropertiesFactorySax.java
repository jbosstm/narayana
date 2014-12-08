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
