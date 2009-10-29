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
 * LogFactoryInterface.java
 *
 * Copyright (c) 2003 Arjuna Technologies Ltd.
 * Arjuna Technologies Ltd. Confidential
 *
 * Created on Jun 30, 2003, 10:23:01 AM by Thomas Rischbeck
 */
package com.arjuna.common.internal.util.logging;

import com.arjuna.common.util.exceptions.LogConfigurationException;

/**
 * Interface that gives a handle to the underlying log subsystem's log factory.
 *
 * We provide an implementation for Jakarta Commons Logging (JCL) and a simple built-in one.
 * Users may provide additional implementations, see LoggingEnvironmentBean.logFactory for config.
 *
 * @author Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $ $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 * @since clf-2.0
 */
public interface LogFactoryInterface
{
   /**
    * Method to return a named logger.
    *
    * @param name Logical name of the <code>Log</code> instance to be
    *  returned (the meaning of this name is only known to the underlying
    *  logging implementation that is being wrapped)
    *
    * @exception LogConfigurationException if a suitable <code>LogInterface</code>
    *  instance cannot be returned
    */
   public LogInterface getLog(String name) throws LogConfigurationException;
}
