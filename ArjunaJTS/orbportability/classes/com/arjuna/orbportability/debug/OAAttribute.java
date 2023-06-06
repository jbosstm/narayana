/*
 * SPDX short identifier: Apache-2.0
 */



package com.arjuna.orbportability.debug;

import com.arjuna.orbportability.logging.opLogger;



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
            if (opLogger.logger.isTraceEnabled()) {
                opLogger.logger.trace("ORBAttribute.initialise - parameters: ");
            }

	    for (int i = 0; i < params.length; i++)
	    {
                if (opLogger.logger.isTraceEnabled()) {
                    opLogger.logger.trace(params[i]);
                }
	    }

            if ( opLogger.logger.isTraceEnabled() ) {
                opLogger.logger.trace("");
            }
	}
		
    }

public boolean postORBInit ()
    {
	return true;
    }
 
}