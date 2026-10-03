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
 * $Id: BusinessActivityTerminator.java,v 1.4 2004/09/09 08:48:32 kconner Exp $
 */

package com.arjuna.wst11;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

/**
 * Not in the 1.1 specification. Supposed to use participant interface.
 */

public interface BusinessActivityTerminator extends com.arjuna.wst.BusinessActivityTerminator
{
    /**
     * return either the terminator or participant endpoint depending upon what type of terminator strub this is
     * @return
     */
    public W3CEndpointReference getEndpoint() ;
}