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
 * Copyright (C) 2005,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: LocalFactory.java,v 1.1 2005/05/19 12:13:32 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.coordinator;

import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.wsc.InvalidProtocolException;

/**
 * Local coordinators can implement this interface to enable direct
 * creation of a coordinator and subordinate coordinator. Since we
 * don't know the actual implementation details, users are required
 * to determine the type dynamically.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id $
 * @since 2.0.
 */

public interface LocalFactory
{

	/**
	 * Create a new subordinate coordinator instance.
	 * 
	 * @return a new coordinator instance.
	 */
	
	public Object createSubordinate () throws NoActivityException, InvalidProtocolException, SystemException;
    
}


