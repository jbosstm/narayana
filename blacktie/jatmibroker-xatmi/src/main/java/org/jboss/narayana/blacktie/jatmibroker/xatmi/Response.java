/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.blacktie.jatmibroker.xatmi;

import java.io.Serializable;

import org.jboss.narayana.blacktie.jatmibroker.xatmi.impl.ConnectionImpl;

/**
 * This class encapsulates the response from the remote service and the return
 * code
 */
public class Response implements Serializable {

    /**
     * A non-default serialized id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The connection descriptor that the response was received for.
     */
    private int cd;

    /**
     * The return value
     */
    private short rval;

    /**
     * The return code
     */
    private int rcode;

    /**
     * The flags to return
     */
    private int flags;

    /**
     * The response from the server
     */
    private Buffer buffer;

    /**
     * Services construct their responses using this constructor.
     * 
     * @param rval The value the service wishes to use.
     * @param rcode The code the service wants to respond with.
     * @param buffer The buffer to return.
     * @param flags The flags to respond with.
     */
    public Response(short rval, int rcode, Buffer buffer, int flags) {
        this.rval = rval;
        this.rcode = rcode;
        this.buffer = buffer;
        this.flags = flags;
    }

    /**
     * When a client receives a response this is the method that is used by the core framework to assemble the response.
     * 
     * @param cd The connection that actually received the response (may be different to expected if {@link ConnectionImpl#TPGETANY}
     *        was used.
     * @param rval The return value.
     * @param rcode The return code.
     * @param buffer The buffer response.
     * @param flags The flags the service used.
     */
    public Response(int cd, short rval, int rcode, Buffer buffer, int flags) {
        this.cd = cd;
        this.rval = rval;
        this.rcode = rcode;
        this.buffer = buffer;
        this.flags = flags;
    }

    /**
     * Get the return value
     * 
     * @return The return value
     */
    public short getRval() {
        return rval;
    }

    /**
     * Get the return code
     * 
     * @return The return code
     */
    public int getRcode() {
        return rcode;
    }

    /**
     * Get the flags that the service responded with.
     * 
     * @return The flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Get the buffer that the service responded with.
     * 
     * @return The buffer
     */
    public Buffer getBuffer() {
        return buffer;
    }

    /**
     * Get the cd of the service that responded (may vary based on <code>Connection#TPGETANY</code>)
     * 
     * @return The connection descriptor
     */
    public int getCd() {
        return cd;
    }
}
