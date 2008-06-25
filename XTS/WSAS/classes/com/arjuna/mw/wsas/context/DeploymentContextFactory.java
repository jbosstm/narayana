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
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DeploymentContextFactory.java,v 1.4 2004/03/15 13:25:00 nmcl Exp $
 */

package com.arjuna.mw.wsas.context;

import com.arjuna.mw.wsas.common.Environment;

import com.arjuna.mwlabs.wsas.context.DeploymentContextImple;

/**
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: DeploymentContextFactory.java,v 1.4 2004/03/15 13:25:00 nmcl Exp $
 * @since 1.0.
 */

/*
 * This is left over from an attempt at some fancy stuff in an early
 * version of the WS-AS spec. That is not gone and this class (and all of
 * its users) needs to go to. Braindead! Uggh!!
 */

public class DeploymentContextFactory
{
    
    public static DeploymentContext deploymentContext ()
    {
	return _deployContext;
    }
    
    private static DeploymentContext _deployContext = null;

    static
    {
	String contextImple = System.getProperty(Environment.DEPLOYMENT_CONTEXT);

	try
	{
	    if (contextImple != null)
	    {
		Class c = Class.forName(contextImple);

		_deployContext = (DeploymentContext) c.newInstance();
	    }
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
	
	if (_deployContext == null)
	    _deployContext = new DeploymentContextImple();
    }
    
}
