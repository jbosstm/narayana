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

import org.jboss.jbossts.fileio.xalib.txfiles.file.XAFile;

import java.io.Serializable;
import java.io.IOException;

/**
 * This class represents a record when reading or writing from an
 * {@link org.jboss.jbossts.fileio.xalib.txfiles.file.XAFile}.
 *
 * @author Ioannis Ganotis
 * @version Jun 12, 2008
 */
public class DataRecord implements Serializable
{
  private long startPoint;
  private int recordLength;
  private int[] recordBytes;

  /**
   * Constructor to create <code>DataRecord</code> objects to be used while
   * read/write operations are used in the <code>XAFile</code>. It is also used
   * by the {@link org.jboss.jbossts.fileio.xalib.txfiles.file.XAResourceManager} after reading a log file and while trying
   * to re-construct the records' information to be committed to the <code>XAFile</code>.
   *
   * @param startPoint the position in the file of the first byte in the <code>recordBytes</code>
   *                   array
   * @param recordLength the length of the updated bytes to commit
   * @param recordBytes the actual updated bytes
   * @exception  IOException if an I/O error occurs
   */
  protected DataRecord(long startPoint, int recordLength, int[] recordBytes)
      throws IOException {
    this.startPoint = startPoint;
    this.recordLength = recordLength;
    this.recordBytes = recordBytes;
  }

  /**
   * Returns the updated bytes as an array of <code>Integer</code>s.
   * @return an array of <code>int</code>s that contains the updated
   *         bytes
   */
  protected int[] getIntBytes() {
    return recordBytes;
  }

  /**
   * Returns the position in the file of the first byte in the
   * <code>recordBytes</code> array.
   * @return a <code>long</code> which represents the starting
   *         position of this record in the file
   */
  protected long getStartPosition() {
    return startPoint;
  }

  /**
   * Returns the length of this record
   * @return an <code>int</code> which represents the length of
   * this record
   */
  protected int getRecordLength() {
    return recordLength;
  }

  /**
   * Returns a <code>String</code> which contains all the updated bytes
   * of this record.
   * @return a <code>String</code> which represents the updated bytes of
   *         this record
   */
  protected String getRecordStr() {
    return toString(getIntBytes());
  }

  /**
   * Returns a <code>byte</code> array which contains the updated bytes
   * held by this record.
   * @return an array of bytes which are the updates held by this record
   */
  protected byte[] getRecordBytes() {
    return XAFile.getBytesFromInts(recordBytes);
  }

  /**
   * Converts the given <code>ints</code> array into a <code>String</code>
   * used to display a more readable representation of the updated bytes
   * held by this record.
   * @param ints the array of updated bytes to convert
   * @return a <code>String</code> that incluldes the updated bytes in a
   *         readable format
   */
  private String toString(int[] ints) {
    String txt = "[ ";
    for (int i : ints) {
      txt += i + ", ";
    }
    return txt.substring(0, txt.length() - 2) + " ]";
  }
}