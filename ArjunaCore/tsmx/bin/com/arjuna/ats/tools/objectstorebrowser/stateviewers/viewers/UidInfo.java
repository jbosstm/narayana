/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.OutputBuffer;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.tools.objectstorebrowser.UidConverter;

import javax.transaction.xa.Xid;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;

/**
 * Base class for representing common state relevant to Uid's and Xid's
 */
public class UidInfo
{
    private static DateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");
    private static UidConverter uidConverter;

    public static void setUidConverter(UidConverter uidConverter)
    {
        UidInfo.uidConverter = uidConverter;
    }

    private Uid uid;
    private String instanceName;
    private Long creationTime;

    public UidInfo(Uid uid, String instanceName)
    {
        this.uid = uid;
        this.instanceName = instanceName;
    }

    public Uid getUid()
    {
        return uid;
    }

    public String getInstanceName()
    {
        return instanceName;
    }

    public long getCreationTime()
    {
        if (creationTime == null)
            creationTime = getCreationTime(uid);

        return creationTime;
    }

    public long getAge()
    {
        return (getCreationTime() < 0 ? -1 : (System.currentTimeMillis() / 1000) - getCreationTime());
    }

    // static utility methods
    public static String formatTime(long seconds)
    {
        return seconds < 0 ? "" : formatter.format(new Date(seconds * 1000L));
    }

    public static Uid toUid(Xid xid)
    {
        return uidConverter.toUid(xid);
    }

    public static long getCreationTime(Xid xid)
    {
        return getCreationTime(toUid(xid));
    }

    public static long getCreationTime(Uid uid)
    {
        if (uid != null)
        {
            try
            {
                OutputBuffer outBuf = new OutputBuffer();
                InputBuffer inBuf;

                UidHelper.packInto(uid, outBuf);
                inBuf = new InputBuffer(outBuf.buffer());

                //host = inet4AddressToString(inBuf.unpackInt());
                inBuf.unpackInt();
                inBuf.unpackInt(); // process

                return inBuf.unpackInt();
//            int other = inBuf.unpackInt();
            }
            catch (IOException e)
            {
            }
        }

        return -1;
    }

    private static String inet4AddressToString(int ip)
    {
        StringBuffer sb = new StringBuffer(15);

        for (int shift=24; shift > 0; shift -= 8)
        {
            sb.append( Integer.toString((ip >>> shift) & 0xff)).append('.');
        }

        return sb.append( Integer.toString(ip & 0xff)).toString();
    }
}
