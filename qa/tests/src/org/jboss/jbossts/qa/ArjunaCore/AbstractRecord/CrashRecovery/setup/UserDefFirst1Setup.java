/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 *
 * Look at OTS_AbstractRecordSetup.java to see what CORBA does.
 */
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.setup;

import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.gandiva.ObjectName;
import com.arjuna.ats.arjuna.gandiva.inventory.InventoryElement;
import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl.CrashAbstractRecord02;

public class UserDefFirst1Setup implements InventoryElement
{

	public UserDefFirst1Setup()
	{
	}

	public synchronized Object createVoid()
	{
		return CrashAbstractRecord02.create();
	}

	public synchronized Object createClassName(ClassName className)
	{
		return null;
	}

	public synchronized Object createObjectName(ObjectName objectName)
	{
		return null;
	}

	public synchronized Object createResources(Object[] resources)
	{
		return null;
	}

	public synchronized Object createClassNameResources(ClassName className, Object[] resources)
	{
		return null;
	}

	public synchronized Object createObjectNameResources(ObjectName objectName, Object[] resources)
	{
		return null;
	}

	public ClassName className()
	{
		return new ClassName("RecordType.USER_DEF_FIRST1");
	}

}
