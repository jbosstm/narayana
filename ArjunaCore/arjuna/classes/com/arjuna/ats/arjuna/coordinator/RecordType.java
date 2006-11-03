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
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.ArjunaNames;

/**
 * The following enumerated type defines the types of record that are
 * derived from AbstractRecord. The type also defines the order in which
 * these types may be held by an AtomicAction if a record refers to the
 * same object (as defined by the AbstractRecord operator== operation).
 * Since records are processed sequentially during an AtomicAction
 * operation the ordering below also defines the order in which each
 * operation is invoked.
 *
 * Hence a LOCK type record will always have operations invoked before an
 * RPCTERMINATE type record. This ordering is important otherwise some
 * records may negate the effects of other record, e.g. during the 
 * top_level_commit operation a RPCTERMINATE record terminates a 
 * server - this should not occur before the server record has sent the 
 * final Commit rpc.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: RecordType.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class RecordType
{
    /**
     * The following values are provided for user-defined
     * records that should have operations invoked BEFORE the
     * system records.
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
    public static final int XTS_RECORD = 161;
    public static final int JTA_RECORD = 171;
    public static final int JTAX_RECORD = 172;
    public static final int REPLICATION = 181;
    public static final int NAMING = 191;
    public static final int CADAVER = 201;
    public static final int DISPOSE = 211;
    public static final int RPCCALL = 221;
    public static final int RPCINITIATE = 231;
    
    /**
     * The following values are provided for user-defined
     * records that should have operations invoked AFTER the
     * system records.
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
     * Note that RPCTERMINATE is deliberately late in the list
     * otherwise the server would be terminated BEFORE the user
     * records were processed.
     */

    public static final int RPCTERMINATE = 433;
    public static final int RPCCADAVER = 443;
    public static final int UNTYPED = 453;
    public static final int NONE_RECORD = 463;

    /**
     * The following is for a one-phase aware resource that we enlist in
     * a two-phase commit transaction using the last resource commit
     * optimization. It's prepare must go off after all other records.
     */
    public static final int LASTRESOURCE = Integer.MAX_VALUE;
    
    /**
     * @return the <code>ClassName</code> representing this type.
     * @see com.arjuna.ats.arjuna.gandiva.ClassName
     */

    public static ClassName typeToClassName (int rt)
    {
	 switch (rt)
	 {
	 case RecordType.LASTRESOURCE:
	     return new ClassName("RecordType.LASTRESOURCE");

	 case RecordType.USER_DEF_FIRST0:
	     return new ClassName("RecordType.USER_DEF_FIRST0");
	 case RecordType.USER_DEF_FIRST1:
	     return new ClassName("RecordType.USER_DEF_FIRST1");
	 case RecordType.USER_DEF_FIRST2:
	     return new ClassName("RecordType.USER_DEF_FIRST2");
	 case RecordType.USER_DEF_FIRST3:
	     return new ClassName("RecordType.USER_DEF_FIRST3");
	 case RecordType.USER_DEF_FIRST4:
	     return new ClassName("RecordType.USER_DEF_FIRST4");
	 case RecordType.USER_DEF_FIRST5:
	     return new ClassName("RecordType.USER_DEF_FIRST5");
	 case RecordType.USER_DEF_FIRST6:
	     return new ClassName("RecordType.USER_DEF_FIRST6");
	 case RecordType.USER_DEF_FIRST7:
	     return new ClassName("RecordType.USER_DEF_FIRST7");
	 case RecordType.USER_DEF_FIRST8:
	     return new ClassName("RecordType.USER_DEF_FIRST8");
	 case RecordType.USER_DEF_FIRST9:
	     return new ClassName("RecordType.USER_DEF_FIRST9");

	 case RecordType.LOCK:
	     return new ClassName("RecordType.LOCK");
	 case RecordType.RECOVERY:
	     return new ClassName("RecordType.RECOVERY");
	 case RecordType.PERSISTENCE:
	     return ArjunaNames.Implementation_AbstractRecord_PersistenceRecord();
	 case RecordType.TXLOG_PERSISTENCE:
	     return ArjunaNames.Implementation_AbstractRecord_TxLogPersistenceRecord();
	 case RecordType.CADAVER:
	     return ArjunaNames.Implementation_AbstractRecord_CadaverRecord();
	 case RecordType.DISPOSE:
	     return ArjunaNames.Implementation_AbstractRecord_CadaverRecord_DisposeRecord();
	 case RecordType.ACTIVATION:
	     return new ClassName("RecordType.ACTIVATION");
	 case RecordType.OTS_RECORD:
	     return new ClassName("RecordType.OTS_RECORD");
	 case RecordType.OTS_ABSTRACTRECORD:
	     return new ClassName("RecordType.OTS_ABSTRACTRECORD");
	 case RecordType.REPLICATION:
	     return new ClassName("RecordType.REPLICATION");
	 case RecordType.NAMING:
	     return new ClassName("RecordType.NAMING");
	 case RecordType.RPCCALL:
	     return new ClassName("RecordType.RPCCALL");
	 case RecordType.RPCINITIATE:
	     return new ClassName("RecordType.RPCINITIATE");
	 case RecordType.RPCTERMINATE:
	     return new ClassName("RecordType.RPCTERMINATE");
	 case RecordType.RPCCADAVER:
	     return new ClassName("RecordType.RPCCADAVER");
	 case RecordType.XTS_RECORD:
	     return new ClassName("RecordType.XTS_RECORD");
	 case RecordType.JTA_RECORD:
	     return new ClassName("RecordType.JTA_RECORD");
	 case RecordType.JTAX_RECORD:
	     return new ClassName("RecordType.JTAX_RECORD");

	 case RecordType.USER_DEF_LAST0:
	     return new ClassName("RecordType.USER_DEF_LAST0");
	 case RecordType.USER_DEF_LAST1:
	     return new ClassName("RecordType.USER_DEF_LAST1");
	 case RecordType.USER_DEF_LAST2:
	     return new ClassName("RecordType.USER_DEF_LAST2");
	 case RecordType.USER_DEF_LAST3:
	     return new ClassName("RecordType.USER_DEF_LAST3");
	 case RecordType.USER_DEF_LAST4:
	     return new ClassName("RecordType.USER_DEF_LAST4");
	 case RecordType.USER_DEF_LAST5:
	     return new ClassName("RecordType.USER_DEF_LAST5");
	 case RecordType.USER_DEF_LAST6:
	     return new ClassName("RecordType.USER_DEF_LAST6");
	 case RecordType.USER_DEF_LAST7:
	     return new ClassName("RecordType.USER_DEF_LAST7");
	 case RecordType.USER_DEF_LAST8:
	     return new ClassName("RecordType.USER_DEF_LAST8");
	 case RecordType.USER_DEF_LAST9:
	     return new ClassName("RecordType.USER_DEF_LAST9");

	 case RecordType.UNTYPED:
	     return new ClassName("RecordType.UNTYPED");

	 case RecordType.NONE_RECORD:
	     return new ClassName("RecordType.NONE_RECORD");

	 default:
	     return null;
	 }
    }

    /**
     * @return the <code>int</code> value representing this ClassName.
     * @see com.arjuna.ats.arjuna.gandiva.ClassName
     */

