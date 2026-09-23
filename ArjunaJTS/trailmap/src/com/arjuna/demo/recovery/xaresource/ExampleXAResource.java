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
* ExampleXAResource.java
*
* Copyright (c) 2004 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* Created on 21-Dec-2004,13:28:06 by Arnaud Simon
*
* $Id: ExampleXAResource.java 2342 2006-03-30 13:06:17Z  $
*/

package com.arjuna.demo.recovery.xaresource;

import java.io.IOException;
import java.io.Serializable;

/**
 * This is a trivial implementation of a Serializable XAResource. It will crash (<CODE>System.exit</CODE>) when commit
 * is called on it for the first time and will then complete normally when it is recovered.
 *
 * @author  Arnaud Simon <Arnaud.Simon@arjuna.com>
 */
public class ExampleXAResource extends NonSerializableExampleXAResource implements Serializable
{

   //-----------------------------------------------------------------------------------------
   //---------------  Class Constructors
   //-----------------------------------------------------------------------------------------

   /**
    * Create a new ExampleXAResource. This resource is used to crash the VM.
    *
    * @param name          The name to associate with the XA resource.
    */
   public ExampleXAResource(String name)
   {
      super(name, false);
      log("ExampleXAResource (Constructor) name: " + name);
   }

   //-----------------------------------------------------------------------------------------
   //---------------  Utility methods
   //-----------------------------------------------------------------------------------------

   /**
    * Override default serialization code.
    *
    * @param out           The stream to write the state of this object to.
    * @throws IOException  Not thrown by this implementation.
    */
   private void writeObject(java.io.ObjectOutputStream out)
         throws IOException
   {
      log("Serialized");
      out.writeUTF( getName() );
   }

   /**
    * Override default serialization code.
    *
    * @param in                        The stream to read the state of this object from.
    * @throws IOException              Not thrown by this implementation.
    */
   private void readObject(java.io.ObjectInputStream in)
         throws IOException
   {
      setMame( in.readUTF() );
      setRecovered( true );
      log("Deserialized");
   }
}
