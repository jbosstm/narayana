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
* LogInterface.java
*
* Copyright (c) 2003 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* Created on Jun 27, 2003, 4:50:15 PM by Thomas Rischbeck
*/
package com.arjuna.common.internal.util.logging;

/**
 * This interface must be implemented by any underlying log subsystem to which CLF acts as a veneer.
 *
 * The following log subsystems are supported:
 * <ul>
 *    <li>Jakarta Commons Logging (JCL). JCL can delegate to various other logging subsystems, such as:
 *    <ul>
 *       <li>log4j</li>
 *       <li>JDK 1.4 logging</li>
 *       <li>Windows NT syslog</li>
 *       <li>console</li>
 *    </ul>
 *    </li>
 *    <li>.net logging. (must be JDK 1.1 compliant for compilation by the Microsoft compiler)</li>
 * </ul>
 *
 * @author Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $ $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 * @since clf-2.0
 */
public interface LogInterface extends AbstractLogInterface
{

   /**
    * <p> Log a message with trace log level. </p>
    *
    * @param message log this message
    */
   public void trace(Object message);


   /**
    * <p> Log an error with trace log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void trace(Object message, Throwable t);


   /**
    * <p> Log a message with debug log level. </p>
    *
    * @param message log this message
    */
   public void debug(Object message);


   /**
    * <p> Log an error with debug log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void debug(Object message, Throwable t);


   /**
    * <p> Log a message with info log level. </p>
    *
    * @param message log this message
    */
   public void info(Object message);


   /**
    * <p> Log an error with info log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void info(Object message, Throwable t);


   /**
    * <p> Log a message with warn log level. </p>
    *
    * @param message log this message
    */
   public void warn(Object message);


   /**
    * <p> Log an error with warn log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void warn(Object message, Throwable t);


   /**
    * <p> Log a message with error log level. </p>
    *
    * @param message log this message
    */
   public void error(Object message);


   /**
    * <p> Log an error with error log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void error(Object message, Throwable t);


   /**
    * <p> Log a message with fatal log level. </p>
    *
    * @param message log this message
    */
   public void fatal(Object message);


   /**
    * <p> Log an error with fatal log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void fatal(Object message, Throwable t);

}
