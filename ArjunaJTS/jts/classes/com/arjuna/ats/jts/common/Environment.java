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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Environment.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.common;

/**
 * The various property values that may be specified at runtime to
 * change the configuration and behaviour of the system.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Environment.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 * @deprecated use JTSEnvironmentBean instead
 */
@Deprecated
public class Environment
{

    public static final String PROPERTIES_FILE = "com.arjuna.ats.jts.common.propertiesFile";
    public static final String TRANSACTION_MANAGER = "com.arjuna.ats.jts.transactionManager";
    public static final String NEED_TRAN_CONTEXT = "com.arjuna.ats.jts.needTranContext";
    public static final String ALWAYS_PROPAGATE_CONTEXT = "com.arjuna.ats.jts.alwaysPropagateContext";
    public static final String INTERPOSITION = "com.arjuna.ats.jts.interposition";
    public static final String CHECKED_TRANSACTIONS = "com.arjuna.ats.jts.checkedTransactions";
    public static final String SUPPORT_SUBTRANSACTIONS = "com.arjuna.ats.jts.supportSubtransactions";
    public static final String SUPPORT_ROLLBACK_SYNC = "com.arjuna.ats.jts.supportRollbackSync";
    public static final String SUPPORT_INTERPOSED_SYNCHRONIZATION = "com.arjuna.ats.jts.supportInterposedSynchronization";
    public static final String DEFAULT_TIMEOUT = "com.arjuna.ats.jts.defaultTimeout"; // deprecated
    public static final String PROPAGATE_TERMINATOR = "com.arjuna.ats.jts.propagateTerminator";
    public static final String CONTEXT_PROP_MODE = "com.arjuna.ats.jts.contextPropMode";
    public static final String RECOVERY_MANAGER_ORB_PORT = "com.arjuna.ats.jts.recoveryManagerPort";
    public static final String RECOVERY_MANAGER_ADDRESS = "com.arjuna.ats.jts.recoveryManagerAddress";
    public static final String OTS_1_0_TIMEOUT_PROPAGATION = "com.arjuna.ats.jts.ots_1_0.timeoutPropagation";
}
