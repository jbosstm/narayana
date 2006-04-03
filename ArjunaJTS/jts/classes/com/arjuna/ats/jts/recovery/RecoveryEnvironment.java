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
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveryEnvironment.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.recovery ;

/**
 * Container for property names used by recovery
 */
public class RecoveryEnvironment
{
    public static final String OTS_ISSUE_RECOVERY_ROLLBACK  = "com.arjuna.ats.jts.recovery.issueRecoveryRollback" ;
    public static final String COMMITTED_TRANSACTION_RETRY_LIMIT = "com.arjuna.ats.jts.recovery.commitTransactionRetryLimit";
    public static final String ASSUMED_OBJECT_NOT_EXIST = "com.arjuna.ats.jts.recovery.assumedObjectNotExist" ;

   /** Not used */
   private RecoveryEnvironment()
   {
   }
}
