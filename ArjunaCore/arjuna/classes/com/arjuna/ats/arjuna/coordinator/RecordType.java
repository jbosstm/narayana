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
 * $Id: RecordType.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import java.io.*;

import com.arjuna.ats.arjuna.coordinator.record.RecordTypeManager;
import com.arjuna.ats.arjuna.ActivationRecord;
import com.arjuna.ats.arjuna.CadaverRecord;
import com.arjuna.ats.arjuna.DisposeRecord;
import com.arjuna.ats.arjuna.LastResourceRecord;
import com.arjuna.ats.arjuna.PersistenceRecord;
import com.arjuna.ats.arjuna.RecoveryRecord;
import com.arjuna.ats.internal.arjuna.TxLogWritePersistenceRecord;

/**
 * The following enumerated type defines the types of record that are derived
 * from AbstractRecord. The type also defines the order in which these types may
 * be held by an AtomicAction if a record refers to the same object (as defined
 * by the AbstractRecord operator== operation). Since records are processed
 * sequentially during an AtomicAction operation the ordering below also defines
 * the order in which each operation is invoked. Hence a LOCK type record will
 * always have operations invoked before an RPCTERMINATE type record. This
 * ordering is important otherwise some records may negate the effects of other
 * record, e.g. during the top_level_commit operation a RPCTERMINATE record
 * terminates a server - this should not occur before the server record has sent
 * the final Commit rpc.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: RecordType.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public class RecordType
{
    /**
     * The following values are provided for user-defined records that should
     * have operations invoked BEFORE the system records.
     */

    public static final int USER_DEF_FIRST0 = 1;

    public static final int USER_DEF_FIRST1 = 2;

    public static final int USER_DEF_FIRST2 = 3;

    public static final int USER_DEF_FIRST3 = 4;

    public static final int USER_DEF_FIRST4 = 5;

    public static final int USER_DEF_FIRST5 = 6;

    public static final int USER_DEF_FIRST6 = 7;

    public static final int USER_DEF_FIRST7 = 8;

    public static final int USER_DEF_FIRST8 = 9;

    public static final int USER_DEF_FIRST9 = 10;

    /**
     * The values are used by the system records.
     */

    public static final int RECOVERY = 101;

    public static final int PERSISTENCE = 111;

    public static final int TXLOG_PERSISTENCE = 112;

    public static final int LOCK = 121;

    public static final int ACTIVATION = 131;

    public static final int OTS_RECORD = 141;

    public static final int OTS_ABSTRACTRECORD = 151;

    public static final int XTS_WSAT_RECORD = 161;

    public static final int XTS_WSBA_RECORD = 162;

    public static final int JTA_RECORD = 171;

    public static final int JTAX_RECORD = 172;

    public static final int REPLICATION = 181;

    public static final int NAMING = 191;

    public static final int CADAVER = 201;

    public static final int DISPOSE = 211;

    public static final int RPCCALL = 221;

    public static final int RPCINITIATE = 231;

    /**
     * The following values are provided for user-defined records that should
     * have operations invoked AFTER the system records.
     */

    public static final int USER_DEF_LAST0 = 242;

    public static final int USER_DEF_LAST1 = 252;

    public static final int USER_DEF_LAST2 = 262;

    public static final int USER_DEF_LAST3 = 272;

    public static final int USER_DEF_LAST4 = 282;

    public static final int USER_DEF_LAST5 = 292;

    public static final int USER_DEF_LAST6 = 302;

    public static final int USER_DEF_LAST7 = 312;

    public static final int USER_DEF_LAST8 = 322;

    public static final int USER_DEF_LAST9 = 332;

    /**
     * Note that RPCTERMINATE is deliberately late in the list otherwise the
     * server would be terminated BEFORE the user records were processed.
     */

    public static final int RPCTERMINATE = 433;

    public static final int RPCCADAVER = 443;

    public static final int UNTYPED = 453;

    public static final int NONE_RECORD = 463;

    /**
     * The following is for a one-phase aware resource that we enlist in a
     * two-phase commit transaction using the last resource commit optimization.
     * It's prepare must go off after all other records.
     */
    public static final int LASTRESOURCE = Integer.MAX_VALUE;

    /**
     * @return the <code>Class</code> representing this type.
     */

    @SuppressWarnings("unchecked")
    public static Class typeToClass (int rt)
    {
        switch (rt)
        {
        case RecordType.LASTRESOURCE:
            return LastResourceRecord.class;

        case RecordType.USER_DEF_FIRST0:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_FIRST0);
        case RecordType.USER_DEF_FIRST1:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_FIRST1);
        case RecordType.USER_DEF_FIRST2:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_FIRST2);
        case RecordType.USER_DEF_FIRST3:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_FIRST3);
        case RecordType.USER_DEF_FIRST4:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_FIRST4);
        case RecordType.USER_DEF_FIRST5:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_FIRST5);
        case RecordType.USER_DEF_FIRST6:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_FIRST6);
        case RecordType.USER_DEF_FIRST7:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_FIRST7);
        case RecordType.USER_DEF_FIRST8:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_FIRST8);
        case RecordType.USER_DEF_FIRST9:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_FIRST9);

        case RecordType.LOCK:
            RecordTypeManager.manager().getClass(
                    RecordType.LOCK);
        case RecordType.RECOVERY:
            return RecoveryRecord.class;
        case RecordType.PERSISTENCE:
            return PersistenceRecord.class;
        case RecordType.TXLOG_PERSISTENCE:
            return TxLogWritePersistenceRecord.class;
        case RecordType.CADAVER:
            return CadaverRecord.class;
        case RecordType.DISPOSE:
            return DisposeRecord.class;
        case RecordType.ACTIVATION:
            return ActivationRecord.class;

        case RecordType.OTS_RECORD:
            return RecordTypeManager.manager().getClass(RecordType.OTS_RECORD);
        case RecordType.OTS_ABSTRACTRECORD:
            return RecordTypeManager.manager().getClass(
                    RecordType.OTS_ABSTRACTRECORD);
        case RecordType.REPLICATION:
            return RecordTypeManager.manager().getClass(RecordType.REPLICATION);
        case RecordType.NAMING:
            return RecordTypeManager.manager().getClass(RecordType.NAMING);
        case RecordType.RPCCALL:
            return RecordTypeManager.manager().getClass(RecordType.RPCCALL);
        case RecordType.RPCINITIATE:
            return RecordTypeManager.manager().getClass(RecordType.RPCINITIATE);
        case RecordType.RPCTERMINATE:
            return RecordTypeManager.manager()
                    .getClass(RecordType.RPCTERMINATE);
        case RecordType.RPCCADAVER:
            return RecordTypeManager.manager().getClass(RecordType.RPCCADAVER);
        case RecordType.XTS_WSAT_RECORD:
            return RecordTypeManager.manager().getClass(
                    RecordType.XTS_WSAT_RECORD);
        case RecordType.XTS_WSBA_RECORD:
            return RecordTypeManager.manager().getClass(
                    RecordType.XTS_WSBA_RECORD);
        case RecordType.JTA_RECORD:
            return RecordTypeManager.manager().getClass(RecordType.JTA_RECORD);
        case RecordType.JTAX_RECORD:
            return RecordTypeManager.manager().getClass(RecordType.JTAX_RECORD);

        case RecordType.USER_DEF_LAST0:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_LAST0);
        case RecordType.USER_DEF_LAST1:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_LAST1);
        case RecordType.USER_DEF_LAST2:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_LAST2);
        case RecordType.USER_DEF_LAST3:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_LAST3);
        case RecordType.USER_DEF_LAST4:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_LAST4);
        case RecordType.USER_DEF_LAST5:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_LAST5);
        case RecordType.USER_DEF_LAST6:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_LAST6);
        case RecordType.USER_DEF_LAST7:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_LAST7);
        case RecordType.USER_DEF_LAST8:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_LAST8);
        case RecordType.USER_DEF_LAST9:
            return RecordTypeManager.manager().getClass(
                    RecordType.USER_DEF_LAST9);

        case RecordType.UNTYPED:
            return AbstractRecord.class;

        case RecordType.NONE_RECORD:
            return null;

        default:
            return null;
        }
    }

    /**
     * @return the <code>int</code> value representing this Class.
     */

    @SuppressWarnings("unchecked")
    public static int classToType (Class cn)
    {
        if (LastResourceRecord.class.equals(cn))
            return LASTRESOURCE;

        else if (RecoveryRecord.class.equals(cn))
            return RECOVERY;
        else if (PersistenceRecord.class.equals(cn))
            return PERSISTENCE;
        else if (TxLogWritePersistenceRecord.class.equals(cn))
            return TXLOG_PERSISTENCE;
        else if (CadaverRecord.class.equals(cn))
            return CADAVER;
        else if (DisposeRecord.class.equals(cn))
            return DISPOSE;
        
        else
            return RecordTypeManager.manager().getType(cn);
    }

    /**
     * Print a human-readable version of the lock type.
     */

    public static void print (PrintWriter strm, int rt)
    {
        strm.print(typeToClass(rt));
        strm.flush();
    }

}
