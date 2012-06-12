/*     JBoss, Home of Professional Open Source Copyright 2008, Red Hat
 *  Middleware LLC, and individual contributors as indicated by the
 *  @author tags.
 *     See the copyright.txt in the distribution for a full listing of
 *  individual contributors. This copyrighted material is made available
 *  to anyone wishing to use, modify, copy, or redistribute it subject to
 *  the terms and conditions of the GNU Lesser General Public License, v. 2.1.
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT A WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU Lesser General Public License for more details. You should
 *  have received a copy of the GNU Lesser General Public License, v.2.1
 *  along with this distribution; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor,
 *  Boston, MA  02110-1301, USA.
 *
 *  (C) 2008,
 *  @author Red Hat Middleware LLC.
 */
package org.jboss.jbossts.fileio.xalib.txdirs.dir;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.File;

import org.apache.commons.transaction.file.*;
import org.apache.commons.transaction.util.CommonsLoggingLogger;
import org.apache.commons.logging.LogFactory;
import org.jboss.jbossts.fileio.xalib.Globals;

/**
 * This class implements methods of the standard {@link javax.transaction.xa.XAResource} interface
 * which acts like a contract between a Resource Manager and a Transaction
 * Manager.
 * <p>
 * Each instance of an <code>XAFileResourceManager</code> has its own {@link javax.transaction.xa.Xid}
 * which distinguishes by the other objects of this class and is associated
 * with a Transaction. It also has a unique transaction id which is represented by
 * a <code>String</code> and is used by {@link org.apache.commons.transaction.file.FileResourceManager}
 * to know which transaction to start, commit/rollback etc.
 * <p>
 * Any changes made within a transaction specified by the <code>Xid</code> mentioned above
 * will be applied to the transactional directory only after this <code>XAFileResourceManager</code>
 * has commited. If the decision is to rollback then all the modifications made within the transaction
 * are removed.
 *
 * @author Ioannis Ganotis
 * @version Aug 6, 2008
 */
public class XAFileResourceManager implements XAResource, Serializable
{
  private Xid currentXid;
  private String curTxId;
  transient private FileResourceManager freMngr; // todo must be setializable
  private boolean recovers;
  private String storeDir;

  /**
   * Constructor to create Resource Manager objects. Each of these
   * objects, at transaction time, are informed by the Transaction
   * Manager to prepare, commit or rollback (depending on the outcome
   * of the 2PC protocol).
   *
   * @param freMngr the Resource Manager on which methods like start, commit
   *                or rollback a transaction will be applied
   * @param curTxId the unique transaction id used by <code>freMngr</code> to
   *                know on which transaction to work
   */
  public XAFileResourceManager(FileResourceManager freMngr, String curTxId) {
    this.freMngr = freMngr;
    this.curTxId = curTxId;
    storeDir = freMngr.getStoreDir();
    recovers = false;
  }

  /**
   * Acts like the {@link org.apache.commons.transaction.file.FileResourceManager#prepareTransaction(Object)}
   * method does.
   *
   * @param xid the global transaction id
   * @throws XAException
   *         if a <code>ResourceManagerException</code> is thrown
   */
  public int prepare(Xid xid) throws XAException {
    // flush data on disk here
    System.out.println("XAFileResourceManager.prepare(Xid=" + xid + ")");
    try {
      return freMngr.prepareTransaction(curTxId);
    } catch (ResourceManagerException rme) {
      throw new XAException(rme.getMessage());
    }
  }

  /**
   * Method to commit the global transaction with the given <code>xid</code>.
   * <p/>
   * It also acts like the {@link org.apache.commons.transaction.file.FileResourceManager#commitTransaction(Object)}
   * does.
   *
   * @param xid      a global Transaction id
   * @param onePhase If true, the resource manager should use a one-phase
   *                 commit protocol to commit the work done on behalf of xid
   * @throws XAException
   *         if a <code>ResourceManagerException</code> is thrown
   */
  public void commit(Xid xid, boolean onePhase) throws XAException {
    System.out.println("XAFileResourceManager.commit(Xid=" + xid + ", onePhase=" +
        onePhase + ")");
    if (!xid.equals(currentXid)) {
      System.out.println("XAFileResourceManager.commit - wrong Xid!");
    }
    try {
      if (!recovers) {
        freMngr.commitTransaction(curTxId);
      } else {
        // the initFREM() method will take care of incomplete txs
      }
    } catch (ResourceManagerException rme) {
      throw new XAException(rme.getMessage());
    }
    currentXid = null;
  }

  /**
   * Ends the work performed on behalf of a transaction branch.
   *
   * @param xid   a global transaction identifier that is the same as what was used
   *              previously in the start method
   * @param flags (can be anything)
   */
  public void end(Xid xid, int flags) {
    System.out.println("XAFileResourceManager.end(Xid=" + xid + ", flags=" + flags + ")");
  }

