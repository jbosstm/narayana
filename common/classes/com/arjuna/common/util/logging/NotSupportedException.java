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
package com.arjuna.common.util.logging;

/**
 * The NotSupportedException class is used to inform on the fact that a particular 
 * logging implementation is not implemented or on the fact that it does not recognize
 * a particular parameter. For instance, if the underlying logging service is log4j, it is
 * an error to register a handler object implementing the HP LogWriter interface. 
 *
 * @author Malik SAHEB - malik.saheb@arjuna.com
 * @since 1.0
 * @version $Id: NotSupportedException.java 2342 2006-03-30 13:06:17Z  $
 *
 * @deprecated use OperationNotSupportedException from now on.
 */

public class NotSupportedException extends Exception {

   /**
    * Constructs a new instance of NotSupportedException
    */
   public NotSupportedException() {
      super();
   }

   /**
    * Constructs a new instance of NotSupportedException using a message for explanation
    */
   public NotSupportedException(String message) {
      super(message);
   }

}
