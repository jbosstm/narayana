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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Environment.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jta.common;

/**
 * The module specific properties which may be set to configure the
 * system at runtime.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: Environment.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class Environment
{
    public static final String PROPERTIES_FILE = "com.arjuna.ats.jta.common.propertiesFile";
    public static final String SUPPORT_SUBTRANSACTIONS = "com.arjuna.ats.jta.supportSubtransactions";

    public static final String JTA_TM_IMPLEMENTATION = "com.arjuna.ats.jta.jtaTMImplementation";
    public static final String JTA_UT_IMPLEMENTATION = "com.arjuna.ats.jta.jtaUTImplementation";
	public static final String JTA_TSR_IMPLEMENTATION = "com.arjuna.ats.jta.jtaTSRImplementation";

    public static final String XA_BACKOFF_PERIOD = "com.arjuna.ats.jta.xaBackoffPeriod";
    public static final String XA_RECOVERY_NODE = "com.arjuna.ats.jta.xaRecoveryNode";
    public static final String XA_ROLLBACK_OPTIMIZATION = "com.arjuna.ats.jta.xaRollbackOptimization";
    public static final String XA_ASSUME_RECOVERY_COMPLETE = "com.arjuna.ats.jta.xaAssumeRecoveryComplete";

    public static final String UT_JNDI_CONTEXT = "com.arjuna.ats.jta.utils.UTJNDIContext";
    public static final String TM_JNDI_CONTEXT = "com.arjuna.ats.jta.utils.TMJNDIContext";
	public static final String TSR_JNDI_CONTEXT = "com.arjuna.ats.jta.utils.TSRJNDIContext";

	public static final String XA_ERROR_HANDLER = "com.arjuna.ats.jta.xaErrorHandler";
    public static final String XA_TRANSACTION_TIMEOUT_ENABLED = "com.arjuna.ats.jta.xaTransactionTimeoutEnabled";
    public static final String LAST_RESOURCE_OPTIMISATION_INTERFACE = "com.arjuna.ats.jta.lastResourceOptimisationInterface";
    public static final String ALLOW_MULTIPLE_LAST_RESOURCES = "com.arjuna.ats.jta.allowMultipleLastResources";

}

