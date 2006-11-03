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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ObjectStateQuery.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.tools;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import java.io.PrintWriter;

public class ObjectStateQuery
{
    
public static void main (String[] args)
    {
	String uid = null;
	String type = null;

	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-help") == 0)
	    {
		usage();
		System.exit(0);
	    }
	    else
	    {
		if (args[i].compareTo("-uid") == 0)
		{
		    uid = args[i+1];
		    i++;
		}
		else
		{
		    if (args[i].compareTo("-type") == 0)
		    {
			type = args[i+1];
			i++;
		    }
		    else
		    {
			System.out.println("Unknown option "+args[i]);
			usage();

			System.exit(0);
		    }
		}
	    }
	}

	try
	{
	    ObjectStore imple = new ObjectStore();

	    System.out.println("Status is "+imple.currentState(new Uid(uid), type));

	    InputObjectState buff = new InputObjectState();
	    
	    imple.allObjUids(type, buff, ObjectStore.OS_UNCOMMITTED);
	    
	    Uid u = new Uid(Uid.nullUid());
	    
	    u.unpack(buff);
	    
	    System.out.println("got "+u);
	}
	catch (Exception e)
	{
	    System.err.println("Caught unexpected exception: "+e);
	}
    }

private static void usage ()
    {
	System.out.println("Usage: ObjectStateQuery [-uid <state id>] [-type <state type>] [-help]");
    }
 
}

