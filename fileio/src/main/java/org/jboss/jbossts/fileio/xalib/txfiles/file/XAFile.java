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

import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;
import javax.transaction.*;
import javax.transaction.xa.Xid;
import java.util.Hashtable;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.UTFDataFormatException;
import org.jboss.jbossts.fileio.DataOutputStream;
import org.jboss.jbossts.fileio.xalib.txfiles.exceptions.LockRefusedException;
import org.jboss.jbossts.fileio.xalib.txfiles.exceptions.DuplicateTransactionsException;
import org.jboss.jbossts.fileio.xalib.txfiles.logging.RecordsLogger;
import org.jboss.jbossts.fileio.xalib.txfiles.locking.XALockManager;
import org.jboss.jbossts.fileio.xalib.txfiles.locking.XALock;
import org.jboss.jbossts.fileio.xalib.Globals;

/**
 * Instances of this class support both reading/writing to a
 * random access file and using the random access file with
 * the support of Transactions. If <code>newTransaction</code> is
 * invoked after a <code>TransactionManager</code> has been created
 * and begun, the XAFile will automatically act as a Transactional File,
 * otherwise it will act as a normal random access file with operations
 * acting directly on the source file. If the XAFile is in Transactional
 * mode then any changes (writes) will not affect directly the file, but
 * will only take effect right after the <code>TransactionManager</code> invokes
 * its own </em>commit</em> method which will cause the <code>commitUpdates</code>
 * apply (write) the updated bytes to the source file.
 * <p>
 * Calling the <code>newTransaction</code> method twice from the same
 * thread will cause a <code>DuplicateTransactionsException</code> to
 * be thrown. As this XAFile class implements the <code>DataInput, DataOutput</code>
 * interfaces all of their implemented read or write methods will throw an
 * <code>IOException</code> like specified in the above interfaces and
 * the read/write methods of the random access file used.
 *
 * @author Ioannis Ganotis
 * @version Jun 10, 2008
 *
 * @see org.jboss.jbossts.fileio.xalib.txfiles.locking.XALockManager
 * @see XAResourceManager
 * @see RecordsLogger
 * @see DataRecord
 */
public class XAFile implements DataInput, DataOutput, Serializable, Closeable
{
  private String filename;
  private String mode;
  transient private RandomAccessFile raf;
  private Hashtable<Long, XAResourceManager> xares;
  transient private XALockManager xaLockManager;
  transient private File loggingFolder;
  transient private File locksFolder;
  transient private boolean transactionsEnabled;

  /**
   * Constructor to create objects that represent a Transactional
   * File on which read and write operations can be applied with the
   * presence of ACID semantics.
   * 
   * @param filename the name of the source file
   * @param mode the access mode (as specified in the {@link java.io.RandomAccessFile})
   * @param transactionsEnabled if true the XAFile will behave Transactionally and will
   *                            allow commit/rollback operations, otherwise will behave
   *                            as a normal random access file with read/write operations
   *
   * @exception FileNotFoundException
   *            if the mode is <tt>"r"</tt> but the given string does not
   *            denote an existing regular file, or if the mode begins with
   *            <tt>"rw"</tt> but the given string does not denote an
   *            existing, writable regular file and a new regular file of
   *            that name cannot be created, or if some other error occurs
   *            while opening or creating the file
   * @exception IOException if an I/O error occurs
   */
  public XAFile(String filename, String mode, boolean transactionsEnabled)
      throws IOException {
    raf = new RandomAccessFile(filename, mode);
    loggingFolder = new File(Globals.LOG_FOLDER_PATH);
    locksFolder = new File(Globals.LOCKS_FOLDER_PATH);
    this.filename = filename;
    this.mode = mode;
    this.transactionsEnabled = transactionsEnabled;
    xares = new Hashtable<Long, XAResourceManager>();
    initLocksHeld();

    prepareFolders();
  }

  /**
   * Method to create folders that are needed
   * by the library
   */
  private void prepareFolders()
  {
    if (!loggingFolder.exists())
      loggingFolder.mkdir();
    if (!locksFolder.exists())
      locksFolder.mkdir();
  }

  /**
   * Method to create a new Transaction and enlist XAResources.
   * <p>
   * The transaction is enlisted in a new {@link XAResourceManager}
   * and appropriate log files are created each time the method is invoked.
   * It is important that this method is called after the {@link javax.transaction.TransactionManager}
   * has began, in order to benefit from the Transactional effects.
   * <p>
   * This method can be used in a multi-threaded environemnt but each
   * thread must call it once, otherwise a <code>DuplicateTransactionsException</code>
   * will be thrown to prevent this.
   *
   * @param txnMngr the <code>TransactionManager</code> used to
   *                commit or rollback any attempted modifications to the
   *                source file
   *
   * @exception org.jboss.jbossts.fileio.xalib.txfiles.exceptions.DuplicateTransactionsException
   *            When trying to call this method within the same thread
   * @exception IOException
   *            May be thrown if there is a problem with either creating
   *            the <code>XAResourceManager</code> or the <code>RecordsLogger</code>
   * @exception javax.transaction.SystemException
   *            Will be thrown if there is a problem enlisting the XAResource created
   *            within this method or when the <code>getTransaction</code> in the
   *            <code>TransactionManager</code> fails or when the TransactionManager
   *            is not in ready (<code>Status.ACTIVE</code>) mode
   * @exception javax.transaction.RollbackException
   *            Will be thrown if there is a problem while enlisting the resource to
   *            the transaction
   * @exception IllegalStateException
   *            if the <code>transactionsEnabled</code> in the constructor of the
   *            XAFile has been chosen to be false
   */
  public synchronized void newTransaction(TransactionManager txnMngr)
      throws DuplicateTransactionsException, IOException, SystemException, RollbackException {
    if (transactionsEnabled) {
      if (txnMngr.getStatus() == Status.STATUS_ACTIVE)
      {
        long th_id = Thread.currentThread().getId();
        if (!xares.containsKey(th_id)) {
          String logName = loggingFolder.getPath() + '/' + th_id + '_' + System.nanoTime();

          RecordsLogger log = new RecordsLogger(logName);

          XAResourceManager xareMngr = new XAResourceManager(this, log, th_id);
          xares.put(th_id, xareMngr);
          Transaction txn = txnMngr.getTransaction();
          txn.enlistResource(xareMngr);
        } else
        {
          throw new DuplicateTransactionsException("Cannot create a new Transaction. " +
              "There is already a thread with id=<" + th_id + "> associated with that " +
              "Transaction.");
        }
      } else
      {
        throw new SystemException("The newTransaction() method must be called only " +
            "when TransactionManager's status is ACTIVE, after the manager has begun.");
      }
    } else
    {
      throw new IllegalStateException("Transactional support is set to be disabled. " +
          "Enable using the setTransactionsEnabled() method.");
    }
  }

