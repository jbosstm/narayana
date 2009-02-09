/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ActionStoreSetup.java,v 1.1 2003/06/19 10:50:32 nmcl Exp $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.gandiva.*;
import com.arjuna.ats.arjuna.gandiva.inventory.*;

public class LogStoreSetup implements InventoryElement
{

	public LogStoreSetup()
	{
	}

	public synchronized Object createVoid()
	{
		return LogStore.create();
	}

	public synchronized Object createResources(Object[] param)
	{
		return LogStore.create(param);
	}

	public synchronized Object createClassName(ClassName className)
	{
		return null;
	}

	public synchronized Object createObjectName(ObjectName objectName)
	{
		return LogStore.create(objectName);
	}

	public synchronized Object createClassNameResources(ClassName className,
			Object[] resources)
	{
		return null;
	}

	public synchronized Object createObjectNameResources(ObjectName objectName,
			Object[] resources)
	{
		return null;
	}

	public ClassName className()
	{
		return ArjunaNames.Implementation_ObjectStore_ActionLogStore();
	}

}
