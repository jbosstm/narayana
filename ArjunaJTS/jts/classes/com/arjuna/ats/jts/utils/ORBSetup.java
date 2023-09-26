/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.utils;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.utils.InitClassInterface;

/**
 * Setup the ORB and POA used by the transaction system.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: ORBSetup.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ORBSetup implements InitClassInterface
{
    /**
     * This method is invoked when the ORB is initialised
     */
    public void invoke(Object obj)
    {
        if ( !ORBManager.isInitialised() )
        {
            if (jtsLogger.logger.isTraceEnabled()) {
                jtsLogger.logger.trace("The ORBSetup.invoke method has been invoked");
            }

            /**
             * If the object passed in is an ORB Portability ORB
             * then set it to be the ORB used by the JTS.
             */
            if ( obj instanceof ORB )
            {
                ORB orb = (ORB)obj;

                if (jtsLogger.logger.isTraceEnabled()) {
                    jtsLogger.logger.trace("The JTS ORB has been set to " + orb);
                }

                /**
                 * Set the orb manager and then set the POA to be the root POA for
                 * that ORB.  If another POA is then initialised this will be overridden.
                 */
                ORBManager.setORB(orb);
                ORBManager.setPOA(RootOA.getRootOA(orb));
            }

            /**
             * If the object passed in is an ORB Portability OA
             * then set it to be the OA used by the JTS.
             */
            if ( obj instanceof OA )
            {
                OA oa = (OA)obj;

                if (jtsLogger.logger.isTraceEnabled()) {
                    jtsLogger.logger.trace("The JTS OA has been set " + oa);
                }
                ORBManager.setPOA(oa);
            }
        }
        else
        {
            jtsLogger.i18NLogger.info_utils_ORBSetup_orbalreadyset();
        }
    }
}