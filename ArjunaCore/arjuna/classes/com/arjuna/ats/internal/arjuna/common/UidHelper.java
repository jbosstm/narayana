/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: CadaverRecordSetup.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.common;

import java.io.IOException;
import java.security.InvalidParameterException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.arjuna.state.OutputBuffer;

public class UidHelper
{
    public static final Uid unpackFrom (InputBuffer buff) throws IOException
    {
        long[] hostAddr = new long[2];
        int process;
        int sec;
        int other;

        hostAddr[0] = buff.unpackLong();
        hostAddr[1] = buff.unpackLong();
        process = buff.unpackInt();
        sec = buff.unpackInt();
        other = buff.unpackInt();

        return new Uid(hostAddr, process, sec, other);
    }

    public static final void packInto (Uid u, OutputBuffer buff)
            throws IOException
    {
        if (u.valid())
        {
            UidHelper foo = new UidHelper();
            UidFriend friend = foo.new UidFriend(u);
            long[] hostAddr = friend.getAddress();

            buff.packLong(hostAddr[0]);
            buff.packLong(hostAddr[1]);
            buff.packInt(friend.getProcessId());
            buff.packInt(friend.getSec());
            buff.packInt(friend.getOther());
        }
        else
            throw new InvalidParameterException();
    }

    private UidHelper()
    {
    }

    class UidFriend extends Uid
    {
        public UidFriend(Uid u)
        {
            super(u);
        }

        public long[] getAddress ()
        {
            return hostAddr;
        }

        public int getProcessId ()
        {
            return process;
        }

        public int getSec ()
        {
            return sec;
        }

        public int getOther ()
        {
            return other;
        }
    }
}
