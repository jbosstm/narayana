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
package org.jboss.jbossts.fileio.xalib.txfiles.file;

import org.jboss.jbossts.fileio.xalib.txfiles.logging.LogEntry;
import org.jboss.jbossts.fileio.xalib.txfiles.logging.RecordsLogger;
import org.jboss.jbossts.fileio.xalib.Globals;
import org.jboss.jbossts.fileio.xalib.txfiles.file.DataRecord;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * This class implements methods of the standard {@link javax.transaction.xa.XAResource} interface
 * which acts like a contract between a Resource Manager and a Transaction
 * Manager.
 * <p>
 * Each instance of an <code>XAResourceManager</code> has its own {@link javax.transaction.xa.Xid}
 * which distinguishes by the other objects of this class and is associated
 * with a Transaction. Each <code>XAResourceManager</code> instance has also
 * a <code>log</code> ({@link RecordsLogger} object) to write information
 * about the bytes that have been updated by <code>XAFile</code>. The updates
 * are also added into a hashtable (depending on the starting position of the bytes
 * that make up a {@link DataRecord} each time. If no crash has occured the
 * Resource manager will retrieve information about the requested records using
 * its hashtable in memory. In a situation where a system failure occured and
 * the system tries to recover, the logs will be used to obtain the requested
 * information.
 * <p>
 * The requested information will be written back to the original file only if
 * Transaction Manager decides to commit and asks from the Resource Manager to
 * commit and delete the log file. If Transaction Manager decides a rollback
 * operation the Resource Manager invokes its own rollback operation which will
 * cause all updates made so far to be removed and the log file to be deleted. 
 * When the log is deleted (either in commit or rollback invocations) the thread
 * is disassociated from the corresponding transaction.
 *
 * @author Ioannis Ganotis
 * @version Jun 13, 2008
 *
 * @see XAFile
 * @see RecordsLogger
 * @see DataRecord
 */
public class XAResourceManager implements XAResource, Serializable
{
  private Xid currentXid;
  private int timeout;
  private XAFile xaFile;
  private RecordsLogger log;
  private long th_id;
  transient private final int DEFAULT_TIMEOUT = 60;
  private boolean recovers;
  transient private Hashtable<Long, Integer> updatedBytes;

  /**
   * Constructor to create Resource Manager objects. Each of these
   * objects, at transaction time, are informed by the Transaction
   * Manager to prepare, commit or rollback (depending on the outcome
   * of the 2PC protocol). Each of these Resource Manager objects are
   * kept in a hashtable in the XAFile and are associated with the
   * thread id that created the corresponding Transaction.
   * @param xaFile the file instance on which updates take place
   * @param log the logger object which keeps update-relative information
   * @param th_id the thread id that created the transaction in which
   *              this <code>XAResourceManager</code> object has been
   *              enlisted.
   * @throws IOException if an I/O error occurs
   */
  protected XAResourceManager(XAFile xaFile, RecordsLogger log,
                              long th_id) throws IOException {
    this.xaFile = xaFile;
    this.log = log;
    this.th_id = th_id;
    timeout = DEFAULT_TIMEOUT;
    recovers = false;
    updatedBytes = new Hashtable<Long, Integer>(89);
  }

  /**
   * Method to prepare a transaction with the given <code>xid</code> to
   * commit.
   * <p>
   * The method forces system-memory buffers to write their data to the
   * log file to ensure all the updates that are to be applied to the
   * file are included in the log. The log file is then closed.
   *
   * @param xid a global Transaction id
   * @return  <code>XA_OK</code> after synchronizing the log
   * @exception XAException if an error occured while synchronizing the log
   */
  public int prepare(Xid xid) throws XAException {
    // flush data on disk here
    System.out.println("XAResourceManager.prepare(Xid=" + xid + "), th_id=" + th_id);
    try {
      log.flush();
      log.close();
    } catch (IOException e) {
      throw new XAException("Unable to flush data to the log file <" +
          log.getFilename() + ">.");
    }
    return XAResource.XA_OK;
  }

  /**
   * Method to commit the global transaction with the given <code>xid</code>.
   * <p>
   * If this method is not invoked by a Recovery Manager, then the updated
   * bytes will be read from the hashtable that contains them and will be
   * written back to the original source file. In case there was a system
   * failure, the same procedure will be followed but the updated bytes
   * will be read from the log file instead of the memory, as memory will
   * contain no relative information during recovery.
   *
   * @param xid a global Transaction id
   * @param onePhase If true, the resource manager should use a one-phase
   *                 commit protocol to commit the work done on behalf of xid
   * @exception XAException if an I/O error occurs when calling the
   *                        <code>commitChanges</code> method
   */
  public void commit(Xid xid, boolean onePhase) throws XAException {
    System.out.println("XAResourceManager.commit(Xid=" + xid + ", onePhase=" + onePhase + "), th_id=" + th_id);
    if (!xid.equals(currentXid)) {
      System.out.println("XAResourceManager.commit - wrong Xid!");
    }
//System.exit(1);            // todo testcode ----------------------#################################---------------------

//    try {
//    if (th_id == 10l) {
//      Thread.sleep(3000);
//    }
//    if (th_id == 11l) {
//      Thread.sleep(3000);
//    }
////    if (th_id == 12l) {
////      System.exit(1);
////    }
//  } catch (InterruptedException e) {
//    e.printStackTrace();
//  }
    try {
      commitChanges();
//      if (th_id == 12l)
//        throw new IOException("stupid exception!");
    } catch (IOException ioe)
    {
      ioe.printStackTrace();
      throw new XAException("Commit failed! Possibly an I/O error occurd while " +
          "using the log file <" + log.getFilename() + ">" );
    }

    log.delete();
    System.out.println("Original File Updated Successfully.");

    currentXid = null;

  }

  /**
   * Ends the work performed on behalf of a transaction branch.
   *
   * @param xid a global transaction identifier that is the same as what was used
   *            previously in the start method
   * @param flags (can be anything)
   */
  public void end(Xid xid, int flags) {
    System.out.println("XAResourceManager.end(Xid=" + xid + ", flags=" + flags + "), th_id=" + th_id);
  }

  /**
   * Forget about a heuristically completed transaction branch.
   * The <code>currentXid</code> is set to null
   * @param xid a global Transaction id
   */
  public void forget(Xid xid) {
    System.out.println("XAResourceManager.forget(Xid=" + xid + ")");
    if (!xid.equals(currentXid)) {
      System.out.println("XAResourceManager.forget - wrong Xid!");
    }
    currentXid = null;
  }

  /**
   * Obtain the current transaction timeout value set for this
   * <code>XAResourceManager</code> instance. If
   * <code>XAResourceManager.setTransactionTimeout</code> was not used prior to 
   * invoking this method, the return value is the default timeout set for
   * the resource manager; otherwise, the value used in the previous
   * <code>setTransactionTimeout</code> call is returned.
   *
   * @return the transaction timeout value in seconds
   */
  public int getTransactionTimeout() {
    System.out.println("XAResourceManager.getTransactionTimeout() [returning " + timeout + "]");
    return timeout;
  }

  /*
  * No implementation.
  * @param xares an XAResource object whose resource manager instance is to
  *              be compared with the resource manager instance of the target
  *              object
  * @return always false
  */
  public boolean isSameRM(XAResource xares) {
    System.out.println("XAResourceManager.isSameRM(xares=" + xares + ")");
    return false;
  }

  /*
   * No implementation.
   * @param flag
   * @return always zero xids
   */
  public Xid[] recover(int flag) {
    System.out.println("XAResourceManager.recover(flag=" + flag + ")");
    return new Xid[0];
  }

  /**
   * Rollback any updates attempted to be written to the file.
   * <p>
   * The method closes the log file and deletes it as it is not
   * useful anymore. It also deletes the bytes from the hashtable
   * and disassociates the transaction with the given <code>xid</code>
   * from the thread that initiated it, if not called by the Recovery
   * Manager.
   *
   * @param xid a global Transaction id
   * @exception XAException
   *            if an error occured while trying to disassociate the
   *            the given <code>xid</code> from its initiator thread
   */
  public void rollback(Xid xid) throws XAException {
    System.out.println("XAResourceManager.rollback(Xid=" + xid + "), th_id=" + th_id);
    long th_id = Globals.RECOVERY_ID;
    if (!xid.equals(currentXid)) {
      System.out.println("XAResourceManager.rollback - wrong Xid!");
    }

    if (!recovers) { // normal operation (memory)
      th_id = this.th_id;
      updatedBytes.clear();
    }
    try {
      xaFile.removeTransaction(currentXid, recovers);
    } catch (IOException e) {
      e.printStackTrace();
      throw new XAException("Rollback failed. Could not disassociate " +
          "the current thread " + th_id + "from the transaction with xid=<" + xid);
    }
    log.close();
    log.delete();
    currentXid = null;
  }

  /**
   * Set the current transaction <code>timeout</code> value for this
   * <code>XAResourceManager</code> instance. Once set, this timeout value is
   * effective until <code>setTransactionTimeout</code> is invoked again with
   * a different value. To reset the timeout value to the default value used by
   * the resource manager, set the value to zero. If the timeout operation is
   * performed successfully, the method returns true; otherwise false. If a
   * resource manager does not support transaction timeout value to be set
   * explicitly, this method returns false
   * @param seconds transaction timeout value in seconds
   * @return true if transaction timeout value is set successfully;
   *         otherwise false
   */
  public boolean setTransactionTimeout(int seconds) {
    System.out.println("XAResourceManager.setTransactionTimeout(timeout=" + seconds + ")");
      if (seconds >= 0) {
        timeout = seconds;
        if (timeout == 0)
          timeout = DEFAULT_TIMEOUT;
        return true;
      }
    return false;
  }

  /**
   * Start work on behalf of a transaction branch specified in <code>xid</code>.
   * @param xid a global Transaction id to be associated with this
   *            Resource Manager instance
   * @param flags (can be anything)
   * @throws XAException if there is already a Transaction
   */
  public void start(Xid xid, int flags) throws XAException {
    System.out.println("XAResourceManager.start(Xid=" + xid + ", flags=" +
        flags + "), th_id=" + th_id);
    if (currentXid != null) {
      System.out.println("XAResourceManager.start - wrong Xid!");
      throw new XAException("Current Transaction is: <" + currentXid +
          ">\nCannot start the new Transaction.");
    }
    currentXid = xid;
  }

  /**
   * The method is invoked by the <code>commit</code> method
   * of the <code>XAResourceManager</code> and performs the
   * work as specified in that method.
   *
   * @exception IOException If an I/O error occurs while reading the log
   */
  private void commitChanges() throws IOException {
    long th_id = this.th_id;
    if (recovers) { // after a crash occured
      th_id = Globals.RECOVERY_ID;
    }
    LinkedList<DataRecord> records = retrieveRecords();

    for (DataRecord dr : records) {
      xaFile.commitUpdates(dr.getStartPosition(),
          dr.getRecordLength(), dr.getRecordBytes(), th_id);
    }
    xaFile.sync(); // Force updates to be written to the file
    if (!recovers) { // normal operation(memory)
      updatedBytes.clear();
    }
    xaFile.removeTransaction(currentXid, recovers);
  }

  /**
   * Returns a list with the records (updates) written by some write
   * operation in the <code>XAFile</code>. It either reads the records
   * from the hashtable in memory (if not in recover phase), or from
   * the <code>log</code> file which contains all the updates (if the
   * system tries to recover).
   *
   * @return a list with <code>DataRecord</code> objects that contain
   *         the updated data
   * @exception IOException if an I/O error occurs
   */
  private LinkedList<DataRecord> retrieveRecords() throws IOException {
    LinkedList<DataRecord> records = new LinkedList<DataRecord>();
    if (recovers) {
      LinkedList<LogEntry> logRecords = log.readAllRecords();
      for (LogEntry entry : logRecords) {
        try {
          DataRecord dr = new DataRecord(entry.getPosition(), entry.getRecordLength(),
              XAFile.getIntsFromBytes(entry.getData()));
          records.add(dr);
        } catch (Exception ioe) {
          ioe.printStackTrace();
        }
      }
    } else {
      Set<Map.Entry<Long, Integer>> set = updatedBytes.entrySet();
      Iterator<Map.Entry<Long, Integer>> it = set.iterator();

      while (it.hasNext()) {
        int[] bs = new int[set.size()];
        Map.Entry<Long, Integer> kvPair = it.next();
        long position = kvPair.getKey();
        int b = kvPair.getValue();
        int index = bs.length-1;
        bs[index] = b;
        while (it.hasNext()) {
          kvPair = it.next();
          if (position - kvPair.getKey() == 1) {
            index--;
            bs[index] = kvPair.getValue();
            position = kvPair.getKey();
          } else
            break;
        }
        int[] bytes = new int[bs.length - index];
        System.arraycopy(bs, 0, bytes, 0, bytes.length);
        records.add(new DataRecord(position-index, bytes.length, bytes));
      }
    }
    return records;
  }

  /**
   * Adds adequate information to the <code>log</code>. Each entry
   * consists of the start position of the record in the file, its
   * length and the data (updates)
   * @param dr the record by which information will be written to the log
   * @exception IOException if an I/O error occurs
   */
  protected void add2Log(DataRecord dr) throws IOException {
    LogEntry le = new LogEntry(dr.getStartPosition(), dr.getRecordLength(), dr.getRecordBytes());
    log.addInfo(le);
  }

  /**
   * Returns the global Transaction id associated with this
   * <code>XAResourceManager</code>
   * @return the global Transaction id associated with this
   *         XAResourceManager
   */
  protected Xid getXid() {
    return currentXid;
  }

  /**
   * Returns all the updated bytes that have been modified by the
   * transaction with <code>currentXid</code>.
   * @return all the updated bytes that have been stored in the
   *         table by previous <code>write</code> operations.
   */
  protected Hashtable<Long, Integer> getUpdatedBytes() {
    return updatedBytes;
  }

  /**
   * Adds the given <code>bytes</code> to the table that keeps all
   * the <code>updatedBytes</code>. The array is treated as a group
   * of modified bytes (record) with its first byte located at
   * <code>startPosition</code> in the Transactional File.
   *
   * @param startPosition the position in the file where the record starts
   * @param bytes an array that includes all the updated bytes to be stored
   */
  protected void addUpdatedBytes(long  startPosition, int[] bytes) {
    for (int b : bytes)
      updatedBytes.put(startPosition++, b);
  }

  /**
   * This method is invoked when trying to deserialize an <code>XAResourceManager</code>
   * object.
   * <p>
   * The method will read the non-static and non-transient fields of the current class and
   * will set the <code>recovers</code> value to <code>true</code>. This will allow the
   * rest of the methods in this class to be aware that the system is in its recovery
   * phase and aact properly.
   * <p>
   * The method also initializes the <code>RandomAccessFile</code> object used in the
   * <Code>XAFile</code> as well as the <code>XALockManager</code> objects to obtain the
   * list of held locks.
   *
   * @param in input stream to read from
   * @exception IOException if an I/O error occurs
   * @exception ClassNotFoundException if the class of the Serialized object cannot be
   *                                   found
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    recovers = true;
    xaFile.initRAF();
    xaFile.initLocksHeld();
  }
}