  /**
   * Returns the <code>RandomAccessFile</code> object used to read/write
   * @return the <code>RandomAccessFile</code> object associated with
   *         this class to apply the read/write operations
   */
  public RandomAccessFile getRAF() {
    return raf;
  }

  /**
   * Closes this XAFile.
   * <p>
   * The method actually closes the random access file stream and releases
   * any system resources associated with the stream and the Transactions.
   * If this file has an associated channel then the channel is closed
   * as well.
   *
   * @exception  IOException  if an I/O error occurs or if there are
   *                          incomplete Transactions.
   */
  public void close() throws IOException {
    if (!xares.isEmpty())
        throw new IOException("Failed to close the file. There are incomplete Transactions."); //todo better rollback??
    raf.close();
    if (xaLockManager.obtainHeldLocksWith(null).isEmpty()) // if the file is not  empty,
      xaLockManager.deleteFile();      // possibly another VM has written to it, so
  }                                    // do not delete it

  /**
   * Gets the <code>FileDescriptor</code> of the
   * random access file used and forces synchronization.
   *
   * This method is used at the <code>prepare</code> phase of an
   * <code>{@link XAResourceManager}</code>.
   *
   * @exception IOException if an I/O error occurs
   */
  protected void sync() throws IOException {
    FileDescriptor fd = raf.getFD();
    fd.sync();
  }

  /**
   * Forces data to be written to disk by instantly closing and re-opening
   * the random access file. The file pointer returns to the correct
   * position after re-opening the file.
   *
   * @exception IOException if an I/O error occurs
   */
  public synchronized void flush() throws IOException {
    long curPos = raf.getFilePointer();
    raf.close();
    raf = new RandomAccessFile(filename, mode);
    raf.seek(curPos);
  }

  /**
   * Sets the file-pointer offset, measured from the beginning of this
   * file, at which the next read or write occurs.  The offset may be
   * set beyond the end of the file. Setting the offset beyond the end
   * of the file does not change the file length.  The file length will
   * change only by writing after the offset has been set beyond the end
   * of the file.
   *
   * @param      position   the offset position, measured in bytes from the
   *                        beginning of the file, at which to set the file
   *                        pointer.
   * @exception  IOException  if <code>position</code> is less than
   *                          <code>0</code> or if an I/O error occurs.
   */
  public void seek(long position) throws IOException {
    raf.seek(position);
  }

  /**
   * Method to enable Transactional support in the file
   * <p>
   * If the transactions are enabled and a read/write operation is invoked
   * outside the scope of begin-commit/rollback an exception will be thrown
   * as specified in the <code>newTransaction</code> method. To use these
   * operations in that way set transaction support to false. 
   * @param transactionsEnabled if true, the XAFile will create transactions
   *                            and will allow commit/rollback operations in
   *                            the <code>TransactionManager.</code>
   */
  public void setTransactionsEnabled(boolean transactionsEnabled) {
    this.transactionsEnabled = transactionsEnabled;
  }

  /**
   * Returns a standard error message based on the given <code>th_id</code>
   * @param th_id the thread id participating in the generated message
   * @return a standard error message based on the given <code>th_id</code>
   */
  private String getErrMsg(long th_id)
  {
    return "Failed to update source file. " +
        "The thread with id=<" + th_id + "> is not associated with any Transaction";
  }

  /**
   * Method to update a series of bytes in the file starting from a
   * given <code>position</code>.
   * <p>
   * The method is called after an {@link XAResourceManager} is
   * ready to commit. It copies <code>data</code> of exact <code>recordLength</code>
   * and starting from position <code>position</code> in the source file.
   *
   * @param position the position in the file to start copying <code>data</code>
   * @param recordLength the length of bytes to be copied
   * @param data the actual data/updates
   * @param th_id the thread associated with the Transaction trying to commit
   * @exception IOException if an I/O error occurs
   * @exception IllegalStateException
   *            if the given <code>th_id</code> is not associated with a
   *            Transaction
   */  
  protected synchronized void commitUpdates(long position, int recordLength, byte[] data,
                                            long th_id) throws IOException {
    if (th_id != Globals.RECOVERY_ID && th_id != getCurrentThreadId())
      throw new IllegalStateException(getErrMsg(th_id));
    long curPos = raf.getFilePointer();
    commitUpdates(position, recordLength, data);
    raf.seek(curPos);
  }

  /**
   * Method to update a series of bytes in the file starting from a
   * given <code>position</code>.
   * <p>
   * The method is called after an {@link XAResourceManager} is
   * ready to commit. It copies <code>data</code> of exact <code>recordLength</code>
   * and starting from position <code>position</code> in the source file.
   *
   * @param position the position in the file to start copying <code>data</code>
   * @param recordLength the length of bytes to be copied
   * @param data the actual data/updates
   * @exception IOException if an I/O error occurs
   */
  private void commitUpdates(long position, int recordLength, byte[] data) throws IOException
  {
    raf.seek(position);
    raf.write(data, 0, recordLength);
  }

  /**
   * Disassociates a current thread from an existing Transaction.
   * <p>
   * After a <code>TransactionManager</code>'s commit/rollback operations
   * this method is invoked to remove the thread registered by the
   * <code>newTransaction</code> method.
   * 
   * @param xid the thread as a key to remove the corresponding
   *              <code>XAResourceManager</code> from the list.
   * @param recovers true if the system has crashed and tries to recover;
   *                 otherwise false
   * @exception IOException
   *         if an I/O error occurs or there is no registered thread
   *         with the given <code>th_id</code>
   */
  protected synchronized void removeTransaction(Xid xid, boolean recovers)
      throws IOException {
    if (!recovers) {
      long th_id = getCurrentThreadId();
      xares.remove(th_id);
    }
    xaLockManager.releaseLocks(xid);
  }

