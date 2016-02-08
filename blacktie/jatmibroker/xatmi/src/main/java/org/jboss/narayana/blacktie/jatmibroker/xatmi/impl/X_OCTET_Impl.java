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

import java.util.ArrayList;
import java.util.List;

import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;

/**
 * The X_OCTET buffer is used to send byte arrays in an XATMI application
 */
public class X_OCTET_Impl extends BufferImpl implements X_OCTET {

    /**
     * The default ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Populated by the list of supported types for this buffer, in this case byte[]
     */
    private static List<Class> types = new ArrayList<Class>();

    /**
     * Populate the list
     */
    static {
        Class[] x_octetType = new Class[] { byte[].class };
        for (int i = 0; i < x_octetType.length; i++) {
            types.add(x_octetType[i]);
        }
    }

    /**
     * The constructor is hidden as it should be created from the Connection factory method.
     * 
     * @param subtype Ignored, provided for reflective ease
     * 
     * @throws ConnectionException
     * @throws ConfigurationException
     */
    public X_OCTET_Impl(String subtype) throws ConnectionException, ConfigurationException {
        super("X_OCTET", "", false, types);
    }

    /**
     * Set the data of the buffer.
     * 
     * @param bytes The data to set.
     */
    public void setByteArray(byte[] bytes) {
        super.setRawData(bytes);
        this.len = bytes.length;
    }

    /**
     * Get the content of the buffer.
     * 
     * @return The content of the buffer.
     */
    public byte[] getByteArray() {
        return super.getRawData();
    }
}
