/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.arjuna.tools.osb.api.mbeans;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import java.io.Serializable;

/**
 * Wrapper for ObjectInputState and ObjectOutputState to facilitate use in JMX invocations
 */
public class ObjectStateWrapper implements Serializable {
    private static final long serialVersionUID = 323923953274907077L;

    private Uid newUid;
    private String tName;
    private byte[] buff;
    private boolean valid = true;

    public ObjectStateWrapper(OutputObjectState oos) {
        this();

        if (oos != null)
            init(oos.stateUid(), oos.type(), oos.buffer());
    }

    public ObjectStateWrapper(InputObjectState ios) {
        this();

        if (ios != null)
            init(ios.stateUid(), ios.type(), ios.buffer());
    }

    public ObjectStateWrapper(InputObjectState ios, boolean ok) {
        this(ios);
        setValid(ok);
    }

    public ObjectStateWrapper() {
        init(Uid.nullUid(), "", new byte[0]);
    }

    private void init(Uid u, String t, byte[] b) {
        this.newUid = u;
        this.tName = t;
        this.buff = b;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public InputObjectState getIOS() {
        if (getBuff() == null || getBuff().length == 0)
            return null;

        Uid u = getNewUid() == null ? Uid.nullUid() : getNewUid();
        String t = gettName() == null ? "" : gettName();
        byte[] b = getBuff() == null ? new byte[0] : getBuff();
        return new InputObjectState(u, t, b);
    }
    
    public OutputObjectState getOOS() {
        if (getBuff() == null || getBuff().length == 0)
            return null;

        Uid u = getNewUid() == null ? Uid.nullUid() : getNewUid();
        String t = gettName() == null ? "" : gettName();
        byte[] b = getBuff() == null ? new byte[0] : getBuff();
        return new OutputObjectState(u, t, b);
    }

    public Uid getNewUid() {
        return newUid;
    }

    public String gettName() {
        return tName;
    }

    public byte[] getBuff() {
        return buff;
    }
}
