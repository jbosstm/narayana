/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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