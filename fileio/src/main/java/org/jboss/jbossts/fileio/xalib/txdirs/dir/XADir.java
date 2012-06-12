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

import org.apache.commons.transaction.file.FileResourceManager;
import org.apache.commons.transaction.file.ResourceManagerException;
import org.apache.commons.transaction.util.CommonsLoggingLogger;
import org.apache.commons.transaction.util.PrintWriterLogger;
import org.apache.commons.logging.LogFactory;
import org.jboss.jbossts.fileio.xalib.txdirs.exceptions.NotDirectoryException;
import org.jboss.jbossts.fileio.xalib.txdirs.exceptions.IncompleteTransactionsException;
import org.jboss.jbossts.fileio.xalib.Globals;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import java.util.LinkedList;
import java.io.*;

/**
 * Instances of this class represent a transactional directory on which
 * file operations such as create, rename and delete can be applied. The
 * contents (files) of this directory are represented by {@link XADirFile}
 * objects. Through these objects the application programmer can access the
 * file operations.
 * <p>
 * It provides methods to list the files in the directory and access the
 * {@link org.apache.commons.transaction.file.FileResourceManager} of the Apache
 * commons transaction project.
 *
 * @see XADirFile
 * @see org.apache.commons.transaction.file.FileResourceManager
 * @see org.jboss.jbossts.fileio.xalib.txdirs.dir.XAFileResourceManager 
 *
 * @author Ioannis Ganotis
 * @version Aug 6, 2008
 */
public class XADir implements Serializable, Closeable
{
  private String curTxId;
  transient private FileResourceManager freMngr;
  private long length;

  /**
   * Constructor to create objects that represent a transactional directory.
   * <p>
   * The constructor checks if the <code>storeDir</code> is a directory or not.
   * If it is not then {@link org.jboss.jbossts.fileio.xalib.txdirs.exceptions.NotDirectoryException}
   * exception is thrown. Otherwise, a new <code>FileResourceManager</code> object
   * is created to allow access to transactional methods (e.g. start, commit, rollback
   * a transaction)
   *
   * @param storeDir directory where main data should go after commit
   * @exception org.jboss.jbossts.fileio.xalib.txdirs.exceptions.NotDirectoryException
   *            if the <code>storeDir</code> is not a directory
   * @exception org.apache.commons.transaction.file.ResourceManagerException
   *            if an error in the <code>FileResourceManager</code> occurs
   */
  public XADir(File storeDir) throws IOException, ResourceManagerException {
    if (!storeDir.exists()) {
      storeDir.mkdir();
    } else {
      if (!storeDir.isDirectory())
        throw new NotDirectoryException("The file given is not a directory.");
    }

    length = storeDir.list().length;
    String workDir = storeDir.getCanonicalPath() + "/" + Globals.WORK_DIR_NAME;
    freMngr = new FileResourceManager(storeDir.getCanonicalPath(), workDir, false,
        new PrintWriterLogger(new PrintWriter(System.out),
            XADirFile.class.getName(), false));
    freMngr.start(); // start the FileResourceManager service, must be started
  }                  // before using any of its methods

  /**
   * This method lists all the files under the transactional directory.
   * It lists only files and not <code>File</code>s as in Java this may
   * also mean directories.
   * @return a list of <code>XADirFile</code> objects which represent
   *         the files within the directory
   */
  public synchronized XADirFile[] listTXFiles() {
    File dir = new File(freMngr.getStoreDir());
    File[] files = dir.listFiles();
    LinkedList<XADirFile> xaDirFileList = new LinkedList<XADirFile>();

    for (File f : files) {
      if (!f.isDirectory()) {
        xaDirFileList.add(new XADirFile(f, this));
      }
    }
    XADirFile[] xaDirFiles = new XADirFile[xaDirFileList.size()];
    xaDirFileList.toArray(xaDirFiles);
    return xaDirFiles;
  }

  /**
   * This method must be used after a <code>TransactionManager</code>
   * has begun and within the boundaries of a transaction (<code>begin,
   * commit/rollback</code>).
   * <p>
   * The method also creates a new {@link XAFileResourceManager} and
   * enlists it to the transaction obtained by the <code>txnMngr</code>.
   *
   * @param txnMngr the <code>TransactionManager</code> used to commit
   *                or rollback the file operations
   * @exception javax.transaction.RollbackException
   *            if an error occurs while enlisting the <code>XAResource</code>
   * @exception javax.transaction.SystemException
   *            if there is a problem enlisting the XAResource created
   *            within this method or when the <code>getTransaction</code> in the
   *            <code>TransactionManager</code> fails or when the TransactionManager
   *            is not in ready (<code>Status.ACTIVE</code>) mode
   */
  public synchronized void startTransactionOn(TransactionManager txnMngr)
      throws SystemException, RollbackException {
    curTxId = "txDir-" + freMngr.getWorkDir().replace('/', '_').replace('\\', '_').replace(':', '_') + "_" +
        Thread.currentThread().getId() + "!" + System.nanoTime();
    XAFileResourceManager xafre = new XAFileResourceManager(freMngr, curTxId);
    Transaction tx = txnMngr.getTransaction();
    tx.enlistResource(xafre);
  }

  /**
   * Returns the <code>FileResourceManager</code> object used to access
   * transaction and file operations (e.g. startTransaction, copyResource).
   *
   * @return the <code>FileResourceManager</code> object
   */
  protected FileResourceManager getFreMngr() {
    return freMngr;
  }

  /**
   * This is the name of the "shadow" folder which keeps the changes whilst
   * a transaction is still in progress.
   *
   * @return a <code>String</code> which contains the name of the "shadow"
   *         folder
   */
  protected String getCurTxId() {
    return curTxId;
  }

  /**
   * Increases the counter of files in the directory by one
   */
  protected void increaseLength() {
    length++;
  }

  /**
   * Decreases the counter of files in the directory by one
   */
  protected void decreaseLength() {
    length--;
  }

  /**
   * Returns the number of files (not directories) under this
   * transactional directory.
   *
   * @return a <code>long</code> representing the number of files
   *         within the transactional directory
   */
  public long length() {
    return length;
  }

  /**
   * As this class represents a transactional directory, the application
   * programmer must call this method after his transactional work is over.
   * The method will release and remove any of the "shadow" files/folders
   * used whilist the transaction was in progress.
   * <p>
   * If there are pending transactions the method will fail to close.
   *
   * @exception IOException if an I/O error occurs
   */
  public void close() throws IOException {
    File store = new File (freMngr.getWorkDir());
    if (store.list().length != 0) // pending txs exist
      throw new IncompleteTransactionsException();

    store.delete();
    freMngr = null;
  }
}