  /**
   * Fill the list of locks held while trying to read/write to
   * the file.
   *
   * @exception IOException if an I/O error occurs
   */
  protected void initLocksHeld() throws IOException {
    xaLockManager = new XALockManager(filename);
  }

  /**
   * Internal method to ask from {@link org.jboss.jbossts.fileio.xalib.txfiles.locking.XALockManager} to apply
   * a read/write lock on a given <code>DataRecord</code>.
   * <p>
   * The method is invoked by read/write operations within the
   * XAFile to lock on a specific range of bytes in the file.
   * @param dr the record to lock on, either in read or write mode
   * @param xid the {@link javax.transaction.xa.Xid} of the current
   *        {@link XAResourceManager}
   * @param mode read/write modes as specified in the
   *             {@link com.arjuna.ats.txoj.LockMode} class
   * @return the {@link com.arjuna.ats.txoj.LockResult} depending on
   *         whether the lock can be <em>GRANTED or REFUSED</em>
   * @exception IOException if an I/O error occurs
   * @exception org.jboss.jbossts.fileio.xalib.txfiles.exceptions.LockRefusedException if lock cannot be <em>GRANTED</em>
   */
  private int acquireLockOn(DataRecord dr, Xid xid, int mode) throws IOException {
    XALock xaLock = new XALock(xid, mode, dr.getStartPosition(), dr.getRecordLength());
    int res = xaLockManager.tryLock(xaLock);//LockManager.waitTotalTimeout);

    if (res == LockResult.REFUSED) {
      String msg = "REFUSED:WRITE_LOCK on byte(s): " + dr.getRecordStr();
      if (mode == LockMode.READ) {
        msg = "REFUSED:READ_LOCK on byte(s): " + dr.getRecordStr();
      }
      throw new LockRefusedException(msg);
    } else if (res == LockResult.GRANTED) {
      String msg = "GRANTED:WRITE_LOCK on byte(s): " + dr.getRecordStr();
      if (mode == LockMode.READ) {
        msg = "GRANTED:READ_LOCK on byte(s): " + dr.getRecordStr();
      }
//      System.out.println(msg);
    }
    return res;
  }

  /**
   * Returns the name of the file
   * @return the name of the file
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Returns the access mode in which the <code>XAFile</code> was
   * created
   * @return the access mode which was set when created the
   *         <code>XAFile</code> object
   */
  public String getMode() {
    return mode;
  }

  /**
   * Internal method used by read operations in this <code>XAFile</code>.
   * If the class is used in the Transactional mode (Transactions are enabled)
   * the method tries to read bytes from memory. If that fails it means no
   * previous write operations applied on the same range of bytes so the
   * method will try to read and return the bytes as read directly from the
   * source file. If Transactions are disabled the method does not make any
   * attempts to find if there are any modified bytes by previous write
   * operations and so it reads directly from the file.
   * <p>
   * The method starts reading bytes at the current file pointer. If data
   * are retrieved from the memory instead of the actual file, the file
   * pointer progresses by <code>len</code> steps, if successful.
   * @param len the length of data to read
   * @return the bytes read into an array of Integers
   * @exception IOException          if an I/O error occurs 
   * @exception org.jboss.jbossts.fileio.xalib.txfiles.exceptions.LockRefusedException if a lock cannot be <code>GRANTED</code>
   */
  private synchronized int[] readRecord(int len) throws IOException {
    if (transactionsEnabled) {
      long curThread = getCurrentThreadId();
      XAResourceManager xare = xares.get(curThread);

      Hashtable<Long, Integer> updatedBytes = xare.getUpdatedBytes();
      int[] upds = new int[len];
      long startPos = raf.getFilePointer();
      int i;
      for (i=0;i<len;i++) {
//        if (startPos >= raf.length())
//          return null;
        if (updatedBytes.containsKey(startPos)) {
          upds[i] = updatedBytes.get(startPos++);
          raf.seek(raf.getFilePointer()+1);
        } else {
          upds[i] = raf.read();
        }
      }
      DataRecord dr = new DataRecord(raf.getFilePointer()-len, len, upds);
      acquireLockOn(dr, xare.getXid(), LockMode.READ);
      return upds;
    }
    return readDirectlyFromFile(len);
  }

  /**
   * Method to read exactly <code>len</code> bytes
   * directly from the file and starting at the current file
   * pointer.
   *
   * @param len the length of bytes to read from the file
   * @return the bytes read into an Integer array
   * @exception IOException if an I/O error occurs
   */
  public int[] readDirectlyFromFile(int len) throws IOException {
    int[] buffer = new int[len];
    for (int b=0; b<len; b++) {
      buffer[b] = raf.read();
    }
    return buffer;
  }

  /**
   * Reads a <code>boolean</code> from this file. This method reads a
   * single byte from the <code>readByte</code> method, starting at
   * the current file pointer. A value of <code>0</code> represents
   * <code>false</code>. Any other value represents <code>true</code>.
   * This method blocks until the byte is read, the end of the stream
   * is detected, or an exception is thrown.
   *
   * @return     the <code>boolean</code> value read.
   * @exception  EOFException  if this file has reached the end.
   * @exception  IOException   if an I/O error occurs.
   */  
  public boolean readBoolean() throws IOException {
    int ch = readByte();
    if (ch < 0) {
      throw new EOFException();
    }
    return (ch != 0);
  }

