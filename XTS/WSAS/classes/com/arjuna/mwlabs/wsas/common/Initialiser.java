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
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Initialiser.java,v 1.2 2005/03/10 15:37:01 nmcl Exp $
 */

package com.arjuna.mwlabs.wsas.common;

import com.arjuna.mw.wsas.logging.wsasLogger;

import com.arjuna.mw.wsas.common.Environment;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Initialiser.java,v 1.2 2005/03/10 15:37:01 nmcl Exp $
 * @since 1.0.
 */

public class Initialiser
{

    /**
     * @message com.arjuna.mwlabs.wsas.common.Initialiser_1 [com.arjuna.mwlabs.wsas.common.Initialiser_1] - Activity timeout format incorrect: 
     */

    public static final synchronized void initialise ()
    {
	if (!_done)
	{
	    try
	    {
		String timeout = System.getProperty(Environment.ACTIVITY_TIMEOUT);
		
		try
		{
		    Integer l = new Integer(timeout);
		    _defaultTimeout = l.intValue();
		}
		catch (NumberFormatException e)
		{
		    wsasLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsas.common.Initialiser_1",
						  new Object[]{e});
		}
	    }
	    catch (Exception ex)
	    {
	    }
	    
	    _done = true;
	}
    }

    private static boolean _done = false;
    private static int     _defaultTimeout = 0;
    
}

