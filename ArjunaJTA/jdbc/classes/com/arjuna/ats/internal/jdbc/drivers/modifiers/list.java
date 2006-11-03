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
 * Copyright (C) 2001 - 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: list.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc.drivers.modifiers;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.ModifierFactory;

public class list
{

public list ()
    {
	ModifierFactory.putModifier("COM.FirstSQL.Dbcp.DbcpDriver", -1, -1, "com.arjuna.ats.internal.jdbc.drivers.modifiers.firstsql_jndi");

	ModifierFactory.putModifier("Oracle JDBC driver", 9, -1, "com.arjuna.ats.internal.jdbc.drivers.modifiers.oracle_jndi");

	ModifierFactory.putModifier("SQLServer", 2, -1, "com.arjuna.ats.internal.jdbc.drivers.modifiers.sqlserver_jndi");

	ModifierFactory.putModifier(oracleName, 9, 0, "com.arjuna.ats.internal.jdbc.drivers.modifiers.oracle_9_0");

	ModifierFactory.putModifier(oracleName, 8, 1, "com.arjuna.ats.internal.jdbc.drivers.modifiers.oracle_8_1");

	ModifierFactory.putModifier(sequelinkName, 5, 1, "com.arjuna.ats.internal.jdbc.drivers.modifiers.sequelink_5_1");

	ModifierFactory.putModifier(cloudscapeName, 3, 6, "com.arjuna.ats.internal.jdbc.drivers.modifiers.cloudscape_3_6");
    }

private static final String oracleName = "oracle";
private static final String sequelinkName = "sequelink";
private static final String cloudscapeName = "cloudscape";
    
}
