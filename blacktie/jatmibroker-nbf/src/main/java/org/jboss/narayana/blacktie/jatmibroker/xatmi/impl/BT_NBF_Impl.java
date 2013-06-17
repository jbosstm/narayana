/*
 * JBoss, Home of Professional Open Source
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
package org.jboss.narayana.blacktie.jatmibroker.xatmi.impl;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.nbf.NBFParser;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.BT_NBF;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;

public class BT_NBF_Impl extends BufferImpl implements BT_NBF {

    private static final Logger log = LogManager.getLogger(BT_NBF_Impl.class);
    /**
     * The default ID
     */
    private static final long serialVersionUID = 1L;
    private NBFParser parser;
    private String rootElement;

    public BT_NBF_Impl(String subtype) throws org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException, ConnectionException {
        super("BT_NBF", subtype, false, null);

        rootElement = "</" + subtype + ">";
        String xsd = "buffers/" + subtype + ".xsd";
        File file = new File(xsd);
        if (!file.exists()) {
            throw new org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException("can not find " + xsd);
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version='1.0'?>");
        buffer.append("<");
        buffer.append(subtype);
        buffer.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        buffer.append(" xmlns=\"http://www.jboss.org/blacktie\"");
        buffer.append(" xsi:schemaLocation=\"http://www.jboss.org/blacktie buffers/");
        buffer.append(subtype);
        buffer.append(".xsd\">");
        buffer.append("</");
        buffer.append(subtype);
        buffer.append(">");

        setRawData(buffer.toString().getBytes());
        parser = new NBFParser(xsd);
        parser.parse(getRawData());
    }

    private String insertString(String buffer, String attr) {
        int k = buffer.indexOf(rootElement);
        return buffer.substring(0, k) + attr + buffer.substring(k);
    }

    public boolean btaddattribute(String attrId, Object attrValue) {
        boolean rc = false;
        try {
            parser.setId(attrId);
            String buffer = new String(getRawData());
            String attr = "<" + attrId + "></" + attrId + ">";

            String newbuffer = insertString(buffer, attr);
            rc = parser.parse(newbuffer.getBytes());

            if (rc) {
                String type = parser.getType();
                StringBuffer buf = new StringBuffer();
                if (type.equals("long")) {
                    buf.append((Long) attrValue);
                } else if (type.equals("string")) {
                    buf.append((String) attrValue);
                } else if (type.equals("integer")) {
                    buf.append((Integer) attrValue);
                } else if (type.equals("short")) {
                    buf.append((Short) attrValue);
                } else if (type.equals("float")) {
                    buf.append((Float) attrValue);
                } else if (type.endsWith("_type")) {
                    String nbf = new String(((BT_NBF_Impl) attrValue).getRawData());
                    int k = nbf.indexOf(".xsd\">");
                    int size = nbf.length();
                    String test = nbf.substring(k + 6, size - attrId.length() - 3);
                    buf.append(test);
                } else {
                    log.error("Can not support type " + type);
                    rc = false;
                }

                if (buf.length() > 0) {
                    String newattr = "<" + attrId + ">" + buf + "</" + attrId + ">";
                    String attrbuf = insertString(buffer, newattr);

                    rc = parser.parse(attrbuf.getBytes());
                    if (rc) {
                        setRawData(attrbuf.getBytes());
                    }
                }
            }
        } catch (ClassCastException e) {
            rc = false;
            log.warn("type is " + parser.getType() + " but attrValue type is " + attrValue.getClass().getName());
        } catch (Throwable e) {
            log.error("btaddattribute failed with " + e.getMessage());
        }
        return rc;
    }

    public Object btgetattribute(String attrId, int index) {
        Object toReturn = null;

        try {
            boolean rc = false;
            parser.setId(attrId);
            parser.setIndex(index);
            rc = parser.parse(getRawData());

            if (rc) {
                String type = parser.getType();
                String value = parser.getValue();

                log.debug("vlaue is " + value);
                if (value == null) {
                    log.warn("can not find " + attrId + " at index " + index);
                } else {
                    String tmp = new String(getRawData());
                    int pos = find_element_string(tmp, attrId, index, false);

                    if (pos == -1) {
                        log.warn(attrId + " at index " + index + " has been deleted");
                        return null;
                    }

                    if (type.equals("long")) {
                        toReturn = Long.parseLong(value);
                    } else if (type.equals("string")) {
                        toReturn = value;
                    } else if (type.equals("integer")) {
                        toReturn = Integer.parseInt(value);
                    } else if (type.equals("short")) {
                        toReturn = Short.parseShort(value);
                    } else if (type.equals("float")) {
                        toReturn = Float.parseFloat(value);
                    } else if (type.endsWith("_type")) {
                        // log.info(value);
                        toReturn = new BT_NBF_Impl(attrId);
                        String nbf = new String(((BT_NBF_Impl) toReturn).getRawData());
                        int k = nbf.indexOf(".xsd\">") + 6;
                        int size = nbf.length();

                        String buf = nbf.substring(0, k) + value + nbf.substring(k, size);
                        // log.info(buf);
                        ((BT_NBF_Impl) toReturn).setRawData(buf.getBytes());

                    } else {
                        log.error("Can not support type " + type);
                    }
                }
            }
        } catch (org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException e) {
            log.error("ConfigurationException: btgetattribute failed with " + e.getMessage());
        } catch (ConnectionException e) {
            log.error("ConnectionException: btgetattribute failed with " + e.getMessage());
        }

        return toReturn;
    }

    public boolean btdelattribute(String attrId, int index) {
        boolean toReturn = false;

        try {
            boolean rc;
            parser.setId(attrId);
            parser.setIndex(index);
            rc = parser.parse(getRawData());

            if (rc) {
                String value = parser.getValue();
                if (value == null) {
                    log.warn("can not find " + attrId + " at index " + index);
                } else {
                    String buf = new String(getRawData());
                    int pos = find_element_string(buf, attrId, index, false);

                    if (pos > 0) {
                        int size = buf.length();
                        buf = buf.substring(0, pos + attrId.length() + 2)
                                + buf.substring(pos + attrId.length() + 2 + value.length(), size);
                        log.debug(buf);
                        setRawData(buf.getBytes());
                        toReturn = true;
                    }
                }
            }
        } catch (org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException e) {
            log.error("btdelattribute failed with " + e.getMessage());
        }
        return toReturn;
    }

    public boolean btsetattribute(String attrId, int index, Object newValue) {
        boolean rc = false;

        try {
            parser.setId(attrId);
            parser.setIndex(index);
            rc = parser.parse(getRawData());

            String value = parser.getValue();
            String buf = new String(getRawData());

            int pos = find_element_string(buf, attrId, index, true);

            if (value == null && pos == -1) {
                log.warn("can not find " + attrId + " at index " + index);
                rc = false;
            } else {
                String type = parser.getType();
                StringBuffer tmp = new StringBuffer();
                if (type.equals("long")) {
                    tmp.append((Long) newValue);
                } else if (type.equals("string")) {
                    tmp.append((String) newValue);
                } else if (type.equals("integer")) {
                    tmp.append((Integer) newValue);
                } else if (type.equals("short")) {
                    tmp.append((Short) newValue);
                } else if (type.equals("float")) {
                    tmp.append((Float) newValue);
                } else if (type.endsWith("_type")) {
                    String nbf = new String(((BT_NBF_Impl) newValue).getRawData());
                    int k = nbf.indexOf(".xsd\">");
                    int size = nbf.length();
                    String test = nbf.substring(k + 6, size - attrId.length() - 3);
                    tmp.append(test);
                } else {
                    log.error("Can not support type " + type);
                    rc = false;
                }

                if (rc) {
                    int size = buf.length();
                    char ch = buf.charAt(pos + attrId.length() + 3);
                    int length;
                    if (ch == '/') {
                        length = 0;
                    } else {
                        length = value.length();
                    }
                    buf = buf.substring(0, pos + attrId.length() + 2) + tmp.toString()
                            + buf.substring(pos + attrId.length() + 2 + length, size);
                    // log.info(buf.substring(0, pos + attrId.length() + 2));
                    // log.info(tmp);
                    // log.info(buf.substring(pos + attrId.length() + 2 + length, size));
                    setRawData(buf.getBytes());
                }
            }
        } catch (org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException e) {
            log.error("btsetattribute failed with " + e.getMessage());
            rc = false;
        } catch (ClassCastException e) {
            rc = false;
            log.warn("type is " + parser.getType() + " but newValue type is " + newValue.getClass().getName());
        }

        return rc;
    }

    private int find_element_string(String buf, String id, int index, boolean isset) {
        int pos = -1;

        String element = "<" + id + ">";
        int fromIndex = 0;
        for (int i = 0; i <= index; i++) {
            pos = buf.indexOf(element, fromIndex);
            if (pos > 0) {
                fromIndex = pos + element.length();
            }
        }

        char ch = buf.charAt(pos + id.length() + 3);
        if (isset == false && ch == '/') {
            pos = -1;
        }

        return pos;
    }

    public int getLen() {
        return this.getRawData().length;
    }

    public String toString() {
        return new String(getRawData());
    }
}
