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
* Copyright (C) 2004
* Arjuna Technologies Limited
* Newcastle upon Tyne, UK
*
* $Id: NonSerializableExampleXAResourceRecovery.java 2342 2006-03-30 13:06:17Z  $
*/

package com.arjuna.demo.recovery.xaresource;

import com.arjuna.ats.jta.recovery.XAResourceRecovery;

import javax.transaction.xa.XAResource;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * This class shows a simple implementation of XAResourceRecovery. It shows how even though
 * NonSerializableExampleXAResource is not <CODE>Serializable</CODE> the resource is recovered
 * via this recovery module that is used by the recovery manager to periodically scan the resource
 * for indoubt transaction XIDs.
 *
 * An <CODE>XAResourceRecovery</CODE> class is analagous to an <CODE>Iterator</CODE> or <CODE>Enumeration</CODE>.
 * Implementations will have their getXAResource method invoked each time they return true to hasMoreResources.
 */
public class NonSerializableExampleXAResourceRecovery implements XAResourceRecovery
{
   //-----------------------------------------------------------------------------------------
   //---------------  Attributes
   //-----------------------------------------------------------------------------------------

   /**
    * The name of this resource recovery, may be null.
    */
   private static final String NAME = "NonSerializableExampleXAResourceRecovery";

   /**
    * Used to count the times the resource has been returned to the transaction manager.
    */
   private int m_count = 0;

   /**
    * A reference to the non-Serializable XAResource.
    */
   private NonSerializableExampleXAResource m_nonSerializableExampleXAResource = null;

   //-----------------------------------------------------------------------------------------
   //---------------  Class Constructors
   //-----------------------------------------------------------------------------------------

   /**
    * Default constructor is only provided in order to log when one is created.
    */
   public NonSerializableExampleXAResourceRecovery ()
   {
      log(" (Constructor)");
   }

   //-----------------------------------------------------------------------------------------
   //---------------  Methods of the interface XAResourceRecovery
   //-----------------------------------------------------------------------------------------

   /**
    * Initialise with all properties required to create the resource(s).
    *
    * @param initializer   An arbitrary string from which initialization data is obtained.
    *
    * @return              <CODE>true</CODE> if initialization happened successfully, <CODE>false</CODE> otherwise.
    *
    * @throws SQLException Not thrown by this implementation.
    */
   public boolean initialise(String initializer) throws SQLException
   {
      log("initialise[" + initializer + "]");
      return true;
   }

   /**
    * Check to see if their are any more resources to recover.
    *
    * @return  <CODE>true</CODE> if this instance can provide more resources, <CODE>false</CODE> otherwise.
    */
   public boolean hasMoreResources()
   {
      log("hasMoreResources[]");
      // Check if the resource has been recovered yet
      boolean toReturn = false;
      // We know there is only one resource to recover, this method will return true twice
      if (m_count < 2)
      {
         toReturn = true;
         m_count++;
      }
      log("hasMoreResources: " + toReturn);
      return toReturn;
   }

   /**
    * Get an XAResource. This implementation will create the next XA resource to recover.
    *
    * @return              An <CODE>NonSerializableExampleXAResource</CODE>.
    *
    * @throws SQLException Not thrown by this implementation.
    */
   public XAResource getXAResource() throws SQLException
   {
      String resourceName = "Non_Serializable_Resource";
      log("getXAResource: " + resourceName);
      // If the non serializable resource has not been recovered yet
      if (m_nonSerializableExampleXAResource == null)
      {
          // Create a new non-serializable example XAResource.
          m_nonSerializableExampleXAResource = new NonSerializableExampleXAResource(resourceName, true);
      }
      // Return the non serializable XAResource
      return m_nonSerializableExampleXAResource;
   }

   //-----------------------------------------------------------------------------------------
   //---------------  Utility methods
   //-----------------------------------------------------------------------------------------

   /**
    * Write the required message to file and to screen.
    *
    * @param msg   The message to append to the console and a file "./Resource[count].log"
    */
   private static void log(String msg)
   {
      // Assemble the formatted log statement
      String message = NAME + ": " + msg;

      // Write to screen
      System.out.println(message);

      // Write to file named
      String fileName = NAME + ".log";

      // Write to file
      try
      {
         FileWriter fw = new FileWriter(fileName, true);
         PrintWriter pw = new PrintWriter(fw);
         pw.println(message);
         pw.flush();
         pw.close();
         fw.close();
      }
      catch (IOException ioe)
      {
         System.err.println("Trailmap IO problem: Could not log to file \"" + fileName + "\", check stack trace");
         ioe.printStackTrace();
      }
   }
}
