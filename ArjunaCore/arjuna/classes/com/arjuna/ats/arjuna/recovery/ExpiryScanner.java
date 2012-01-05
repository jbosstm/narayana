/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
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
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ExpiryScanner.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.recovery ;

/**
 * Interface for Expiry scanner plug-ins.
 * ExpiryScanners check for ancient entries in the ObjectStore (or elsewhere) to 
 * avoid leaving long-dead and irrelevant entries lying around for ever.
 * The scan() method of each registered ExpiryScanner implementation is called 
 * by the RecoveryManager at an interval expiryScanInterval (hours).
 */

public interface ExpiryScanner
{
   /**
    * perform a scan
    */
   public void scan() ;

   /**
    * Is this scanner to be used.  (E.g. if zero age means "don't remove", and it
    * has been set to zero, toBeUsed replies false)
    */
   public boolean toBeUsed() ;
}
