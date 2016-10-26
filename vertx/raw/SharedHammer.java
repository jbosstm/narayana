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

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.ObjectModel;

public class SharedHammer
{
    public static void main (String[] args)
    {
        boolean creator = true;
        Uid id = null;
        
        for (int i = 0; i < args.length; i++)
        {
            if ("-object".equals(args[i]))
            {
                creator = false;
                
                id = new Uid(args[i+1]);
            }
        }
        
        if (creator)
        {
            HammerThreadedObject.object = new AtomicObject(ObjectModel.MULTIPLE);
            
            System.out.println("Object created with id: "+HammerThreadedObject.object);

	    try
	    {
		Thread.sleep(5000);  // time to get other server(s) running
	    }
	    catch (final Throwable ex)
	    {
	    }
        }
        else
            HammerThreadedObject.object = new AtomicObject(id, ObjectModel.MULTIPLE);
        
        HammerThreadedObject object1 = new HammerThreadedObject(1);
        
        object1.start();

        try
        {
            object1.join();
        }
        catch (InterruptedException e)
        {
        }
    }
}
