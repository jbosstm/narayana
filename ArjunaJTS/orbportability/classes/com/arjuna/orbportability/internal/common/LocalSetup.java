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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: LocalSetup.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.internal.common;

import com.arjuna.orbportability.common.Environment;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.logging.*;
import com.arjuna.common.util.logging.VisibilityLevel;
import com.arjuna.common.util.logging.DebugLevel;

public class LocalSetup extends com.arjuna.orbportability.orb.Attribute
{

    /**
     * @message com.arjuna.orbportability.internal.common.LocalSetup.invalidoption {0} - Invalid debug option {1}
     */
public void initialise (String[] params)
    {
        if (opLogger.logger.isDebugEnabled())
        {
            opLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				  FacilityCode.FAC_ORB_PORTABILITY, "LocalSetup.initialise(String[] params)");
	}

	if (params == null)
	    return;
	
	for (int i = 0; i < params.length; i++)
	{
            if (opLogger.logger.isDebugEnabled())
            {
                opLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				      FacilityCode.FAC_ORB_PORTABILITY, "Searching "+params[i]);
	    }

	    if (params[i].startsWith(LocalSetup.prefix))
	    {
		String propertyName = params[i].substring(LocalSetup.prefix.length());

                if (opLogger.logger.isDebugEnabled())
                {
                    opLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				          FacilityCode.FAC_ORB_PORTABILITY, "Got "+propertyName);
		}

                if (opLogger.logger.isDebugEnabled())
                {
                    opLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					  FacilityCode.FAC_ORB_PORTABILITY, "Setting property: "+propertyName+" with value: "+params[i+1]);
		}

		opPropertyManager.getPropertyManager().setProperty(propertyName, params[i+1]);
		i++;

		if (propertyName.compareTo(Environment.CORBA_DIAGNOSTICS) == 0)
		{
		    try
		    {
			/*
			 * Strip out any 0x extension as Java can't handle it!
			 */

			String toUse = null;

			if (params[i+1].startsWith(LocalSetup.hexStart))
			    toUse = params[i+1].substring(LocalSetup.hexStart.length());
			else
			    toUse = params[i+1];

			Integer level = Integer.valueOf(toUse, 16);
			opLogger.logger.setDebugLevel(level.intValue());

			level = null;
			toUse = null;
		    }
		    catch (Exception e)
		    {
                        if ( opLogger.loggerI18N.isWarnEnabled() )
                        {
                            opLogger.loggerI18N.warn( "com.arjuna.orbportability.internal.common.LocalSetup.invalidoption",
                                                        new Object[] { "LocalSetup.initialisse", params[i+1] } );
                        }
		    }
		    
		    i++;
		}
		
		propertyName = null;
	    }
	}
    }

public static final String prefix = "-HP_";

private static final String hexStart = "0x";

}
