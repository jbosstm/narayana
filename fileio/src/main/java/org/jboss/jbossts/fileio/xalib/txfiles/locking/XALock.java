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

import com.arjuna.ats.txoj.LockMode;
import javax.transaction.xa.Xid;
import java.io.Serializable;
import java.util.Hashtable;

import org.jboss.jbossts.fileio.xalib.Globals;

/**
 * Instances of this class allow accessing the information held by each lock.
 * The <em>byte-range</em> to lock, the <code>mode</code> as well as the
 * <code>xid</code> are the ones that define a lock.
 * <p>
 * Each lock object has a separate mode for each byte in its byte-range. When
 * initialised all of these bytes have the mode passed in the constructor during
 * initialisation. Computations by the <code>adjustWith</code> method determine
 * which of the bytes within the byte-range needs to change mode. This will allow
 * more concurrent operations acting on the same lock.
 * <p>
 * The class implements {@link java.io.Serializable} as the objects need to be
 * stored in a file by the {@link XALockManager}. It also implements the
 * {@link Comparable} interface to allow comparisons between <code>XALock</code>
 * objects.
 *
 * @author Ioannis Ganotis
 * @version Jul 23, 2008
 */
public class XALock implements Serializable, Comparable<XALock>
{
  private Xid xid;
  private long startPosition;
  private long lockLength;
  private Hashtable<Long, Integer> byteModes;

  /**
   * Constructor to create <code>XALock</code> objects. These objects are
   * managed by the {@link XALockManager} and are stored in a <em>locks file</em>.
   *
   * @param xid the global Transaction id which was used when the lock was acquired
   * @param mode the mode (<code>LockMode.READ</code or <code>LockMode.WRITE) in which
   *             the bytes in the byte-range will be initialised
   * @param startPosition the position in the file of the first byte in the byte-range
   * @param lockLength the number of bytes to lock
   */
  public XALock(Xid xid, int mode, long startPosition, long lockLength) {
    this.xid = xid;
    this.startPosition = startPosition;
    this.lockLength = lockLength;

    initModes(mode);
  }

  /**
   * The method is used to initialise all the bytes in the byte range to the
   * given <code>mode</code>.
   *
   * @param mode the mode in which the bytes will be initialised
   */
  private void initModes(int mode) {
    byteModes = new Hashtable<Long, Integer>(89);
    long sp = startPosition;
    while (sp < getEndPosition()) {
      byteModes.put(sp++, mode);
    }
  }

  /**
   * Returns the global id used when this lock was acquired.
   *
   * @return the global Transaction id used when the lock was
   *         acquired
   */
  protected Xid getXid() {
    return xid;
  }

  /**
   * This method sets a <code>newMode</code> at <code>position</code>
   *
   * @param position the position at which the new mode will be set
   * @param newMode the new mode to set to the byte at <code>position</code>
   */
  private void setModeAt(long position, int newMode) {
    byteModes.put(position, newMode);
  }

  /**
   * This method sets a <code>newMode</code> to a byte-range.
   *
   * @param position the position of the first byte in the byte range
   * @param len the number of bytes that will change mode
   * @param newMode the new mode to set to a range of bytes
   */
  private void setModeStartingAt(long position, long len, int newMode) {
    long ps = position;
    while (ps < len+position) {
      setModeAt(ps++, newMode);
    }
  }

  /**
   * Returns the position in the file where this lock starts.
   *
   * @return the start position of this lock
   */
  private long getStartPosition() {
    return startPosition;
  }

  /**
   * This method sets a new start position of this lock. The
   * method is especially useful when the bounds of the lock
   * have to be moved.
   *
   * @param startPosition the new start position of this lock
   */
  private void setStartPosition(long startPosition) {
    long len_difference = this.startPosition - startPosition;
    lockLength += len_difference;
    this.startPosition = startPosition;
  }

  /**
   * Returns the length of the locked (by this lock) region in the
   * Transactional file.
   *
   * @return a <code>long</code> which specifies how many bytes have
   *         been locked by this lock
   */
  private long getLockLength() {
    return lockLength;
  }

  /**
   * This method modifies the amount of bytes locked by this lock
   *
   * @param lockLength how many bytes to lock
   */
  private void setLockLength(long lockLength) {
    this.lockLength = lockLength;
  }

