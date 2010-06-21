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
 * $Id: ObjectStoreType.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.objectstore;

import java.io.*;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.type.ObjectStoreTypeManager;
import com.arjuna.ats.internal.arjuna.objectstore.ActionStore;
import com.arjuna.ats.internal.arjuna.objectstore.CacheStore;
import com.arjuna.ats.internal.arjuna.objectstore.HashedActionStore;
import com.arjuna.ats.internal.arjuna.objectstore.HashedStore;
import com.arjuna.ats.internal.arjuna.objectstore.JDBCActionStore;
import com.arjuna.ats.internal.arjuna.objectstore.JDBCStore;
import com.arjuna.ats.internal.arjuna.objectstore.LogStore;
import com.arjuna.ats.internal.arjuna.objectstore.NullActionStore;
import com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore;
import com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore;
import com.arjuna.ats.internal.arjuna.objectstore.VolatileStore;

/**
 * The various types of object store implementations that are available.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectStoreType.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public class ObjectStoreType
{

    /*
     * Do not re-order these!
     */

    public static final int ACTION = 0;

    public static final int ACTIONLOG = 1;

    public static final int SHADOWING = 2;

    public static final int FRAGMENTED = 3;

    public static final int VOLATILE = 4;

    public static final int HASHED = 5;

    public static final int REPLICATED = 6;

    public static final int MAPPED = 7;

    public static final int SINGLETYPEMAPPED = 8;

    public static final int USER_DEF_0 = 9;

    public static final int USER_DEF_1 = 10;

    public static final int USER_DEF_2 = 11;

    public static final int USER_DEF_3 = 12;

    public static final int USER_DEF_4 = 13;

    public static final int SHADOWNOFILELOCK = 14;

    public static final int JDBC = 15;

    public static final int JDBC_ACTION = 16;

    public static final int HASHED_ACTION = 17;

    public static final int CACHED = 18;

    public static final int NULL_ACTION = 19;

    public static final int USER_DEF_5 = 30;

    public static final int USER_DEF_6 = 31;

    public static final int USER_DEF_7 = 32;

    public static final int USER_DEF_8 = 33;

    public static final int USER_DEF_9 = 34;

    /**
     * @return the <code>Class</code> for this object store value.
     */

    @SuppressWarnings("unchecked")
    public final static Class<? extends ObjectStore> typeToClass (int rt)
    {
        switch (rt)
        {
        case ACTION:
            return ActionStore.class;
        case NULL_ACTION:
            return NullActionStore.class;
        case ACTIONLOG:
            return LogStore.class;
        case SHADOWING:
            return ShadowingStore.class;
        case VOLATILE:
            return VolatileStore.class;
        case HASHED:
            return HashedStore.class;
        case SHADOWNOFILELOCK:
            return ShadowNoFileLockStore.class;
        case JDBC:
            return JDBCStore.class;
        case JDBC_ACTION:
            return JDBCActionStore.class;
        case HASHED_ACTION:
            return HashedActionStore.class;
        case CACHED:
            return CacheStore.class;
        default:
            return ObjectStoreTypeManager.manager().getObjectStoreClass(rt);
        }
    }

    /**
     * @return the <code>int</code> value for this object store.
     *         <code>Class</code>.
     * @message com.arjuna.ats.arjuna.objectstore.ObjectStoreType_1
     *          [com.arjuna.ats.arjuna.objectstore.ObjectStoreType_1] - unknown
     *          store: {0}
     * @message com.arjuna.ats.arjuna.objectstore.ObjectStoreType_2
     *          [com.arjuna.ats.arjuna.objectstore.ObjectStoreType_2] - unknown
     *          store:
     */

    @SuppressWarnings("unchecked")
    public final static int classToType (Class c)
    {
        if (c.equals(ActionStore.class))
            return ACTION;
        if (c.equals(NullActionStore.class))
            return NULL_ACTION;
        if (c.equals(LogStore.class))
            return ACTIONLOG;
        if (c.equals(ShadowingStore.class))
            return SHADOWING;
        if (c.equals(VolatileStore.class))
            return VOLATILE;
        if (c.equals(HashedStore.class))
            return HASHED;
        if (c.equals(ShadowNoFileLockStore.class))
            return SHADOWNOFILELOCK;
        if (c.equals(JDBCStore.class))
            return JDBC;
        if (c.equals(JDBCActionStore.class))
            return JDBC_ACTION;
        if (c.equals(HashedActionStore.class))
            return HASHED_ACTION;
        if (c.equals(CacheStore.class))
            return CACHED;
        
        int type = ObjectStoreTypeManager.manager().getType(c);

        if (type >= 0)
            return type;
        
        if (tsLogger.arjLoggerI18N.isWarnEnabled()) {
            tsLogger.i18NLogger.warn_objectstore_ObjectStoreType_1(c.getCanonicalName());
        }

        throw new com.arjuna.ats.arjuna.exceptions.FatalError(
                tsLogger.i18NLogger.get_objectstore_ObjectStoreType_2()
                        + c);
    }

    /**
     * Print on the specified <code>PrintWriter</code> a string representation
     * of the object store value.
     */

    @SuppressWarnings("unchecked")
    public final static void print (PrintWriter strm, int rt)
    {
        Class c = typeToClass(rt);

        strm.print(c);

        c = null;
    }

    /**
     * @return <code>true</code> if the value is valid, <code>false</code>
     *         otherwise.
     */

    public static final boolean valid (int rt)
    {
        if (typeToClass(rt) != null)
            return true;
        else
            return false;
    }
    
    public static final String getDefaultStoreType ()
    {
        /*
         * Check once per application. At present this means that all objects
         * have the same object store implementation. However, this need not be
         * the case, and could be an attribute of the object, or derived from
         * the object's name.
         */

        if (objectStoreType == null)
            objectStoreType = arjPropertyManager
                    .getObjectStoreEnvironmentBean().getObjectStoreType();

        return objectStoreType;
    }
    
    static String objectStoreType = null;

}
