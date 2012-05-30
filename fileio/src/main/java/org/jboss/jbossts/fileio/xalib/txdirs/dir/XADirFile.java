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

import org.apache.commons.transaction.file.ResourceManagerException;
import org.apache.commons.transaction.file.FileResourceManager;
import java.io.*;

/**
 * Instances of this class are used by {@link XADir} objects to
 * apply file operations. These operations can include creating
 * new, deletinig and renaming files.
 * <p>
 * Each of these file operations invoke methods from the Apache's
 * {@link org.apache.commons.transaction.file.FileResourceManager} object
 * to manipulate operations on the files.
 *
 * @see XADir
 * @see org.apache.commons.transaction.file.FileResourceManager
 *
 * @author Ioannis Ganotis
 * @version Aug 7, 2008
 */
public class XADirFile implements Serializable
{
  transient private FileResourceManager freMngr;
  private String curTxId;
  private String curObjId;
  private XADir xadir;
  private String filename;

  /**
   * Constructor to create <code>XADirFile</code> objects.
   * These objects can be used to apply file operations.
   *
   * @param file the <code>File</code> object that represents
   *        the file in the transactional directory
   * @param xadir the transactional directory
   */
  public XADirFile(File file, XADir xadir) {
    filename = file.getName();
    this.freMngr = xadir.getFreMngr();
    this.curTxId = xadir.getCurTxId();
    curObjId = "/" + file.getName();
    this.xadir = xadir;
  }

  /**
   * Returns the name of the file.
   *
   * @return <code>String</code> representing the name of the
   *         file in the transactional directory
   */
  public String getName() {
    return filename;
  }

  /**
   * Returns an InputStream containing the bytes of this
   * <code>XADirFile</code> file.
   *
   * @return an <code>InputStream</code> object
   * @exception ResourceManagerException ]
   *            if an error in the <code>ResourceManager</code> occurs
   */
  public InputStream readResource() throws ResourceManagerException {
    try {
      String obj = freMngr.getStoreDir() + curObjId;
      return new FileInputStream(new File(obj));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return null;
  }

  /**
   * Returns an OutputStream containing the bytes written to this
   * <code>XADirFile</code> file.
   *
   * @return an <code>OutputStream</code>
   * @throws ResourceManagerException
   *         if an error in the <code>ResourceManager</code> occurs
   */
  public OutputStream writeResource() throws ResourceManagerException {
    return freMngr.writeResource(curTxId, curObjId);
  }

  /**
   * Renames this file to the name of the file given by <code>file</code>.
   *
   * @param file the <codeFile</code> object that contains the new file name
   * @return true if the rename operations completed successfully;false otherwise
   *
   */
  public synchronized boolean renameTo(File file) {
    String resId = "/" + file.getName();
    try {
      freMngr.lockResource(curObjId, curTxId, false, false, 0, true);
      freMngr.copyResource(curTxId, curObjId, resId, true);
      freMngr.deleteResource(curTxId, curObjId);
      curObjId = resId;
      filename = file.getName();
      return true;
    } catch (ResourceManagerException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Create a new file in the disk. If the file already exists the method
   * will return false; otherwise true.
   *
   * @return true if the file was created successfully; false otherwise
   */
  public boolean createNewFile() {
    try {
      freMngr.createResource(curTxId, curObjId, false);
      xadir.increaseLength();
      return true;
    } catch (ResourceManagerException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * This method will delete the file. If the file does not
   * exist it will return false; Otherwise true.
   *
   * @return true if the file could be deleted successfully;
   *         false otherwise.
   */
  public boolean delete() {
    try {
      freMngr.lockResource(curObjId, curTxId, false, false, 0, true);
      freMngr.deleteResource(curTxId, curObjId, false);
      xadir.decreaseLength();
      return true;
    } catch (ResourceManagerException e) {
      e.printStackTrace();
    }
    return false;
  }
}