  /**
   * Forget about a heuristically completed transaction branch.
   * The <code>currentXid</code> is set to null
   *
   * @param xid a global Transaction id
   */
  public void forget(Xid xid) {
    System.out.println("XAFileResourceManager.forget(Xid=" + xid + ")");
    if (!xid.equals(currentXid)) {
      System.out.println("XAFileResourceManager.forget - wrong Xid!");
    }
    currentXid = null;
  }

  /**
   * Obtain the current transaction timeout value set for this
   * <code>XAFileResourceManager</code> instance. If
   * <code>XAFileResourceManager.setTransactionTimeout</code> was not used prior to
   * invoking this method, the return value is the default timeout set for
   * the resource manager; otherwise, the value used in the previous
   * <code>setTransactionTimeout</code> call is returned.
   *
   * @return the transaction timeout value in seconds
   */
  public int getTransactionTimeout() throws XAException {
    try {
      int timeout = (int) freMngr.getTransactionTimeout(curTxId) / 1000; // ms -> secs
      System.out.println("XAFileResourceManager.getTransactionTimeout() [returning " + timeout + "]");
      return timeout;
    } catch (ResourceManagerException rme) {
      throw new XAException(rme.getMessage());
    }
  }

  /*
  * No implementation.
  * @param xares an XAResource object whose resource manager instance is to
  *              be compared with the resource manager instance of the target
  *              object
  * @return always false
  */
  public boolean isSameRM(XAResource xares) throws XAException {
    System.out.println("XAFileResourceManager.isSameRM(xaresMngr=" + xares + ")");
    return false;
  }

  /*
  * No implementation.
  * @param flag
  * @return always zero xids
  */
  public Xid[] recover(int flag) throws XAException {
    System.out.println("XAFileResourceManager.recover(flag=" + flag + ")");
    return new Xid[0];
  }

  /**
   * Rollback any updates attempted to be written to the directory.
   * <p/>
   * The method acts like the
   * {@link org.apache.commons.transaction.file.FileResourceManager#rollbackTransaction(Object)}
   * does.
   * @param xid a global Transaction id
   * @throws XAException
   *         if a <code>ResourceManagerException</code> is thrown
   */
  public void rollback(Xid xid) throws XAException {
    System.out.println("XAFileResourceManager.rollback(Xid=" + xid + ")");
    try {
      freMngr.rollbackTransaction(curTxId);
    } catch (ResourceManagerException rme) {
      throw new XAException(rme.getMessage());
    }
    currentXid = null;
  }

  /**
   * Set the current transaction <code>timeout</code> value for this
   * <code>XAFileResourceManager</code> instance. Once set, this timeout value is
   * effective until <code>setTransactionTimeout</code> is invoked again with
   * a different value.
   * The method acts like the
   * {@link org.apache.commons.transaction.file.FileResourceManager#setTransactionTimeout(Object, long)}
   * does.
   *
   * @param seconds transaction timeout value in seconds
   * @return true if transaction timeout value is set successfully;
   *         otherwise false
   */
  public boolean setTransactionTimeout(int seconds) throws XAException {
    System.out.println("XAFileResourceManager.setTransactionTimeout(timeout=" + seconds + ")");
    try {
      freMngr.setTransactionTimeout(curTxId, seconds * 1000);
    } catch (ResourceManagerException rme) {
      throw new XAException(rme.getMessage());
    }
    return false;
  }

  /**
   * Start work on behalf of a transaction branch specified in <code>xid</code>.
   * The method act like the
   * {@link org.apache.commons.transaction.file.FileResourceManager#startTransaction(Object)}
   * does.
   * @param xid   a global Transaction id to be associated with this
   *              Resource Manager instance
   * @param flags (can be anything)
   * @throws XAException if there is already a Transaction
   */
  public void start(Xid xid, int flags) throws XAException {
    System.out.println("XAFileResourceManager.start(Xid=" + xid + ", flags=" +
        flags + ")");
    if (currentXid != null) {
      System.out.println("XAFileResourceManager.start - wrong Xid!");
      throw new XAException("Current Transaction is: <" + currentXid +
          ">\nCannot start the new Transaction.");
    }
    try {
      freMngr.startTransaction(curTxId);
    } catch (ResourceManagerException rme) {
      throw new XAException(rme.getMessage());
    }
    currentXid = xid;
  }

  /**
   * Returns the global Transaction id associated with this
   * <code>XAFileResourceManager</code>
   * @return the global Transaction id associated with this
   *         <code>XAFileResourceManager</code>
   */
  public Xid getCurrentXid() {
    return currentXid;
  }

  /**
   * After a system crash and upon recovery phase the <code>FileResourceManager</code>
   * object needs to be re-initialised.
   */
  private void initFREM() {
    String workDir = storeDir + "/" + Globals.WORK_DIR_NAME;
    freMngr = new FileResourceManager(storeDir, workDir, false,
        new CommonsLoggingLogger(LogFactory.getLog(XADir.class.getName())));
    try {
      freMngr.start(); // will automatically recover incomplete txs
    } catch (ResourceManagerSystemException e) {
      e.printStackTrace();
    }
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    recovers = true; // indicate that the XAFileResourceManager recovers now
    initFREM();
  }
}