  /**
   * Returns the end position in the file of this lock.
   *
   * @return a <code>long</code> which represents the end
   *         of this lock within the Transactional file
   */
  private long getEndPosition() {
    return getStartPosition() + getLockLength();
  }

  /**
   * This method returns true if the <code>mode</code> is contained
   * in the byte-range of this lock; otherwise returns false.
   *
   * @param mode the mode to check if exists
   * @return true if the <code>mode</code> exists in the byte-range;
   *         false otherwise
   */
  private boolean containsMode(int mode) {
    return byteModes.containsValue(mode);
  }

  /**
   * This method checks if the <code>mode</code> is contained within a
   * specified range (sub-range) in the byte-range of this lock.
   *
   * @param sp the start position of the sub-range to search
   * @param ep the end position of the sub-range
   * @param mode the mode to search if exists
   * @return true if the mode exists within the given sub-range
   */
  private boolean containsModeBetween(long sp, long ep, int mode) {
    for (long p=sp; p<ep; p++) {
      if (byteModes.get(sp) == mode)
        return true;
    }
    return false;
  }

  /**
   * This method returns true if the <code>newLock</code> conflicts with this
   * lock; false otherwise.
   *
   * @param newLock the lock trying to add
   * @return true if there is a conflict between <code>this</code> lock and the
   *         <code>newLock</code>; false otherwise
   */
  private boolean conflictsWith(XALock newLock) {
    return newLock == null                   || this.wraps(newLock)                ||
           this.isWrappedBy(newLock)         || this.equals(newLock)               ||
           this.intersectedFromLeft(newLock) || this.intersectedFromRight(newLock);
  }

  /**
   * This method performs some calculations to return a result whether
   * a conflict exists or not. If the conflict exists then it tries to
   * check the byte-range to see if the lock can be granted or not. If
   * needed the method will modify the bounds of existing locks so the
   * new one can fit next to it.
   *
   * @param neighLock the lock located next to this lock
   * @return <code>Globals.UPDATE_OLD_LOCK</code>, <code>Globals.ADD_NEW_LOCK</code>
   *         <code>Globals.MOVE_LOCK_BOUNDS</code>, <code>Globals.REFUSE_LOCK</code>,
   *         <code>Globals.NO_MOD_LOCK</code>, depending on the outcome of computations 
   */
  protected int adjustWith(XALock neighLock) {
    final int R_M = LockMode.READ;
    final int W_M = LockMode.WRITE;
    int result = Globals.REFUSE_LOCK;

    if (neighLock != null && neighLock.conflictsWith(this)) {
      if (neighLock.getXid() == this.getXid()) {
        if (this.equals(neighLock) || neighLock.wraps(this)) {
          if (!this.containsMode(W_M))
            result = Globals.NO_MOD_LOCK;
          if (!this.containsMode(R_M)) {
            neighLock.setModeStartingAt(this.getStartPosition(), this.getLockLength(), W_M);
            result = Globals.UPDATE_OLD_LOCK;
          }
        } else if (neighLock.intersectedFromLeft(this)) {
          long neighSP = neighLock.getStartPosition();
          long conflict_length = this.getEndPosition() - neighSP;
          if (!this.containsMode(R_M)) // the new Lock is in W_M
            neighLock.setModeStartingAt(neighSP, conflict_length, W_M);
          this.setLockLength(this.getLockLength() - conflict_length);
          result = Globals.MOVE_LOCK_BOUNDS;
        } else if (neighLock.intersectedFromRight(this)) {
          long neighEP = neighLock.getEndPosition();
          long conflict_length = neighEP - this.getStartPosition();
          if (!this.containsMode(R_M)) // the new lock is in W_M
            neighLock.setModeStartingAt(this.getStartPosition(), conflict_length, W_M);
          this.setStartPosition(neighEP); // lock length will be calculated by this method
          result = Globals.MOVE_LOCK_BOUNDS;
        } else if (neighLock.isWrappedBy(this)) {
          if (!this.containsMode(R_M)) {
            neighLock.setStartPosition(this.getStartPosition());
            neighLock.setLockLength(this.getLockLength());
            neighLock.initModes(W_M);
            result = Globals.UPDATE_OLD_LOCK;
          }
        }
      } else { //different Transaction
        if (this.containsMode(W_M) || neighLock.containsMode(W_M)) {
          result = Globals.REFUSE_LOCK;
        } else if (neighLock.intersectedFromLeft(this)) {
          if (!this.containsMode(W_M)) {
            if (neighLock.containsModeBetween(neighLock.getStartPosition(), this.getEndPosition(), R_M))
              result = Globals.NO_MOD_LOCK;
            else
              result = Globals.REFUSE_LOCK;
          }
        } else if (neighLock.intersectedFromRight(this)) {
          if (!this.containsMode(W_M)) {
            if (neighLock.containsModeBetween(this.getStartPosition(), neighLock.getEndPosition(), R_M))
              result = Globals.NO_MOD_LOCK;
            else
              result = Globals.REFUSE_LOCK;
          }
        }
      }
    } else {
      return Globals.ADD_NEW_LOCK;
    }
    return result;
  }