public static int classNameToType (ClassName cn)
    {
	if (cn.stringForm().compareTo("RecordType.LASTRESOURCE") == 0)
	    return LASTRESOURCE;
	
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_FIRST0") == 0)
		return USER_DEF_FIRST0;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_FIRST1") == 0)
		return USER_DEF_FIRST1;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_FIRST2") == 0)
		return USER_DEF_FIRST2;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_FIRST3") == 0)
		return USER_DEF_FIRST3;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_FIRST4") == 0)
		return USER_DEF_FIRST4;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_FIRST5") == 0)
		return USER_DEF_FIRST5;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_FIRST6") == 0)
		return USER_DEF_FIRST6;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_FIRST7") == 0)
		return USER_DEF_FIRST7;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_FIRST8") == 0)
		return USER_DEF_FIRST8;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_FIRST9") == 0)
		return USER_DEF_FIRST9;
	
	else
	    if (cn.stringForm().compareTo("RecordType.LOCK") == 0)
		return LOCK;
	else
	    if (cn.stringForm().compareTo("RecordType.RECOVERY") == 0)
		return RECOVERY;
	else
	    if (cn.equals(ArjunaNames.Implementation_AbstractRecord_PersistenceRecord()))
		return PERSISTENCE;
	else
	    if (cn.equals(ArjunaNames.Implementation_AbstractRecord_TxLogPersistenceRecord()))
		return TXLOG_PERSISTENCE;
	else
	    if (cn.equals(ArjunaNames.Implementation_AbstractRecord_CadaverRecord()))
		return CADAVER;
	else
	    if (cn.equals(ArjunaNames.Implementation_AbstractRecord_CadaverRecord_DisposeRecord()))
		return DISPOSE;
	else
	    if (cn.stringForm().compareTo("RecordType.ACTIVATION") == 0)
		return ACTIVATION;
	else
	    if (cn.stringForm().compareTo("RecordType.OTS_RECORD") == 0)
		return OTS_RECORD;
	else
	    if (cn.stringForm().compareTo("RecordType.OTS_ABSTRACTRECORD") == 0)
		return OTS_ABSTRACTRECORD;
	else
	    if (cn.stringForm().compareTo("RecordType.REPLICATION") == 0)
		return REPLICATION;
	else
	    if (cn.stringForm().compareTo("RecordType.NAMING") == 0)
		return NAMING;
	else
	    if (cn.stringForm().compareTo("RecordType.RPCCALL") == 0)
		return RPCCALL;
	else
	    if (cn.stringForm().compareTo("RecordType.RPCINITIATE") == 0)
		return RPCINITIATE;
	else
	    if (cn.stringForm().compareTo("RecordType.RPCTERMINATE") == 0)
		return RPCTERMINATE;
	else
	    if (cn.stringForm().compareTo("RecordType.RPCCADAVER") == 0)
		return RPCCADAVER;

	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_LAST0") == 0)
		return USER_DEF_LAST0;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_LAST1") == 0)
		return USER_DEF_LAST1;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_LAST2") == 0)
		return USER_DEF_LAST2;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_LAST3") == 0)
		return USER_DEF_LAST3;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_LAST4") == 0)
		return USER_DEF_LAST4;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_LAST5") == 0)
		return USER_DEF_LAST5;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_LAST6") == 0)
		return USER_DEF_LAST6;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_LAST7") == 0)
		return USER_DEF_LAST7;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_LAST8") == 0)
		return USER_DEF_LAST8;
	else
	    if (cn.stringForm().compareTo("RecordType.USER_DEF_LAST9") == 0)
		return USER_DEF_LAST9;

	else
	    if (cn.stringForm().compareTo("RecordType.UNTYPED") == 0)
		return UNTYPED;

	else
	    if (cn.stringForm().compareTo("RecordType.NONE_RECORD") == 0)
		return NONE_RECORD;
	else
	    if (cn.stringForm().compareTo("RecordType.XTS_RECORD") == 0)
		return XTS_RECORD;
	else
	    return -1;
    }

    /**
     * Print a human-readable version of the lock type.
     */

public static void print (PrintWriter strm, int rt)
    {
	strm.print(typeToClassName(rt));
	strm.flush();
    }

}
