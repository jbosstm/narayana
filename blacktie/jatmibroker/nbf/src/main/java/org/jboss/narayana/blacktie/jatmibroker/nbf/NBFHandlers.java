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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.PSVIProvider;
import org.apache.xerces.xs.XSTypeDefinition;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NBFHandlers extends DefaultHandler {
    private static final Logger log = LogManager.getLogger(NBFHandlers.class);

    private PSVIProvider provider;
    private String id;
    private String type;
    private String value;
    private String other_value;
    private int index;
    private int curIndex;
    private boolean found;

    public NBFHandlers(PSVIProvider provider) {
        this.provider = provider;
        curIndex = 0;
        value = null;
        found = false;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        if (type.endsWith("_type")) {
            return other_value;
        } else {
            return value;
        }
    }

    public void setId(String id) {
        this.id = id;
        this.value = null;
    }

    public void setIndex(int index) {
        this.curIndex = 0;
        this.index = index;
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if (found) {
            String strValue = new String(ch, start, length);
            if (value == null) {
                value = strValue;
            } else {
                value += strValue;
            }

            log.debug("index = " + index + " curIndex = " + curIndex + " value = " + value);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (qName.equals(id)) {
            if (index == curIndex) {
                found = true;
            }
            curIndex++;

            ElementPSVI psvi = provider.getElementPSVI();
            if (psvi != null) {
                XSTypeDefinition typeInfo = psvi.getTypeDefinition();

                while (typeInfo != null) {
                    String typeName = typeInfo.getName();
                    if (typeName != null
                            && (typeName.equals("long") || typeName.equals("string") || typeName.equals("integer")
                                    || typeName.equals("float") || typeName.endsWith("_type"))) {

                        type = typeName;
                        log.debug(qName + " has type of " + type);

                        break;
                    }
                    typeInfo = typeInfo.getBaseType();
                }
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals(id)) {
            found = false;
        }

        if (found && type != null && type.endsWith("_type") && value != null) {
            String tmp = "<" + qName + ">" + value + "</" + qName + ">";
            if (other_value == null) {
                other_value = tmp;
            } else {
                other_value += tmp;
            }
            value = null;
        }
    }
}
