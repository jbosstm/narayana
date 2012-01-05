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
* NonSerializableExampleXAResource.java
*
* Copyright (c) 2004 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* Created on 21-Dec-2004,13:28:06 by Arnaud Simon
*
* $Id: NonSerializableExampleXAResource.java 2342 2006-03-30 13:06:17Z  $
*/

package com.arjuna.demo.recovery.xaresource;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

/**
 * This is a trivial implementation of a non-serializable XAResource.
 * It will crash (<CODE>System.exit</CODE>) when commit
 * is called on it for the first time and will then complete normally when it is recovered.
 *
 * @author  Tom Jenkinson <tom.jenkinson@arjuna.com>
 */
public class NonSerializableExampleXAResource implements XAResource
{
   //-----------------------------------------------------------------------------------------
   //---------------  Attributes
   //-----------------------------------------------------------------------------------------

   /**
    * The transaction timeout. This is ONLY used by the get and set operations required by an XAResource. This
    * implementation of XAResource does not support transaction timeout.
    */
   private int m_timeout = 0;

   /**
    * By default, assume that this resource is not recovering.
    */
   private boolean m_recovered = false;

   /**
    * The name of this resource, may be null.
    */
   private String m_name;


   //-----------------------------------------------------------------------------------------
   //---------------  Class Constructors
   //-----------------------------------------------------------------------------------------

   /**
    * Create a new NonSerializableExampleXAResource. This resource is used to crash the VM.
    *
    * @param name       The name to associate with the XA resource.
    * @param recover    If this XA resource is recovering.
    */
   public NonSerializableExampleXAResource(String name, boolean recover)
   {
      m_name      = name;
      m_recovered = recover;
      log("NonSerializableExampleXAResource (Constructor) name: " + name + " recovering: " + recover);
   }

   /**
    * Create a new NonSerializableExampleXAResource with a default name.
    *
    */
   public NonSerializableExampleXAResource()
   {
      m_name      = "default";
      m_recovered = false;
      log("NonSerializableExampleXAResource (Recovery Constructor) name: default");
   }

   //-----------------------------------------------------------------------------------------
   //---------------  Interface XAResource methods
   //-----------------------------------------------------------------------------------------

   /**
    * Either crashes the resource (if it wasn't recovered) or commits the resource normally
    * (if it was recovered).
    *
    * @param xid       A global transaction identifier
    * @param onePhase  If true, use a one-phase commit protocol to commit the work done on behalf of <code>Xid</code>.
    *
    * @throws XAException  Not thrown by this implementation.
    *
    * @see XAResource
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      log("XA_COMMIT["+ xid + ", " + onePhase + "]");
      if ( ! m_recovered )
      {
         // If the commit is one phase we will not have prepared yet so write out the XID now
         if (onePhase)
         {
            persistXid(xid);
            log("Persisted XID = " + xid);
         }
         log("The resource has been commited, it will now crash the VM");
         System.exit(-1);
      }
      else
      {
         deleteXid();
         log("The resource has committed, deleted old XID file");
         TestXAResourceRecovery.notifyRecoveredResource();
      }
   }

   /**
    * Deletes the file containing the XID.
    *
    * @param xid           The transaction ID that the rollback is performed under.
    * @throws XAException  Not thrown by this implementation.
    *
    * @see XAResource
    */
   public void rollback(Xid xid) throws XAException
   {
      log("XA_ROLLBACK[" + xid + "]");
      TestXAResourceRecovery.notifyRecoveredResource();
   }

   /**
    * This is called to indicate to the resource that the transaction manager is ready to commit the transaction.
    * The XID is saved so that it could be recovered
    *
    * @param xid           The transaction about to be completed.
    * @return              A flag indicating whether the updates so far are read only or have resulted in a volatile
    *                      change in the data ready to be committed.
    * @throws XAException  Not thrown by this implementation.
    *
    * @see XAResource
    */
   public int prepare(Xid xid) throws XAException
   {
      log("XA_PREPARE[" + xid + "]");

      persistXid(xid);
      log("Persisted XID = " + xid);
      return XA_OK;
   }

