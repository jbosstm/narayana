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
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: OAAttribute.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.debug;

import com.arjuna.orbportability.logging.opLogger;

import com.arjuna.orbportability.logging.*;
import com.arjuna.common.util.logging.VisibilityLevel;
import com.arjuna.common.util.logging.DebugLevel;

/**
 * This class prints out all of the parameters passed to it.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: OAAttribute.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

public class OAAttribute extends com.arjuna.orbportability.oa.Attribute
{
    
public void initialise (String[] params)
    {
	if (params != null)
	{
            if (opLogger.logger.isDebugEnabled())
            {
                opLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                                      FacilityCode.FAC_ORB_PORTABILITY, "ORBAttribute.initialise - parameters: ");
	    }

	    for (int i = 0; i < params.length; i++)
	    {
                if (opLogger.logger.isDebugEnabled())
                {
                    opLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                                          FacilityCode.FAC_ORB_PORTABILITY, params[i]);
                }
	    }

            if ( opLogger.logger.isDebugEnabled() )
	    {
                opLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                                      FacilityCode.FAC_ORB_PORTABILITY, "");
            }
	}
		
    }

public boolean postORBInit ()
    {
	return true;
    }
 
}
