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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Configuration.javatmpl 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.common;

/**
 * Runtime configuration information for the interceptors.
 *
 * @author Kevin Conner
 */

public class InterceptorInfo
{
    private static final boolean OTS_NEED_TRAN_CONTEXT ;
    private static final boolean OTS_ALWAYS_PROPAGATE ;
    
    static
    {
	boolean otsNeedTranContext = Defaults.needTransactionContext ;
	boolean otsAlwaysPropagate = Defaults.alwaysPropagateContext ;
	
        final String needTranContextValue = jtsPropertyManager.propertyManager.getProperty(Environment.NEED_TRAN_CONTEXT) ;
        
        if (needTranContextValue != null)
        {
            otsNeedTranContext = "YES".equals(needTranContextValue) ;
        }
        
        final String alwaysPropagate = jtsPropertyManager.propertyManager.getProperty(Environment.ALWAYS_PROPAGATE_CONTEXT) ;
        
        if (alwaysPropagate != null)
        {
            otsAlwaysPropagate = "YES".equals(alwaysPropagate) ;
        }
        
        OTS_NEED_TRAN_CONTEXT = otsNeedTranContext ;
        OTS_ALWAYS_PROPAGATE = otsAlwaysPropagate ;
    }
    
    /**
     * Get the flag indicating whether a transaction context is required.
     * @return true if a context is required, false otherwise.
     */
    public static boolean getNeedTranContext()
    {
	return OTS_NEED_TRAN_CONTEXT ;
    }
    
    /**
     * Get the flag indicating whether a transaction context should always be propagated.
     * @return true if a context is alwats propagated, false if it is only sent to OTS transactional objects.
     */
    public static boolean getAlwaysPropagate()
    {
	return OTS_ALWAYS_PROPAGATE ;
    }
}
