/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * Copyright (C) 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ORBSetup.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.utils;

import com.arjuna.orbportability.utils.InitClassInterface;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.internal.InternalORB;
import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.exceptions.FatalError;


import com.arjuna.ats.internal.jts.ORBManager;

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
     *
     * @message com.arjuna.ats.jts.utils.ORBSetup.orbalreadyset The ORBManager is already associated with an ORB/OA.
     */
    public void invoke(Object obj)
    {
	if ( !ORBManager.isInitialised() )
        {
            if (jtsLogger.logger.isDebugEnabled()) {
                jtsLogger.logger.debug("The ORBSetup.invoke method has been invoked");
            }

            /**
             * If the object passed in is an ORB Portability ORB
             * then set it to be the ORB used by the JTS.
             */
            if ( obj instanceof ORB )
            {
                ORB orb = (ORB)obj;

                if ( !( orb instanceof InternalORB ) )
                {
                    if (jtsLogger.logger.isDebugEnabled()) {
                        jtsLogger.logger.debug("The JTS ORB has been set to " + orb);
                    }

                    /**
                     * Set the orb manager and then set the POA to be the root POA for
                     * that ORB.  If another POA is then initialised this will be overridden.
                     */
                    ORBManager.setORB(orb);
                    ORBManager.setPOA(RootOA.getRootOA(orb));
                }
            }

            /**
             * If the object passed in is an ORB Portability OA
             * then set it to be the OA used by the JTS.
             */
            if ( obj instanceof OA )
            {
                OA oa = (OA)obj;

                if ( ( oa != null ) && ( !( oa.getAssociatedORB() instanceof InternalORB ) ) )
                {
                    if (jtsLogger.logger.isDebugEnabled()) {
                        jtsLogger.logger.debug("The JTS OA has been set " + oa);
                    }
                    ORBManager.setPOA(oa);
                }
            }
        }
        else
        {
            if ( jtsLogger.loggerI18N.isDebugEnabled() )
            {
                jtsLogger.loggerI18N.warn( "com.arjuna.ats.jts.utils.ORBSetup.orbalreadyset" );
            }
        }
    }
}
