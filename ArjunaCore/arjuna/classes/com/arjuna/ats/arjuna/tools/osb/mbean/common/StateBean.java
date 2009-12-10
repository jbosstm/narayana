/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2009
 * @author Red Hat Middleware LLC.
 */
package com.arjuna.ats.arjuna.tools.osb.mbean.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.ObjectStatus;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;

/**
 * @see StateBeanMBean
 *
 * @message org.jboss.jbosstm.tools.jmx.osb.MbState.m_1
 *          [org.jboss.jbosstm.tools.jmx.osb.MbState.m_1] - Failed to unpack header: {0}.
 */
public class StateBean extends UidBean implements StateBeanMBean
{
    public static final DateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");

    protected StateManagerInfo info;

    // package private since info is not initialized
    StateBean(BasicBean parent, Uid uid)
    {
        super(parent, uid);
    }

    public StateBean(BasicBean parent, ObjectStore store, Uid uid)
    {
        this(parent, parent.getType(), store, uid);
    }

    public StateBean(BasicBean parent, String type, ObjectStore store, Uid uid)
    {
        super(parent, type, uid);
        this.info = new StateManagerInfo(store, uid, type);
    }

    // MXBean methods
    public String getCreationTime()
    {
        return info.birthDate < 0 ? "" : formatter.format(new Date(info.birthDate));
    }

    public long getAgeInSeconds()
    {
        return (info.birthDate < 0 ? -1 : ((System.currentTimeMillis()) - info.birthDate) / 1000L);
    }

    public String getUid()
    {
        return uid.stringForm();
    }

    public String getStoreRoot()
    {
        return info.getStoreRoot();
    }

    public String getType()
    {
        return type;
    }

    public String getStatus()
    {
        return ObjectStatus.toString(info.status());
    }

    public String getObjectType()
    {
        return ObjectType.toString(info.ObjectType());
    }

    class StateManagerInfo extends StateManager
    {
        String state;
        Uid txId = Uid.nullUid();
        Uid processUid = Uid.nullUid();
        long birthDate = -1;

        protected StateManagerInfo(ObjectStore os, Uid uid, String type) {
            super(uid);
            try {
                unpackHeader(os.read_committed(uid, type));
            } catch (IOException e) {
                if (tsLogger.arjLoggerI18N.isInfoEnabled())
                    tsLogger.arjLoggerI18N.info("org.jboss.jbosstm.tools.jmx.osb.MbState.m_1",
                            new Object[] { e.getMessage() });
            } catch (ObjectStoreException e) {
                if (tsLogger.arjLoggerI18N.isInfoEnabled())
                    tsLogger.arjLoggerI18N.info("org.jboss.jbosstm.tools.jmx.osb.MbState.m_1",
                            new Object[] { e.getMessage() });
            }
        }

        void unpackHeader(InputObjectState os) throws IOException
        {
            if (os != null) {
                state = os.unpackString();
                byte[] txIdBytes = os.unpackBytes();
                txId = new Uid(txIdBytes);

                if (state.equals("#ARJUNA#")) {
                    if (!txId.equals(Uid.nullUid())) {
                        byte[] pUidBytes = os.unpackBytes();
                        processUid = new Uid(pUidBytes);
                    }

                    birthDate = os.unpackLong();
                }
            }
        }

    }
}
