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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: JDBCActionStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.internal.arjuna.objectstore.JDBCImple;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.arjuna.gandiva.*;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreImple;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreType;
import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.gandiva.ObjectName;
import com.arjuna.ats.arjuna.gandiva.ClassName;

import com.arjuna.common.util.logging.DebugLevel;
import com.arjuna.common.util.logging.VisibilityLevel;
import java.io.*;
import java.sql.*;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreError;
import com.arjuna.ats.arjuna.exceptions.FatalError;

import java.io.IOException;

/**
 * The transaction log implementation.
*/

public class JDBCActionStore extends JDBCStore
{
    public int typeIs ()
    {
	return ObjectStoreType.JDBC_ACTION;
    }
    
    /**
     * The following operation commits a previous write_state operation which
     * was made with the SHADOW StateType argument. This is achieved by
     * renaming the shadow and removing the hidden version.
     */

    public synchronized boolean commit_state (Uid objUid,
					      String tName) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, 
				     "JDBCActionStore.commit_state("+objUid+", "+tName+")");
	}

	boolean result = false;

	/* Bail out if the object store is not set up */

	if (!storeValid())
	    return false;

	if (currentState(objUid, tName) == ObjectStore.OS_COMMITTED)
	    result = true;
    
	return result;
    }

    public boolean hide_state (Uid u, String tn) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "JDBCActionStore.hide_state("+u+", "+tn+")");
	}

	return false;
    }

    public boolean reveal_state (Uid u, String tn) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "JDBCActionStore.reveal_state("+u+", "+tn+")");
	}
	
	return false;
    }

    public InputObjectState read_committed (Uid storeUid, String tName) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "JDBCActionStore.read_committed("+storeUid+", "+tName+")");
	}
	
	return super.read_committed(storeUid, tName);
    }

    public InputObjectState read_uncommitted (Uid u, String tn) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "JDBCActionStore.read_uncommitted("+u+", "+tn+")");
	}
	
	return null;
    }

    public boolean remove_committed (Uid storeUid, String tName) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "JDBCActionStore.remove_committed("+storeUid+", "+tName+")");
	}
	
	return super.remove_committed(storeUid, tName);
    }

    public boolean remove_uncommitted (Uid u, String tn) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "JDBCActionStore.remove_uncommitted("+u+", "+tn+")");
	}
	
	return false;
    }

    public boolean write_committed (Uid storeUid, String tName, OutputObjectState state) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "JDBCActionStore.write_committed("+storeUid+", "+tName+")");
	}
	
	return super.write_committed(storeUid, tName, state);
    }

    public boolean write_uncommitted (Uid u, String tn, OutputObjectState s) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "JDBCActionStore.write_uncommitted("+u+", "+tn+", "+s+")");
	}
	
	return false;
    }

    public ClassName className ()
    {
	return ArjunaNames.Implementation_ObjectStore_JDBCActionStore();
    }

    public static ClassName name ()
    {
	return ArjunaNames.Implementation_ObjectStore_JDBCActionStore();
    }    

    public static JDBCStore create ()
    {
	return new JDBCActionStore();
    }

    public static JDBCStore create (Object[] param)
    {
	if (param == null)
	    return null;

	return new JDBCActionStore((String) param[0]);	/* param[0] should be the table name */
    }

    public static JDBCStore create (ObjectName param)
    {
	if (param == null)
	    return null;

	return new JDBCActionStore(param);
    }

    private JDBCActionStore()
    {
	super() ;
    }

    private JDBCActionStore(final String tableName)
    {
	super(tableName) ;
    }

    private JDBCActionStore(final ObjectName objName)
    {
	super(objName) ;
    }

    protected String getAccessClassName()
    {
    	if (_txClassName == null)
	    _txClassName = arjPropertyManager.propertyManager.getProperty(Environment.JDBC_TX_DB_ACCESS);
	return _txClassName;
    }

    protected void setAccessClassName(String txClassName)
    {
	_txClassName = txClassName;
    }

    protected String getDefaultTableName()
    {
	return _defaultTxTableName;
    }

    protected String getAccessClassNameFromObject(ObjectName objName) throws IOException
    {
	return objName.getStringAttribute(com.arjuna.ats.arjuna.common.Environment.JDBC_TX_DB_ACCESS);
    }

    protected String getTableNameFromObject(ObjectName objName) throws IOException
    {
	return objName.getStringAttribute(ArjunaNames.Implementation_ObjectStore_JDBC_tableName());
    }

    protected JDBCAccess getJDBCAccess()
    {
	return _txJDBCAccess;
    }

    protected void setJDBCAccess(JDBCAccess jdbcAccess)
    {
        _txJDBCAccess = jdbcAccess ;
    }

    protected String getTableName()
    {
        return _txTableName;
    }

    protected void setTableName(String tableName)
    {
        _txTableName = tableName ;
    }

    private JDBCAccess _txJDBCAccess;
    private String _txClassName;
    private String _txTableName;
    private static String _defaultTxTableName = "JBossTSTxTable";

}
