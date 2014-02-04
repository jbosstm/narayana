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

import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class SaxHandler extends DefaultHandler {

    private final Properties properties;

    private boolean isEntry;

    private String currentKey;

    private StringBuffer currentBuffer;

    public SaxHandler() {
        properties = new Properties();
        isEntry = false;
        currentKey = null;
        currentBuffer = null;
    }

    /**
     * Returns copy of the properties read from the handled XML.
     *
     * @return Properties read from the handled XML.
     */
    public Properties getProperties() {
        Properties propertiesCopy = new Properties();
        propertiesCopy.putAll(properties);

        return propertiesCopy;
    }

    /**
     * Handles open tag, if its name is <code>entry</code> and has attribute <code>key</code>.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.toLowerCase().equals("entry") && attributes.getLength() > 0) {
            currentKey = getAttributeValue("key", attributes);
            if (currentKey != null) {
                isEntry = true;
                currentBuffer = new StringBuffer();
            }
        }
    }

    /**
     * Handles close tag, if its name is <code>entry</code>.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.toLowerCase().equals("entry") && isEntry) {
            properties.put(currentKey, currentBuffer.toString().trim());
            currentKey = null;
            currentBuffer = null;
            isEntry = false;
        }
    }

    /**
     * Handles text content of the <code>entry</code> tag.
     */
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        if (isEntry) {
            currentBuffer.append(ch, start, length);
        }
    }

    /**
     * Returns attribute's value from the <code>attributes</code> container based on the <code>name</code>.
     *
     * @param name
     * @param attributes
     * @return String value of the attribute if such attribute exists and null otherwise.
     */
    private String getAttributeValue(final String name, final Attributes attributes) {
        for (int i = 0; i < attributes.getLength(); i++) {
            if (attributes.getQName(i).toLowerCase().equals(name.toLowerCase())) {
                return attributes.getValue(i);
            }
        }

        return null;
    }

}