   /**
    * Obtains a list of prepared transaction branches.
    * <p>
    * The transaction manager calls this method during recovery to obtain the list of transaction branches
    * that are currently in prepared or heuristically completed states.
    * <p>
    * This implementation returns the XID that was saved during prepare This saved XID will be commited, 
    * the newly created XID will be rolled back.
    *
    * <p>
    * Note: It is the responsibility of the Transaction Manager to ignore transactions that do not belong to it. Therefore
    * this method could return non-Arjuna XIDs which should be ignored.
    *
    * @param flag One of TMSTARTRSCAN, TMENDRSCAN, TMNOFLAGS.
    *             TMNOFLAGS must be used when no other flags are set in the parameter.
    *
    * @return zero or more XIDs of the transaction branches that are currently in a prepared or heuristically
    *         completed state.
    *
    * @throws XAException  Not thrown by this implementation.
    *
    * @see XAResource
    */
   public Xid[] recover(int flag) throws XAException
   {
      log("XA_RECOVER[" + flag + "]");
      Xid[] xids = null;
      switch (flag)
      {
         case XAResource.TMSTARTRSCAN:
            log("recover: TMSTARTRSCAN");
            Xid commitXID = readXid();
            // If the example application detects that there is an XID to commit it will return it
            if (commitXID != null)
            {
                xids = new Xid[1];
                xids[0] = commitXID;
                log("Recover returns XID to commit: " + commitXID);
            }
            // There is nothing to recover
            else
            {
                xids = new Xid[0];
            }
            break;
         case XAResource.TMENDRSCAN:
            log("recover: TMENDRSCAN");
            xids = new Xid[0];
            break;
         default:
            log("recover code: " + flag);
            xids = new Xid[0];
            break;
      }
      return xids;
   }


   /**
    * Ends the work performed on behalf of a transaction branch.
    * The resource manager disassociates the XA resource from the transaction branch specified
    * and lets the transaction complete.
    * <ul>
    * <li> If TMSUSPEND is specified in the flags, the transaction branch is temporarily suspended in an incomplete state.
    *      The transaction context is in a suspended state and must be resumed via the start method with TMRESUME specified.
    * <li> If TMFAIL is specified, the portion of work has failed. The resource manager may mark the transaction as rollback-only
    * <li> If TMSUCCESS is specified, the portion of work has completed successfully.
    * /ul>
    *
    * @param xid    A global transaction identifier that is the same as the identifier used previously in the start method
    * @param flag   One of TMSUCCESS, TMFAIL, or TMSUSPEND.
    *
    * @throws XAException  Not thrown by this implementation.
    *
    * @see XAResource
    */
   public void end(Xid xid, int flag) throws XAException
   {
      log("XA_END[" + xid + "," +  flag + "]");
   }

   /**
    * Tells the resource manager to forget about a heuristically completed transaction branch.
    *
    * @param xid A global transaction identifier
    *
    * @throws XAException  This implementation does not throw exception
    *
    * @see XAResource
    */
   public void forget(Xid xid) throws XAException
   {
      log("XA_FORGET[" + xid + "]");
   }

   /**
    * This returns the transaction timeout. The timeout is ONLY used by the get and set operations required of an
    * XAResource and does not support the timing out of transactions.
    *
    * @return              The configured transaction timeout.
    * @throws XAException  Not thrown by this implementation.
    *
    * @see XAResource
    */
   public int getTransactionTimeout() throws XAException
   {
      log("GET_TRANSACTION_TIMEOUT: " + m_timeout);
      return m_timeout;
   }

   /**
    * Is this the same resource manager. This implementation does a simple pointer comparison, though more complex
    * implementations are feasible.
    *
    * @param xaResource    The resource to compare.
    * @return              True, if the resources are identical.
    *
    * @throws XAException  Not thrown by this implementation.
    *
    * @see XAResource
    */
   public boolean isSameRM(XAResource xaResource) throws XAException
   {
      log("XA_ISSAMERM[ " + xaResource + "]");
      return xaResource.equals(this);
   }

   /**
    * Sets the current transaction timeout value for this XAResource instance.
    * Once set, this timeout value is effective until setTransactionTimeout is invoked again with a different value.
    * To reset the timeout value to the default value used by the resource manager, set the value to zero.
    *
    * @param seconds   The timeout to set (in seconds).
    * @return          True, if the transaction timeout was set OK.
    *
    * @throws XAException  Not thrown by this implementation.
    *
    * @see XAResource
    */
   public boolean setTransactionTimeout(int seconds) throws XAException
   {
      log("SET_TRANSACTION_TIMEOUT[" + seconds + "]");
      m_timeout = seconds;
      return true;
   }

