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
 * Copyright (c) 2003, 2004,
 *
 * Arjuna Technologies Limited.
 *
 * $Id: Volatile2PCStub.java,v 1.1.2.2 2004/06/18 15:06:09 nmcl Exp $
 */

package com.arjuna.wst.stub;

import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.wst.Volatile2PCParticipant;

public class Volatile2PCStub extends ParticipantStub implements Volatile2PCParticipant
{
    public Volatile2PCStub(final String id, final EndpointReferenceType twoPCParticipant)
        throws Exception
    {
        super(id, twoPCParticipant) ;
    }
}
