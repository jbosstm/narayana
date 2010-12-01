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
