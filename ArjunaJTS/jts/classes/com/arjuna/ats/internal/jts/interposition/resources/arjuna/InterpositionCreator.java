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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: InterpositionCreator.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.interposition.resources.arjuna;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;

import com.arjuna.ats.internal.jts.interposition.FactoryCreator;

import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.Control;

import org.omg.CORBA.SystemException;

public class InterpositionCreator implements FactoryCreator
{

public ControlImple recreateLocal (PropagationContext ctx) throws SystemException
    {
	return Interposition.create(ctx);
    }

public Control recreate (PropagationContext ctx) throws SystemException
    {
	return recreateLocal(ctx).getControl();
    }

}
