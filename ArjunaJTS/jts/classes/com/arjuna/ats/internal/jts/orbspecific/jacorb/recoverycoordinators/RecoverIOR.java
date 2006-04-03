/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/**
 * @author Malik SAHEB (malik.saheb@arjuna.com)
 * @version $Id
 */

package com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators;

import com.arjuna.ats.internal.jts.ORBManager;

import org.jacorb.orb.iiop.*;
import org.jacorb.orb.ORB;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.CDROutputStream;
import org.omg.IOP.TaggedProfile;
import org.omg.IOP.TaggedProfileHolder;
import org.omg.IOP.IOR;
 
import java.util.List;

public class RecoverIOR
{
 
    private static class RecoverableParsedIOR extends ParsedIOR {
	public RecoverableParsedIOR(String ior) {
        this((ORB)ORBManager.getORB().orb(), ior) ;
    }
    
    public RecoverableParsedIOR(final ORB orb, final String ior) {
	    super(ior, orb, orb.getConfiguration().getNamedLogger("arjuna.recovery.parsedior"));
	}
	
	
	public IOR newObjectKey(String objectId) {

	    String the_object_key = new String(get_object_key());
	    int position = the_object_key.indexOf("RecoveryManager");
	    String new_object_key = the_object_key.substring(0, position).concat(objectId);

	    IOR new_ior = new IOR();
	    List profiles = getProfiles();
	    new_ior.type_id = getTypeId();
	    new_ior.profiles = new TaggedProfile[profiles.size()];

	    for (int i = 0; i < profiles.size(); i++)
	    {
		IIOPProfile pb = (IIOPProfile) profiles.get(i);
		IIOPProfile new_pb = (IIOPProfile) pb.copy();
		
		new_pb.set_object_key(new_object_key.getBytes());
		
		new_ior.profiles[i] = new TaggedProfile();
		new_ior.profiles[i].tag = 0; // IIOP

		TaggedProfileHolder holder = new TaggedProfileHolder(new_ior.profiles[i]);

		new_pb.marshal(holder, null);
		new_ior.profiles[i].profile_data = holder.value.profile_data;
	    }
	    
	    return new_ior;
	}

	// It appears that the following method do the same role as above
	public IOR newIOR(String objectId)
	{
	    String the_object_key = new String(get_object_key());
	    int position = the_object_key.indexOf("RecoveryManager");
	    String new_object_key = the_object_key.substring(0, position).concat(objectId);
	    IOR new_ior = ParsedIOR.createObjectIOR(getEffectiveProfile());
	    return new_ior;
         }


	// Sanity check.
	public void printInfo ()
	{
	    IIOPProfile profile = (IIOPProfile) getEffectiveProfile();

	    System.out.println("\n Print some Information -------------");
	    System.out.println("Host Name "+ profile.getAddress().getHostname());
	    System.out.println("Object ID "+ getIDString());
	    System.out.println("Type Name "+ getTypeId());
	    System.out.println("Host Address "+ profile.getAddress());
	    System.out.println("Object Key "+ new String(get_object_key()));
	    System.out.println("\n\n");
	}	
	
    }
    
    public static String newObjectKey (String ior, String Key)
    {
	RecoverableParsedIOR pior = new RecoverableParsedIOR(ior);
	IOR new_ior = pior.newObjectKey(Key);
	//IOR new_ior = pior.newIOR(Key);
	return iorToString(new_ior);
    }


    public static void printIORinfo(String ior)
    {
	RecoverableParsedIOR pior = new RecoverableParsedIOR(ior);
	pior.printInfo();
    }    

    
    private static String iorToString(IOR ior) {

	try  
	   {  
	       CDROutputStream out = new CDROutputStream();
	       
	       // endianness = false, big-endian
	       out.write_boolean(false);
	       org.omg.IOP.IORHelper.write(out, ior);
	       
	       byte bytes[] = out.getBufferCopy();
	       StringBuffer sb = new StringBuffer("IOR:");
	       for (int j=0; j<bytes.length; j++) {
		   int b = bytes[j];
		   if(b<0) b+= 256;
		   int n1 = (0xff & b) / 16;
		   int n2 = (0xff & b) % 16;
		   int c1 = (n1 < 10) ? ('0' + n1) : ('a' + (n1 - 10));
		   int c2 = (n2 < 10) ? ('0' + n2) : ('a' + (n2 - 10));
		   sb.append((char)c1);
		   sb.append((char)c2);
	       }
	       return sb.toString();
	   } 
	catch (Exception e)
	{
	    return null;
	}
	
    }
    
}
