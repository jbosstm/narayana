/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * $Id: SOAPContext.java,v 1.3.24.1 2005/11/22 10:31:41 kconner Exp $
 */

package com.arjuna.mw.wsas.context.soap;

import org.w3c.dom.Element;

import com.arjuna.mw.wsas.context.Context;

/**
 */

public interface SOAPContext extends Context
{
    /**
     * Serialise the SOAP context into a DOM node.
     * @param element The element to contain the serialisation.
     * @return the element added.
     */
    public Element serialiseToElement(final Element element);

    /**
     * Initialise the implementation using the parameter provided.
     */
    public void initialiseContext(Object obj);
}
