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
 * AbstractLogInterface.java
 *
 * Copyright (c) 2003 Arjuna Technologies Ltd.
 * Arjuna Technologies Ltd. Confidential
 *
 * Created on Aug 6, 2003, 5:47:00 PM by Thomas Rischbeck
 */
package com.arjuna.common.internal.util.logging;

/**
 * JavaDoc
 *
 * @author Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $ $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 * @since clf-2.0
 */
public interface AbstractLogInterface
{
   /**
    * Is DEBUG logging currently enabled?
    *
    * Call this method to prevent having to perform expensive operations
    * (for example, <code>String</code> concatination)
    * when the log level is more than DEBUG.
    *
    * @return  True if the logger is enabled for DEBUG, false otherwise
    */
   boolean isDebugEnabled();

   /**
    * Is INFO logging currently enabled?
    *
    * Call this method to prevent having to perform expensive operations
    * (for example, <code>String</code> concatination)
    * when the log level is more than INFO.
    *
    * @return  True if the logger is enabled for INFO, false otherwise
    */
   boolean isInfoEnabled();

   /**
    * Is WARN logging currently enabled?
    *
    * Call this method to prevent having to perform expensive operations
    * (for example, <code>String</code> concatination)
    * when the log level is more than WARN.
    *
    * @return  True if the logger is enabled for WARN, false otherwise
    */
   boolean isWarnEnabled();

   /**
    * Is ERROR logging currently enabled?
    *
    * Call this method to prevent having to perform expensive operations
    * (for example, <code>String</code> concatination)
    * when the log level is more than ERROR.
    *
    * @return  True if the logger is enabled for ERROR, false otherwise
    */
   boolean isErrorEnabled();

   /**
    * Is FATAL logging currently enabled?
    *
    * Call this method to prevent having to perform expensive operations
    * (for example, <code>String</code> concatination)
    * when the log level is more than FATAL.
    *
    * @return  True if the logger is enabled for FATAL, false otherwise
    */
   boolean isFatalEnabled();

   /**
    * Is TRACE logging currently enabled?
    *
    * Call this method to prevent having to perform expensive operations
    * (for example, <code>String</code> concatination)
    * when the log level is more than TRACE.
    */
   public boolean isTraceEnabled();

}
