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
 * $Id:$
 */

package com.arjuna.mw.wscf.protocols;

import com.arjuna.mw.wscf.protocols.ProtocolManager;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ProtocolRegistry.java,v 1.2 2003/03/04 12:55:56 nmcl Exp $
 * @since 1.0.
 */

// TODO we need a separate instance for WSTX

public class ProtocolRegistry
{

    public static ProtocolManager sharedManager ()
    {
	return _shared;
    }

    public static ProtocolManager createManager ()
    {
	return new ProtocolManager();
    }

    private static ProtocolManager _shared = new ProtocolManager();

}