   /**
    * Starts work on behalf of a transaction branch specified in xid.
    * <ul>
    * <li> If TMJOIN is specified, an exception is thrown as it is not supported
    * <li> If TMRESUME is specified, the start applies to resuming a suspended transaction specified in the parameter xid.
    * <li> If neither TMJOIN nor TMRESUME is specified and the transaction specified by xid has previously been seen by the
    * resource manager, the resource manager throws the XAException exception with XAER_DUPID error code.
    * </ul>
    *
    * @param xid   A global transaction identifier to be associated with the resource
    * @param flag  One of TMNOFLAGS, TMJOIN, or TMRESUME
    *
    * @throws XAException  Not thrown by this implementation.
    *
    * @see XAResource
    */
   public void start(Xid xid, int flag) throws XAException
   {
      log("XA_START[" + xid + "," + flag + "]");
   }

   //-----------------------------------------------------------------------------------------
   //---------------  Getter and Setter methods
   //-----------------------------------------------------------------------------------------

   /**
    * Get this resource name.
    *
    * @return This resource name.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Return true if this resource is recovering.
    *
    * @return True if this resource is recovering.
    */
   public boolean isRecovered()
   {
      return m_recovered;
   }

   /**
    * Set this resource name.
    *
    * @param name This resource name.
    */
   public void setMame(String name)
   {
      this.m_name = name;
   }

   /**
    * Set the recovery mode for this resource.
    *
    * @param recovered  The recovery mode for this resource.
    */
   public void setRecovered(boolean recovered)
   {
      this.m_recovered = recovered;
   }

   //-----------------------------------------------------------------------------------------
   //---------------  Utility methods
   //-----------------------------------------------------------------------------------------

   /**
    * Write the required message to file and to screen.
    *
    * @param msg   The message to append to the console and a file "./Resource[count].log"
    */
   protected void log(String msg)
   {
      // Assemble the formatted log statement
      String message = m_name + ": " + msg;

      // Write to screen
      System.out.println(message);

      // Write to file named
      String fileName = m_name + ".log";

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

   /**
    * Write the required XID to file for recovery.
    *
    * @param toPersist   The XID to persist in a file "./Resource[count].xid"
    */
   private void persistXid(Xid toPersist)
   {
      // Write to file named
      String fileName = m_name + ".xid";

      // Write to file
      try
      {
         File file = new File(fileName);
         FileOutputStream out = new FileOutputStream(file);
         ObjectOutputStream s = new ObjectOutputStream(out);
         s.writeObject(toPersist);
         s.flush();
         out.flush();
         s.close();
         out.close();
      }
      catch (IOException ioe)
      {
         System.err.println("Trailmap IO problem: Could not persist XID to file \"" + fileName + "\", check stack trace");
         ioe.printStackTrace();
      }
   }

   /**
    * Read the required XID from file for recovery.
    *
    * @return  The persisted XID from the file "./Resource[count].xid"
    */
   private Xid readXid()
   {
      // The XID to retuen
      Xid commitXid = null;
      // Read from file named
      String fileName = m_name + ".xid";

      // Read file if available
      File file = new File(fileName);
      if (file.exists())
      {
         try
         {
            FileInputStream out = new FileInputStream(file);
            ObjectInputStream s = new ObjectInputStream(out);
            commitXid = (Xid) s.readObject();
            s.close();
            out.close();
         }
         catch (IOException ioe)
         {
            System.err.println("Trailmap IO problem: Could not read XID persisted in file \"" + fileName + "\", check stack trace");
            ioe.printStackTrace();
         }
         catch (ClassNotFoundException cnfe)
         {
            System.err.println("Trailmap IO problem: Class not found for XID persisted in file \"" + fileName + "\", check stack trace");
            cnfe.printStackTrace();
         }
      }

      // Return the XID or null if nothing was persisted
      return commitXid;
   }

   /**
    * Deletes the XID file.
    */
   private void deleteXid()
   {
      // Delete the file named
      String fileName = m_name + ".xid";
      File file = new File(fileName);
      if (file.exists())
      {
         file.delete();
      }
   }
}
