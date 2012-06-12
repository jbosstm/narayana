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
package org.jboss.jbossts.fileio.xalib.txfiles.locking;

import com.arjuna.ats.txoj.LockResult;
import javax.transaction.xa.Xid;
import java.io.*;
import java.util.*;

import org.jboss.jbossts.fileio.ObjectOutputStreamAppend;
import org.jboss.jbossts.fileio.xalib.Globals;

/**
 * This class is used to manage Locking on a {@link org.jboss.jbossts.fileio.xalib.txfiles.file.XAFile}. When methods
 * like read/write are invoked from within the <code>XAFile</code> class
 * locks are acquired automatically in read or write modes, respectively.
 * <p>
 * The class uses a list to maintain all the locks held by the VM. If
 * multiple VMs act on the same Transactional file simultaneously the
 * locks are kept in a file, so every VM is aware of which regions of the
 * file are locked. Processing a different <code>XAFile</code> will
 * produce another <em>lock file</em> containing the locked regions of
 * that Transactional file.
 * <p>
 * When a lock is acquired through the <code>tryLock</code> method, depending
 * on the outcome of the <code>manageLocks</code> a lock will either be
 * <em>GRANTED</em> or <em>REFUSED</em>. Internally, computations happen to
 * either add a new lock, update an existing one (by moving its bounds or
 * changing its access mode), or do nothing at all.
 *
 * @author Ioannis Ganotis
 * @version Jul 23, 2008
 */
public class XALockManager implements Serializable
{
  private String filename = Globals.LOCKS_FOLDER_PATH;
  transient private LinkedList<XALock> heldLocks;

