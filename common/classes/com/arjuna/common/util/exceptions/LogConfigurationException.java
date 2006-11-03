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
* LogConfigurationException.java
*
* Copyright (c) 2003 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* Created on Jun 30, 2003, 10:35:59 AM by Thomas Rischbeck
*/
package com.arjuna.common.util.exceptions;

/**
 * An exception that is thrown only if a suitable <code>LogFactory</code>
 * or <code>Log</code> instance cannot be created by the corresponding
 * factory methods.
 *
 * @author Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $ $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 * @since clf-2.0
 */
public class LogConfigurationException extends RuntimeException
{
   /**
    * Construct a new exception with <code>null</code> as its detail message.
    */
   public LogConfigurationException() {
      super();
   }


   /**
    * Construct a new exception with the specified detail message.
    *
    * @param message The detail message
    */
   public LogConfigurationException(String message) {
      super(message);
   }


   /**
    * Construct a new exception with the specified cause and a derived
    * detail message.
    *
    * @param cause The underlying cause
    */
   public LogConfigurationException(Throwable cause) {
      this((cause == null) ? null : cause.toString(), cause);
   }


   /**
    * Construct a new exception with the specified detail message and cause.
    *
    * @param message The detail message
    * @param cause The underlying cause
    */
   public LogConfigurationException(String message, Throwable cause) {
      super(message);
      this.cause = cause; // Two-argument version requires JDK 1.4 or later
   }


   /**
    * The underlying cause of this exception.
    */
   protected Throwable cause = null;


   /**
    * Return the underlying cause of this exception (if any).
    */
   public Throwable getCause() {
      return (this.cause);
   }
}
