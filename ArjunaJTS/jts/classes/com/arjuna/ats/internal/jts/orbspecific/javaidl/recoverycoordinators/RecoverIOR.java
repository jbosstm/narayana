/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

/**
 * @author Malik SAHEB (malik.saheb@arjuna.com)
 * @version $Id
 */

package com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators;

import com.arjuna.ats.internal.jts.ORBManager;
import com.sun.corba.se.impl.ior.IORImpl;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.spi.ior.IORFactories;
import com.sun.corba.se.spi.ior.ObjectId;
import java.nio.charset.StandardCharsets;

import org.omg.CosTransactions.RecoveryCoordinatorHelper;

/**
 * Construct a recovery IOR for use with the default ORB bundled with the default JDK
 */
public class RecoverIOR
{
    static String getIORFromString(org.omg.CORBA.ORB orb, String str, String Key )
    {
        com.sun.corba.se.spi.orb.ORB sun_orb = (com.sun.corba.se.spi.orb.ORB) orb;

        // calculate the new object key
        String object_key = JavaIdlRCServiceInit.RC_KEY;
        int position = object_key.indexOf(JavaIdlRCServiceInit.RC_ID);
        String new_object_key = object_key.substring(0, position).concat(Key);
//        org.omg.CORBA.Object corbject = ORBManager.getORB().orb().string_to_object(str);
        org.omg.CORBA.ORB orbImple = ORBManager.getORB().orb();
        org.omg.CORBA.Object corbject = orbImple.string_to_object(str); 

        com.sun.corba.se.spi.ior.IOR ior = IORFactories.getIOR(corbject);
        ObjectId oid = IORFactories.makeObjectId(new_object_key.getBytes(StandardCharsets.UTF_8));

        IORImpl new_ior = new IORImpl(sun_orb, RecoveryCoordinatorHelper.id(), ior.getIORTemplates(), oid);

        return new_ior.stringify();
    }

}