  /**
   * Reads a <code>double</code> from this file. This method reads a
   * <code>long</code> value, starting at the current file pointer,
   * as if by the <code>readLong</code> method
   * and then converts that <code>long</code> to a <code>double</code>
   * using the <code>longBitsToDouble</code> method in
   * class <code>Double</code>.
   * <p>
   * This method blocks until the eight bytes are read, the end of the
   * stream is detected, or an exception is thrown.
   *
   * @return     the next eight bytes of this file, interpreted as a
   *             <code>double</code>.
   * @exception  EOFException  if this file reaches the end before reading
   *             eight bytes.
   * @exception  IOException   if an I/O error occurs.
   * @see        java.io.RandomAccessFile#readLong()
   * @see        java.lang.Double#longBitsToDouble(long)
   */
  public double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  /**
   * Reads an unsigned 16-bit number from this file. This method gets
   * the two bytes return from the <code>readRecord</code> method,
   * starting at the current file pointer. If the bytes read, in order, are
   * <code>b1</code> and <code>b2</code>, where
   * <code>0&nbsp;&lt;=&nbsp;b1, b2&nbsp;&lt;=&nbsp;255</code>,
   * then the result is equal to:
   * <blockquote><pre>
   *     (b1 &lt;&lt; 8) | b2
   * </pre></blockquote>
   * <p>
   * This method blocks until the two bytes are read, the end of the
   * stream is detected, or an exception is thrown.
   *
   * @return     the next two bytes of this file (or memory if there are
   *             uncommitted changes), interpreted as an unsigned
   *             16-bit integer.
   * @exception  EOFException  if this file reaches the end before reading
   *                           two bytes.
   * @exception  IOException   if an I/O error occurs.
   */
  public int readUnsignedShort() throws IOException {
    int[] ints = readRecord(Short.SIZE/Byte.SIZE);

    int ch1 = ints[0];
    int ch2 = ints[1];
    if ((ch1 | ch2) < 0)
      throw new EOFException();

    return (ch1 << 8) + (ch2);
  }

  /**
   * Reads <code>bytes.length</code> bytes from this file(or memory if there
   * are uncommitted updated bytes) into the byte
   * array, starting at the current file pointer. This method reads
   * repeatedly until the requested number of bytes are read. This
   * method blocks until the requested number of bytes are read,
   * the end of the stream is detected, or an exception is thrown.
   *
   * @param      bytes   the buffer into which the data is read.
   * @exception  EOFException  if this file reaches the end before reading
   *               all the bytes.
   * @exception  IOException   if an I/O error occurs.
   */
  public void readFully(byte[] bytes) throws IOException {
    readFully(bytes, 0, bytes.length);
  }

  /**
   * Reads exactly <code>len</code> bytes from this file(or memory if there
   * are uncommitted updated bytes) into the byte
   * array, starting at the current file pointer. This method reads
   * repeatedly until the requested number of bytes are read. This
   * method blocks until the requested number of bytes are read,
   * the end of the stream is detected, or an exception is thrown.
   *
   * @param      bytes   the buffer into which the data is read.
   * @param      off   the start offset of the data.
   * @param      len   the number of bytes to read.
   * @exception  EOFException  if this file reaches the end before reading
   *                           all the bytes.
   * @exception  IOException   if an I/O error occurs.
   * @exception IndexOutOfBoundsException
   *            if offset is negative, or len is negative, or offset+len is
   *            greater than the size of the <code>bytes</code> array
   */
  public void readFully(byte[] bytes, int off, int len) throws IOException {
    if (off < 0 || len < 0 || off + len > bytes.length)
      throw new IndexOutOfBoundsException("bytes.length=" + bytes.length +
          ", off=" + off + ", len=" + len);

    byte[] bs = getBytesFromInts(readRecord(len));
    System.arraycopy(bs, 0, bytes, off, len);
//    for (int b = off; b < off + len; b++) {
//      bytes[b] = readByte();
//    }
  }

  /**
   * Reads a signed 16-bit number from this file. The method reads two
   * bytes from this file (or memory if there are uncommitted byte updates),
   * starting at the current file pointer. If the two bytes read, in order,
   * are <code>b1</code> and <code>b2</code>, where each of the two values is
   * between <code>0</code> and <code>255</code>, inclusive, then the
   * result is equal to:
   * <blockquote><pre>
   *     (short)((b1 &lt;&lt; 8) | b2)
   * </pre></blockquote>
   * <p>
   * This method blocks until the two bytes are read, the end of the
   * stream is detected, or an exception is thrown.
   *
   * @return     the next two bytes of this file(memory), interpreted as a signed
   *             16-bit number.
   * @exception  EOFException  if this file reaches the end before reading
   *                           two bytes.
   * @exception  IOException   if an I/O error occurs.
   */
  public short readShort() throws IOException {
    int[] ints = readRecord(Short.SIZE/Byte.SIZE);

    int ch1 = ints[0];
    int ch2 = ints[1];
    if ((ch1 | ch2) < 0) {
      throw new EOFException();
    }
    return (short) ((ch1 << 8) + (ch2));
  }

  /**
   * Reads in a string from this file (or memory if there are uncommitted
   * byte updates). The string has been encoded using a
   * <a href="DataInput.html#modified-utf-8">modified UTF-8</a>
   * format.
   * <p>
   * The first two bytes are read, starting from the current file
   * pointer, as if by
   * <code>readUnsignedShort</code>. This value gives the number of
   * following bytes that are in the encoded string, not
   * the length of the resulting string. The following bytes are then
   * interpreted as bytes encoding characters in the modified UTF-8 format
   * and are converted into characters.
   * <p>
   * This method blocks until all the bytes are read, the end of the
   * stream is detected, or an exception is thrown.
   *
   * @return     a Unicode string.
   * @exception  EOFException
   *             if this file reaches the end before reading all the bytes.
   * @exception  IOException             if an I/O error occurs.
   * @exception  UTFDataFormatException
   *             if the bytes do not represent
   *             valid modified UTF-8 encoding of a Unicode string.
   * @see        java.io.RandomAccessFile#readUnsignedShort()
   */
  public String readUTF() throws IOException {
    return DataInputStream.readUTF(this);
  }