  /**
   * Constructor to create <code>XALockManager</code> objects. Using such
   * an object <code>XAFile</code> can acquire read/write locks on specific
   * regions of any Transactional file.
   * <p>
   * The constructor also invokes a method to get all the locks that have
   * been held so far, before going any further.
   *
   * @param xaFilename the Transactional file on which to apply locks
   */
  public XALockManager(String xaFilename) {
    filename += getProcessedName(xaFilename);
    heldLocks = new LinkedList<XALock>();
    try {
      File f = new File(filename);
      if (f.exists()) {
        obtainHeldLocksWith(null);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private String getProcessedName(String xaFilename) {
    xaFilename = xaFilename.replace('/', '_');
    xaFilename = xaFilename.replace('\\', '_');
    return xaFilename.concat("_locks.log"); 
  }

  /**
   * This method opens an existing <em>locks file</em> and retrieves its existing
   * {@link XALock} objects. These objects now are kept in memory and can be used
   * for further processing.
   *
   * @param xid the global Transaction id that was used when a lock was acquired.
   *            if <code>xid</code> is <code>null</code> then all the locks that
   *            are in a file will be returned.
   * @return a list of {@link XALock} objects acquired by a Transaction with the
   *         given <code>xid</code>. If <code>xid</code> is <code>null</code> then
   *         all the locks that are stored in that file will be returned.
   * @exception IOException if an I/O error occurs
   */
  public synchronized LinkedList<XALock> obtainHeldLocksWith(Xid xid) throws IOException {
    File file = new File (filename);
    if (file.exists()) {
      FileInputStream fIn = new FileInputStream(filename);
      ObjectInputStream in = new ObjectInputStream(fIn);

      boolean eof = false;
      do {
        try {
          Object obj = in.readObject();
          XALock xaLock = (XALock) obj;
          if (xaLock.getXid() == xid || xid == null) {
            insertLockAt(binarySearch(xaLock), xaLock);
          }
        } catch (EOFException eofe) {
          eof = true;
        } catch (ClassNotFoundException cnfe) {
          cnfe.printStackTrace();
        }
      } while (!eof);
      in.close();
      fIn.close();
    }
    return heldLocks;
  }

  /**
   * The method is used to keep the memory locks synchronized
   * with the ones that already exist in the file. Non existing
   * ones will be just added to the <em>locks file</em>.
   */
  private synchronized void syncLocks() {
    try {
      FileOutputStream fOut = new FileOutputStream(filename);
      ObjectOutputStream out = new ObjectOutputStream(fOut);

      for (XALock lock : heldLocks) {
        out.writeObject(lock);
      }
      out.close();
      fOut.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  /**
   * This method tries to set a given <code>lock</code>. If the lock to
   * be set is <code>null</code> the outcome is to <em>REFUSE</em> that
   * lock. In other cases the lock is added, the <em>locks file</code>
   * is synchronized, or both apply.
   * @param lock the lock trying to set
   * @return <code>LockResult.GRANTED</code> if the lock can be granted
   *         or <code>LockResult.REFUSED</code> fail to grant the lock
   *
   * @exception IOException if an I/O error occurs while adding new locks
   */
  public synchronized int tryLock(XALock lock) throws IOException {
    if (lock != null) {
      int index = binarySearch(lock);
      int vResult = manageLocks(index, lock);
      if (vResult != Globals.REFUSE_LOCK) { // allowed to add the new lock
        if (vResult == Globals.ADD_NEW_LOCK || vResult == Globals.MOVE_LOCK_BOUNDS) {
          if (vResult == Globals.MOVE_LOCK_BOUNDS)
                syncLocks();
          insertLockAt(index, lock);
          FileOutputStream fOut;
          ObjectOutputStream out;
          File lockFile = new File(filename);

          if (lockFile.exists()) { // Append the file, write new locks at the end
            fOut = new FileOutputStream(filename, true);
            out = new ObjectOutputStreamAppend(fOut);
          } else {
            fOut = new FileOutputStream(filename);
            out = new ObjectOutputStream(fOut);
          }
          out.writeObject(lock);
          out.close();
          fOut.close();
        } else if (vResult == Globals.UPDATE_OLD_LOCK) {
          syncLocks();
        }
        return LockResult.GRANTED;
      }
    }
    return LockResult.REFUSED;
  }

  /**
   * Searches the list of locks in memory to find at which index the
   * <code>newLock</code> should be added. The list is sorted, so
   * when finds the appropriate index, returns it.
   *
   * @param newLock the lock to add in the locks list
   * @return an index at which the <code>newLock</code> is about to be added
   */
  private int binarySearch(XALock newLock) {  //todo fix comment
    int mid;
    int res = 0;
    int low = 0;
    int high = heldLocks.size() - 1;

    while( low <= high )
    {
      mid = ( low + high ) / 2;
      XALock curLock = heldLocks.get(mid);
      int compRes = curLock.compareTo(newLock);

      if( compRes > 0 ) {
        low = mid + 1;
        res = mid+1;
      }
      else if( compRes < 0 ) {
        high = mid - 1;
        res = mid;
      }
      else
        return mid;
    }
    return res;     // NOT_FOUND = -1
  }

  /**
   * Inserts the given <code>newLock</code> at the specified <code>index</code>.
   *
   * @param index the index to add the new lock
   * @param newLock the lock to be added
   */
  private void insertLockAt(int index, XALock newLock) {
    if (index >= 0) {
      heldLocks.add(index, newLock);
    }
  }

  /**
   * The method takes the given <code>xaLock</code> and checks any of its
   * existing neighbours. If any of the neighbours deny to add the new lock,
   * it will be <em>REFUSED</em> from being added. Other possible cases is
   * to return <code>Globals.UPDATE_OLD_LOCK</code>, <code>Globals.MOVE_LOCK_BOUNDS</code>,
   * or <code>Globals.ADD_NEW_LOCK</code>.
   * <p>
   * If one of the neighbours is <code>null</code> the result of the other neighbour is
   * returned instead. if both of them are <code>null</code> the method returns
   * <code>Globals.ADD_NEW_LOCK</code>, as this means there are no neighbours and as
   * a result there are no conflicts, so the new lock can be added safely.
   *
   * @param it the index at which the lock will be added. Before adding the lock this index
   *           points to the right neighbour, or to a <code>null</code> cell, if the
   *           neighbour does not exist.
   * @param xaLock the lock trying to add
   * @return returns <code>Globals.ADD_NEW_LOCK</code>, <code>Globals.UPDATE_OLD_LOCK</code>,
   *                 <code>Globals.MOVE_LOCK_BOUNDS</code>, or <code>Globals.REFUSE_LOCK</code>
   *                 depending on the result of computations.
   */
  private int manageLocks(int it, XALock xaLock) {
    if (xaLock.getXid() != null) {
      XALock leftLock  = null;
      XALock rightLock = null;

      if (it > 0)
        leftLock = heldLocks.get(it-1);
      if (it < heldLocks.size())
        rightLock = heldLocks.get(it);

      if (leftLock != null && rightLock != null) {
        int leftResult = xaLock.adjustWith(leftLock);
        int rightResult = xaLock.adjustWith(rightLock);

        if (leftResult == Globals.REFUSE_LOCK || rightResult == Globals.REFUSE_LOCK)
          return Globals.REFUSE_LOCK;
        else {
          if (leftResult == Globals.MOVE_LOCK_BOUNDS || rightResult == Globals.MOVE_LOCK_BOUNDS) {
            return Globals.MOVE_LOCK_BOUNDS;
          } else if (leftResult == Globals.UPDATE_OLD_LOCK || rightResult == Globals.UPDATE_OLD_LOCK) {
            return Globals.UPDATE_OLD_LOCK;
          } else if (leftResult == Globals.ADD_NEW_LOCK || rightResult == Globals.ADD_NEW_LOCK) {
            return Globals.ADD_NEW_LOCK;
          }
        }
      } else
      {
        if (leftLock == null && rightLock == null) {
          return Globals.ADD_NEW_LOCK;
        } else if (leftLock == null) {
          return xaLock.adjustWith(rightLock);
        } else {
          return xaLock.adjustWith(leftLock);
        }
      }
    }
    return Globals.REFUSE_LOCK;
  }

  /**
   * This method releases all the locks that have been held by
   * a Transaction with the given <code>xid</code>.
   * <p>
   * The method removes existing locks from the memory and then
   * synchronizes the <em>locks file</em>.
   *
   * @param xid the global Transaction id which was used for
   *            the locks trying to release
   */
  public void releaseLocks(Xid xid) {
    XALock[] locks = new XALock[heldLocks.size()];
    heldLocks.toArray(locks);
    for (XALock lock : locks) {
      if (lock.getXid() == xid) {
        heldLocks.remove(lock);
      }
    }
    syncLocks();
//    System.out.println("--- Locks participating in transaction with xid=" + xid +
//        " have been released!");
  }

  /**
   * Returns a list with all the held locks.
   * @return a list with all the held locks
   */
  public LinkedList<XALock> getHeldLocks() {
    return heldLocks;
  }

  /**
   * This method deletes the <em>locks file</em> and
   * is called by the <code>XAFile#close</code>, as it
   * is no more needed.
   */
  public void deleteFile() {
    File f = new File(filename);
    if (f.exists())
      f.delete();
  }
}
