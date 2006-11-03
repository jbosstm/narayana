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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Current.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.extensions;

import com.arjuna.ats.arjuna.coordinator.CheckedAction;

import org.omg.CORBA.SystemException;

/**
 * This interface gives access to the Arjuna specific extensions
 * to Current.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Current.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 4.0.
 */

public interface Current
{

    public void setCheckedAction (CheckedAction ca) throws SystemException;

    public CheckedAction getCheckedAction () throws SystemException;
 
}