  /**
   * Reads a <code>float</code> from this file (or memory if there are
   * uncommitted byte updates). This method reads an 
   * <code>int</code> value, starting at the current file pointer,
   * as if by the <code>readInt</code> method
   * and then converts that <code>int</code> to a <code>float</code>
   * using the <code>intBitsToFloat</code> method in class
   * <code>Float</code>.
   * <p>
   * This method blocks until the four bytes are read, the end of the
   * stream is detected, or an exception is thrown.
   *
   * @return     the next four bytes of this file, interpreted as a
   *             <code>float</code>.
   * @exception  EOFException  if this file reaches the end before reading
   *             four bytes.
   * @exception  IOException   if an I/O error occurs.
   * @see        java.io.RandomAccessFile#readInt()
   * @see        java.lang.Float#intBitsToFloat(int)
   */
  public float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  /**
   * Reads a signed 32-bit integer from this file (or memory if there
   * are uncommitted byte updates). This method reads 4
   * bytes from the file, starting at the current file pointer.
   * If the bytes read, in order, are <code>b1</code>,
   * <code>b2</code>, <code>b3</code>, and <code>b4</code>, where
   * <code>0&nbsp;&lt;=&nbsp;b1, b2, b3, b4&nbsp;&lt;=&nbsp;255</code>,
   * then the result is equal to:
   * <blockquote><pre>
   *     (b1 &lt;&lt; 24) | (b2 &lt;&lt; 16) + (b3 &lt;&lt; 8) + b4
   * </pre></blockquote>
   * <p>
   * This method blocks until the four bytes are read, the end of the
   * stream is detected, or an exception is thrown.
   *
   * @return     the next four bytes of this file, interpreted as an
   *             <code>int</code>.
   * @exception  EOFException  if this file reaches the end before reading
   *               four bytes.
   * @exception  IOException   if an I/O error occurs.
   */
  public int readInt() throws IOException {
    int[] ints = readRecord(Integer.SIZE/Byte.SIZE);

    int ch1 = ints[0];
    int ch2 = ints[1];
    int ch3 = ints[2];
    int ch4 = ints[3];
    if ((ch1 | ch2 | ch3 | ch4) < 0) {
      throw new EOFException();
    }
    return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
  }

  /**
   * Reads an unsigned eight-bit number from this file (or memory if there
   * are uncommitted byte updates). This method reads
   * a byte, starting at the current file pointer, and returns that byte.
   * <p>
   * This method blocks until the byte is read, the end of the stream
   * is detected, or an exception is thrown.
   *
   * @return     the next byte of this file, interpreted as an unsigned
   *             eight-bit number.
   * @exception  EOFException  if this file has reached the end.
   * @exception  IOException   if an I/O error occurs.
   */
  public int readUnsignedByte() throws IOException {
    int ch = readByte();
    if (ch < 0) {
      throw new EOFException();
    }
    return ch;
  }

  /**
   * Reads a signed eight-bit value from this file (or memory if there
   * are uncommitted byte updates). This method reads a
   * byte, starting from the current file pointer. If the byte read is
   * <code>b</code>, where <code>0&nbsp;&lt;=&nbsp;b&nbsp;&lt;=&nbsp;255</code>,
   * then the result is:
   * <blockquote><pre>
   *     (byte)(b)
   * </pre></blockquote>
   * <p>
   * This method blocks until the byte is read, the end of the stream
   * is detected, or an exception is thrown.
   *
   * @return     the next byte of this file as a signed eight-bit
   *             <code>byte</code>.
   * @exception  EOFException  if this file has reached the end.
   * @exception  IOException   if an I/O error occurs.
   */
  public byte readByte() throws IOException {
    int[] ints = readRecord(Byte.SIZE/Byte.SIZE);
    return (ints != null) ? (byte) ints[0] : -1;
  }

  /**
   * Reads the next line of text from this file.  This method successively
   * reads bytes from the file (or memory if there are uncommitted byte updates),
   * starting at the current file pointer, until it reaches a line terminator
   * or the end of the file. Each byte is converted into a character by taking
   * the byte's value for the lower eight bits of the character and setting the
   * high eight bits of the character to zero.  This method does not,
   * therefore, support the full Unicode character set.
   *
   * <p> A line of text is terminated by a carriage-return character
   * (<code>'&#92;r'</code>), a newline character (<code>'&#92;n'</code>), a
   * carriage-return character immediately followed by a newline character,
   * or the end of the file.  Line-terminating characters are discarded and
   * are not included as part of the string returned.
   *
   * <p> This method blocks until a newline character is read, a carriage
   * return and the byte following it are read (to see if it is a newline),
   * the end of the file is reached, or an exception is thrown.
   *
   * @return     the next line of text from this file, or null if end
   *             of file is encountered before even one byte is read.
   * @exception  IOException  if an I/O error occurs.
   */
  public String readLine() throws IOException {
    StringBuffer input = new StringBuffer();
    int c = -1;
    boolean eol = false;

    while (!eol) {
      switch (c = readByte()) {
        case -1:
        case '\n':
          eol = true;
          break;
        case '\r':
          eol = true;
          long cur = raf.getFilePointer();
          if ((readByte()) != '\n') {
            raf.seek(cur);
          }
          break;
        default:
          input.append((char) c);
          break;
      }
    }
    if ((c == -1) && (input.length() == 0)) {
      return null;
    }
    return input.toString();
  }

  /**
   * Reads up to <code>bytes.length</code> bytes of data from this file
   * (or memory if there are uncommitted byte updates) into an array of
   * bytes. This method blocks until at least one byte of input is available.
   * <p>
   * Although <code>XAFile</code> is not a subclass of
   * <code>InputStream</code>, this method behaves in exactly the
   * same way as the {@link InputStream#read(byte[])} method of
   * <code>InputStream</code>.
   *
   * @param      bytes   the buffer into which the data is read.
   * @return     the total number of bytes read into the buffer, or
   *             <code>-1</code> if there is no more data because the end of
   *             this file has been reached.
   * @exception  IOException
   *             If the first byte cannot be read for any reason
   *             other than end of file, or if the random access file has been
   *             closed, or if some other I/O error occurs.
   * @exception  NullPointerException If <code>bytes</code> is <code>null</code>.
   */
  public int read(byte[] bytes) throws IOException {
    return read(bytes, 0, bytes.length);
  }