  /**
   * The method returns true if an existing lock surrounds the new lock;
   * false otherwise.
   *
   * @param newLock the lock trying to add
   * @return true if the existing lock surrounds the new lock; false othewise
   */
  private boolean wraps(XALock newLock) {
    return (this.getStartPosition() <= newLock.getStartPosition() &&
            this.getEndPosition() > newLock.getEndPosition()) ||
           (this.getStartPosition() < newLock.getStartPosition() &&
            this.getEndPosition() >= newLock.getEndPosition());
  }

  /**
   * The method returns true if the existing lock is surrounded by the
   * new lock; false otherwise.
   *
   * @param newLock the lock trying to add
   * @return true if the existing lock is surrounded by the new lock;
   *         false otherwise
   */
  private boolean isWrappedBy(XALock newLock) {
    return (this.getStartPosition() >= newLock.getStartPosition() &&
            this.getEndPosition() < newLock.getEndPosition()) ||
           (this.getStartPosition() > newLock.getStartPosition() &&
            this.getEndPosition() <= newLock.getEndPosition());
  }

  /**
   * Returns true if the existing lock conflicts from the left hand side
   * with the new lock; false otherwise.
   *
   * @param newLock the lock trying to add
   * @return true if the existing lock conflicts from the left hand side
   *         with the new lock; false otherwise
   */
  private boolean intersectedFromLeft(XALock newLock) {
    return newLock.getStartPosition() < this.getStartPosition() &&
           newLock.getEndPosition() <= this.getEndPosition();
  }

  /**
   * Returns true if the existing lock conflicts from the right hand side
   * with the new lock; false otherwise.
   *
   * @param newLock the lock trying to add
   * @return true if the existing lock conflicts from the right hand side
   *         with the new lock; false otherwise
   */
  private boolean intersectedFromRight(XALock newLock) {
    return newLock.getStartPosition() >= this.getStartPosition() &&
           newLock.getEndPosition() > this.getEndPosition();
  }

  /**
   * This method compares two locks.
   * <p>It will return:
   * <code>-1</code> if the new lock is on the right side of the existing lock
   * <p>
   * <code>0</code> if both of the locks are starting at the same position
   * <p>
   * <code>1</code> if the new lock is on the left side of the existing lock
   *
   * @param newXALock the lock trying to add
   * @return  <code>-1</code> if the new lock is on the right side of the existing lock
   *          <code>0</code> if both of the locks are starting at the same position
   *          <code>1</code> if the new lock is on the left side of the existing lock
   */
  public int compareTo(XALock newXALock) {
    if (getStartPosition() < newXALock.getStartPosition()) {
      return 1;
    } else if (getStartPosition() > newXALock.getStartPosition()) {
      return -1;
    }
    return 0;
  }

  /**
   * This method checks the equality of two locks. It will return
   * true if both of the locks start at the same point in the
   * Transactional file and they have the same length; false
   * otherwise.
   * 
   * @param newLock the lock trying to add
   * @return true if the locks are equal; false otherwise
   */
  private boolean equals(XALock newLock) {
    return (this.compareTo(newLock) == 0 &&
        getLockLength() == newLock.getLockLength());
  }
}
