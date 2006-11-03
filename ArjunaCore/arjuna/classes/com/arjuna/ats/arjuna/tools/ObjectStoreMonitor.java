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
 * $Id: ObjectStoreMonitor.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.tools;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import java.io.PrintWriter;

public class ObjectStoreMonitor
{
    
public static void main (String[] args)
    {
	String storeImple = null;
	String root = null;

	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-help") == 0)
	    {
		usage();
		System.exit(0);
	    }
	    else
	    {
		if (args[i].compareTo("-store") == 0)
		{
		    storeImple = args[i+1];
		    i++;
		}
		else
		{
		    if (args[i].compareTo("-root") == 0)
		    {
			root = args[i+1];
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
	    ObjectStore imple = null;

	    if (storeImple != null)
		imple = new ObjectStore(new ClassName(storeImple), root);
	    else
		imple = new ObjectStore(root);
	    
	    InputObjectState types = new InputObjectState();

	    if (imple.allTypes(types))
	    {
		String theName = null;
		int count = 0;

		try
		{
		    boolean endOfList = false;

		    while (!endOfList)
		    {
			theName = types.unpackString();

			if (theName.compareTo("") == 0)
			    endOfList = true;
			else
			{
			    count++;
	    
			    System.out.println(count+": "+theName);

			    InputObjectState uids = new InputObjectState();

			    if (imple.allObjUids(theName, uids))
			    {
				Uid theUid = new Uid(Uid.nullUid());

				try
				{
				    boolean endOfUids = false;
				    
				    while (!endOfUids)
				    {
					theUid.unpack(uids);

					if (theUid.equals(Uid.nullUid()))
					    endOfUids = true;
					else
					{
					    System.out.print("\t"+theUid+" state is ");
					    System.out.print(ObjectStore.stateStatusString(imple.currentState(theUid, theName)));

					    System.out.println();
					}
				    }
				}
				catch (Exception e)
				{
				    // end of uids!
				}
			    }

			    System.out.println();
			}
		    }
		}
		catch (Exception e)
		{
		    System.err.println(e);
		    
		    // end of list!
		}
	    }
	}
	catch (Exception e)
	{
	    System.err.println("Caught unexpected exception: "+e);
	}
    }

private static void usage ()
    {
	System.out.println("Usage: ActionMonitor [-store <object store>] [-root <store root>] [-help]");
    }
 
};