  /**
   * Reads exactly <code>len</code> bytes of data from this file
   * (or memory if there are uncommitted byte updates) into an array of
   * bytes. This method blocks until at least one byte of input is available.
   * <p>
   * Although <code>XAFile</code> is not a subclass of
   * <code>InputStream</code>, this method behaves in exactly the
   * same way as the {@link InputStream#read(byte[])} method of
   * <code>InputStream</code>.
   *
   * @param      bytes   the buffer into which the data is read.
   * @param      off   the start offset in array <code>bytes</code>
   *                   at which the data is written.
   * @param      len   the maximum number of bytes read.
   * @return     the total number of bytes read into the buffer, or
   *             <code>-1</code> if there is no more data because the end of
   *             this file has been reached.
   * @exception  IOException
   *             If the first byte cannot be read for any reason
   *             other than end of file, or if the random access file has been
   *             closed, or if some other I/O error occurs.
   * @exception  NullPointerException If <code>bytes</code> is <code>null</code>.
   * @exception IndexOutOfBoundsException
   *            if offset is negative, or len is negative, or offset+len is
   *            greater than the size of the <code>bytes</code> array
   */
  public int read(byte[] bytes, int off, int len) throws IOException {
    if (off < 0 || len < 0 || off + len > bytes.length)
      throw new IndexOutOfBoundsException("bytes.length=" + bytes.length +
          ", off=" + off + ", len=" + len);
    if (bytes.length == 0)
      return 0;
    byte[] bs = getBytesFromInts(readRecord(len)); //todo readRecord may return null
    System.arraycopy(bs, 0, bytes, off, len);
//    int b;
//    for (b = off; b < off + len; b++) {
//      if (b >= raf.length())
//        return -1;
//      bytes[b] = readByte();
//    }
    return bs.length;
  }

  /**
   * Reads up to <code>chars.length</code> characters from this file
   * (or memory if there are uncommitted byte updates) into an array
   * of characters.
   * <p>
   * This method blocks until the char is read, the end of the
   * stream is detected, or an exception is thrown.
   *
   * @param chars the array into which the characters are read
   * @return     a <code>String</code> containing the read characters
   * @exception  EOFException  if this file reaches the end before reading
   *                           <code>chars.length</code> characters.
   * @exception  IOException   if an I/O error occurs.
   */
  public String readChars(char[] chars) throws IOException {
    for (int c = 0; c < chars.length; c++) {
      chars[c] = readChar();
    }
    return new String(chars);
  }

  /**
   * Reads a character from this file. This method reads two
   * bytes from the file (or memory if there are uncommitted byte updates),
   * starting at the current file pointer.
   * If the bytes read, in order, are
   * <code>b1</code> and <code>b2</code>, where
   * <code>0&nbsp;&lt;=&nbsp;b1,&nbsp;b2&nbsp;&lt;=&nbsp;255</code>,
   * then the result is equal to:
   * <blockquote><pre>
   *     (char)((b1 &lt;&lt; 8) | b2)
   * </pre></blockquote>
   * <p>
   * This method blocks until the two bytes are read, the end of the
   * stream is detected, or an exception is thrown.
   *
   * @return     the next two bytes of this file, interpreted as a
   *		         <code>char</code>.
   * @exception  EOFException  if this file reaches the end before reading
   *                           two bytes.
   * @exception  IOException   if an I/O error occurs.
   */
  public char readChar() throws IOException {
    int[] ints = readRecord(Character.SIZE/Byte.SIZE);

    int ch1 = ints[0];
    int ch2 = ints[1];
    if ((ch1 | ch2) < 0) {
      throw new EOFException();
    }
    return (char) ((ch1 << 8) + (ch2));
  }

  /**
   * Reads a signed 64-bit integer from this file. This method reads eight
   * bytes from the file (or memory if there are uncommitted byte updates),
   * starting at the current file pointer.
   * If the bytes read, in order, are
   * <code>b1</code>, <code>b2</code>, <code>b3</code>,
   * <code>b4</code>, <code>b5</code>, <code>b6</code>,
   * <code>b7</code>, and <code>b8,</code> where:
   * <blockquote><pre>
   *     0 &lt;= b1, b2, b3, b4, b5, b6, b7, b8 &lt;=255,
   * </pre></blockquote>
   * <p>
   * then the result is equal to:
   * <p><blockquote><pre>
   *     ((long)b1 &lt;&lt; 56) + ((long)b2 &lt;&lt; 48)
   *     + ((long)b3 &lt;&lt; 40) + ((long)b4 &lt;&lt; 32)
   *     + ((long)b5 &lt;&lt; 24) + ((long)b6 &lt;&lt; 16)
   *     + ((long)b7 &lt;&lt; 8) + b8
   * </pre></blockquote>
   * <p>
   * This method blocks until the eight bytes are read, the end of the
   * stream is detected, or an exception is thrown.
   *
   * @return     the next eight bytes of this file, interpreted as a
   *             <code>long</code>.
   * @exception  EOFException  if this file reaches the end before reading
   *                           eight bytes.
   * @exception  IOException   if an I/O error occurs.
   */
  public long readLong() throws IOException {
    return ((long) (readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
  }

  /**
   * Attempts to write a <code>long</code> to the file as eight bytes,
   * high byte first. The write starts at the current position of the
   * file pointer.
   * <p>
   * Updates will be written to the file after a commit operation call.
   *
   * @param      l   a <code>long</code> to be written.
   * @exception  IOException  if an I/O error occurs.
   */
  public void writeLong(long l) throws IOException {
    int[] ls = new int[]{((int) (l >>> 56) & 0xFF), ((int) (l >>> 48) & 0xFF),
                         ((int) (l >>> 40) & 0xFF), ((int) (l >>> 32) & 0xFF),
                         ((int) (l >>> 24) & 0xFF), ((int) (l >>> 16) & 0xFF),
                         ((int) (l >>> 8) & 0xFF), ((int) (l) & 0xFF)};
    writeRecord(ls);
  }

  /**
   * Attempts to write the specified byte to this file. The write starts at
   * the current file pointer.
   * <p>
   * The byte will be written to the file only after the commit operation
   * @param      b   the <code>byte</code> to be written.
   * @exception  IOException  if an I/O error occurs.
   */
  public void write(int b) throws IOException {
    writeRecord(new int[]{b});
  }

  /**
   * Attempts to write an <code>int</code> to the file as four bytes, high
   * byte first. The write starts at the current position of the file pointer.
   * <p>
   * The <code>int</code> will be written to the file only after a commit operation call
   * @param      n   an <code>int</code> to be written.
   * @exception  IOException  if an I/O error occurs.
   */
  public void writeInt(int n) throws IOException {
    int[] ls = new int[]{((n >>> 24) & 0xFF), ((n >>> 16) & 0xFF),
        ((n >>> 8) & 0xFF), ((n) & 0xFF)};
    writeRecord(ls);
  }

  /**
   * Converts the double argument to a <code>long</code> using the
   * <code>doubleToLongBits</code> method in class <code>Double</code>,
   * and then attempts to write that <code>long</code> value to the file as an
   * eight-byte quantity, high byte first. The write starts at the current
   * position of the file pointer.
   * <p>
   * The <code>double</code> will be written to the file only after a
   * commit operation call
   *
   * @param      d   a <code>double</code> value to be written.
   * @exception  IOException  if an I/O error occurs.
   * @see        java.lang.Double#doubleToLongBits(double)
   */
  public void writeDouble(double d) throws IOException {
    writeLong(Double.doubleToLongBits(d));
  }

  /**
   * Converts the float argument to an <code>int</code> using the
   * <code>floatToIntBits</code> method in class <code>Float</code>,
   * and then attempts to write that <code>int</code> value to the file as a
   * four-byte quantity, high byte first. The write starts at the
   * current position of the file pointer.
   * <p>
   * The <code>float</code> will be written to the file only after a
   * commit operation call
   *
   * @param      f   a <code>float</code> value to be written.
   * @exception  IOException  if an I/O error occurs.
   * @see        java.lang.Float#floatToIntBits(float)
   * @see        XAFile#readFloat()
   */
  public void writeFloat(float f) throws IOException {
    writeInt(Float.floatToIntBits(f));
  }

  /**
   * Attempts to write a <code>boolean</code> to the file as a one-byte
   * value. The value <code>true</code> is written out as the value
   * <code>(byte)1</code>; the value <code>false</code> is written out
   * as the value <code>(byte)0</code>. The write starts at
   * the current position of the file pointer.
   * <p>
   * The <code>boolean</code> will be written to the file only after a
   * commit operation call
   *
   * @param      b   a <code>boolean</code> value to be written.
   * @exception  IOException  if an I/O error occurs.
   * @see XAFile#readBoolean()
   */
  public void writeBoolean(boolean b) throws IOException {
    write(b ? 1 : 0);
  }

  /**
   * Attempts to write a <code>byte</code> to the file as a one-byte value.
   * The write starts at the current position of the file pointer.
   * <p>
   * The <code>byte</code> will be written to the file only after a
   * commit operation call
   *
   * @param      b   a <code>byte</code> value to be written.
   * @exception  IOException  if an I/O error occurs.
   */
  public synchronized void writeByte(int b) throws IOException {
    write(b);
  }

  /**
   * Internal method used by write oprations in the <code>XAFile</code>.
   * If Transactions are disabled, bytes are written directly to the file.
   * If Transactions are enabled the given <code>bytes</code> are written
   * to the memory (added in a hashtable).
   * <p>
   * The method may be called by different threads. To prevent different
   * Transactions to modify an already modified record by another
   * Transaction a lock in <code>WRITE</code> mode is acquired. If the lock
   * is finally <code>GRANTED</code> the <code>bytes<code> are added
   * to the list of modified records in the correct <code>XAResourceManager</code>.
   *
   * @param bytes the array of bytes to write to the file
   * @exception IOException if an I/O error occurs
   * @exception org.jboss.jbossts.fileio.xalib.txfiles.exceptions.LockRefusedException if lock is <code>REFUSED</code>
   */
  private synchronized void writeRecord(int[] bytes) throws IOException {
    if (transactionsEnabled) { // write bytes to the memory first
      long curthr = getCurrentThreadId();
      int lockRes;

      XAResourceManager xareMngr = xares.get(curthr);

      DataRecord dr = new DataRecord(raf.getFilePointer(), bytes.length, bytes);
      lockRes = acquireLockOn(dr, xareMngr.getXid(), LockMode.WRITE);

      if (lockRes == LockResult.GRANTED) {
        xareMngr.addUpdatedBytes(raf.getFilePointer(), bytes);
        xareMngr.add2Log(dr);
        raf.skipBytes(bytes.length);
      }
    } else {  // write bytes directly to the file
              // XAFile will now behave like a RandomAccessFile
      commitUpdates(raf.getFilePointer(), bytes.length, getBytesFromInts(bytes));
    }
  }

  /**
   * Attempts to write a <code>short</code> to the file as two bytes, high
   * byte first. The write starts at the current position of the file pointer.
   * <p>
   * The <code>short</code> will be written to the file only after a
   * commit operation call
   *
   * @param      s   a <code>short</code> to be written.
   * @exception  IOException  if an I/O error occurs.
   * @see XAFile#readShort()
   */
  public void writeShort(int s) throws IOException {
    int[] ss = new int[]{(s >>> 8 & 0xFF), ((s) & 0xFF)};
    writeRecord(ss);
  }

  /**
   * Attempts to write <code>len</code> bytes from the specified
   * byte array starting at offset <code>off</code> to this file. 
   * <p>
   * The bytes will be written to the file only after a
   * commit operation call
   *
   * @param      bytes the data.
   * @param      off   the start offset in the data.
   * @param      len   the number of bytes to write.
   * @exception  IOException  if an I/O error occurs.
   * @exception IndexOutOfBoundsException
   *            if offset is negative, or len is negative, or offset+len is
   *            greater than the size of the <code>bytes</code> array
   * @see XAFile#read(byte[], int, int)
   */
  public void write(byte[] bytes, int off, int len) throws IOException {
    if (off < 0 || len < 0 || off + len > bytes.length)
      throw new IndexOutOfBoundsException("bytes.length=" + bytes.length +
          ", off=" + off + ", len=" + len);
    
    byte[] newByteArray = new byte[len];
    System.arraycopy(bytes, off, newByteArray, 0, len);
    writeRecord(getIntsFromBytes(newByteArray));
  }

  /**
   * Attempts to write a string to the file as a sequence of characters.
   * Each character is written to the data output stream as if by the
   * <code>writeChar</code> method. The write starts at the current
   * position of the file pointer.
   * <p>
   * The <code>String</code> will be written to the file only after a
   * commit operation call
   *
   * @param      s   a <code>String</code> value to be written.
   * @exception  IOException  if an I/O error occurs.
   * @see        java.io.RandomAccessFile#writeChar(int)
   * @see        XAFile#readChar()
   */
  public void writeChars(String s) throws IOException {
    int clen = s.length();
    int blen = 2 * clen;
    byte[] b = new byte[blen];
    char[] c = new char[clen];
    s.getChars(0, clen, c, 0);
    for (int i = 0, j = 0; i < clen; i++) {
      b[j++] = (byte) (c[i] >>> 8);
      b[j++] = (byte) (c[i]);
    }
    write(b, 0, blen);
  }

  /**
   * Attempts to write a <code>char</code> to the file as a two-byte
   * value, high byte first. The write starts at the current position
   * of the file pointer.
   * <p>
   * The <code>char</code> will be written to the file only after a
   * commit operation call
   *
   * @param      ch   a <code>char</code> value to be written.
   * @exception  IOException  if an I/O error occurs.
   * @see XAFile#writeChars(String)
   * @see XAFile#readChar()
   */
  public void writeChar(int ch) throws IOException {
    int[] chs = new int[]{((ch >>> 8) & 0xFF), ((ch) & 0xFF)};
    writeRecord(chs);
  }

  /**
   * Attempts to write the string to the file as a sequence of bytes. Each
   * character in the string is written out, in sequence, by discarding
   * its high eight bits. The write starts at the current position of
   * the file pointer.
   * <p>
   * The <code>String</code> will be written to the file only after a
   * commit operation call
   *
   * @param      bytes   a string of bytes to be written.
   * @exception  IOException  if an I/O error occurs.
   */
  public void writeBytes(String bytes) throws IOException {
    int len = bytes.length();
    byte[] b = bytes.getBytes();
    write(b, 0, len);
  }

  /**
   * Attempts to write <code>bytes.length</code> bytes from the specified
   * byte array to this file, starting at current file pointer.
   * <p>
   * The bytes will be written to the file only after a
   * commit operation call
   *
   * @param      bytes the data.
   * @exception  IOException  if an I/O error occurs.
   * @see XAFile#read(byte[])
   */
  public void write(byte[] bytes) throws IOException {
    write(bytes, 0, bytes.length);
  }

  /**
   * Attempts to write a string to the file using
   * <a href="DataInput.html#modified-utf-8">modified UTF-8</a>
   * encoding in a machine-independent manner.
   * <p>
   * First, two bytes are written to the memory, starting at the
   * current file pointer, as if by the
   * <code>writeShort</code> method giving the number of bytes to
   * follow. This value is the number of bytes actually written out,
   * not the length of the string. Following the length, each character
   * of the string is output, in sequence, using the modified UTF-8 encoding
   * for each character.
   * <p>
   * The <code>String</code> will be written to the file only after a
   * commit operation call
   *
   * @param      str   a string to be written.
   * @exception  IOException  if an I/O error occurs
   * @see XAFile#readUTF()
   */
  public void writeUTF(String str) throws IOException {
    DataOutputStream.writeUTF(str, this);
  }

  /**
   * Returns the current offset in this file.
   *
   * @return     the offset from the beginning of the file, in bytes,
   *             at which the next attempt of read or write occurs.
   * @exception  IOException  if an I/O error occurs.
   */
  public long getFilePointer() throws IOException {
    return raf.getFilePointer();
  }

  /**
   * Attempts to skip over <code>n</code> bytes of input discarding the
   * skipped bytes.
   * <p>
   * This method may skip over some smaller number of bytes, possibly zero.
   * This may result from any of a number of conditions; reaching end of
   * file before <code>n</code> bytes have been skipped is only one
   * possibility. This method never throws an <code>EOFException</code>.
   * The actual number of bytes skipped is returned.  If <code>n</code>
   * is negative, no bytes are skipped.
   *
   * @param      n   the number of bytes to be skipped.
   * @return     the actual number of bytes skipped.
   * @exception  IOException  if an I/O error occurs.
   */
  public int skipBytes(int n) throws IOException {
    return raf.skipBytes(n);
  }

  /**
   * Returns the length of this file.
   *
   * @return     the length of this file, measured in bytes.
   * @exception  IOException  if an I/O error occurs.
   */
  public long length() throws IOException {
    return raf.length();
  }

  /**
   * Returns the current thread id. If this id was not used to
   * register a Transaction before an <code>IOException</code> will
   * be thrown.
   * @return the current thread's id registered with a Transaction
   * @exception IOException if the current thread is not associated
   *                     with any Transaction
   */
  private long getCurrentThreadId() throws IOException {
    Thread th = Thread.currentThread();
    long th_id = th.getId();

    if (xares.isEmpty() || !xares.containsKey(th_id)) {
      throw new IOException("There is no thread-transaction association. " +
          "\nCurrent thread with id=<" + th_id + "> has not been registered " +
          "with any Transaction. Possibly a read/write operation happens outside " +
          "the scope of TransactionManager (begin - commit/rollback.) or the " +
          "\nnewTransaction() method has not been called after the TransactionManager " +
          "has begun.");
    }
    return th_id;
  }

  /**
   * Used by <code>XAResourceManager</code> after starting-up
   * recovery procedure
   * @exception FileNotFoundException if the File does not exist
   */
  protected void initRAF() throws FileNotFoundException {
    raf = new RandomAccessFile(filename, mode);
  }

  /**
   * Converts an array of type <code>int</code> to an array of
   * type <code>byte</code> and returns that.
   *
   * @param ints the array to convert into a <code>byte</code> array
   * @return an array of bytes containing the values from the
   *         <code>ints</code> array
   */
  protected static byte[] getBytesFromInts(int[] ints) {
    byte[] bytes = new byte[ints.length];
    for (int b = 0; b < ints.length; b++) {
      bytes[b] = (byte) ints[b];
    }
    return bytes;
  }

  /**
   * Converts an array of type <code>byte</code> to an array of
   * type <code>int</code> and returns that.
   *
   * @param bytes the array to convert into an <code>int</code> array
   * @return an array of <code>int</code> containing the values from the
   *         <code>bytes</code> array.
   */
  protected static int[] getIntsFromBytes(byte[] bytes) {
    int[] ints = new int[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      ints[i] = bytes[i];
    }
    return ints;
  }

  protected void finalize() throws Throwable {  //todo comments
  try {
    if (raf != null)
      raf.close();        // close if file is open
  } finally {
      super.finalize();
    }
  }
}