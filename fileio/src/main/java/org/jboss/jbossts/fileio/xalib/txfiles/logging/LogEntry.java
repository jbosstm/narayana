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
package org.jboss.jbossts.fileio.xalib.txfiles.logging;

/**
 * This class represents an entry in a log file the information of which is
 * handled by a {@link RecordsLogger} object.
 * <p>
 * The class is only a single representation of such entries and is not written
 * in any of the log files. It is the information it includes that is written
 * and there are appropriate <em>get</em> methods to support this.
 *
 * @author Ioannis Ganotis
 * @version Jul 25, 2008
 */
public class LogEntry //implements Serializable
{
  private long position;
  private int recordLength;
  private byte[] data;

  /**
   * Constructor to create <code<LogEntry</code> objects to represent an
   * entry in the log file.
   *
   * @param position the position in the Transactional file of the first byte
   *                 in the <code>data</code> array of bytes
   * @param recordLength the length of the <code>data</code> to read/write
   * @param data an array of the updated bytes
   */
  public LogEntry(long position, int recordLength, byte[] data) {
    this.position = position;
    this.recordLength = recordLength;
    this.data = data;
  }

  /**
   * Returns the position of the first byte in the file.
   * @return the position of the first byte in the file
   */
  public long getPosition() {
    return position;
  }

  /**
   * Returns the length of the <code>data</code> of this record.
   * @return the length of the <code>data</code> of this record
   */
  public int getRecordLength() {
    return recordLength;
  }

  /**
   * Returns the actual updated bytes of this entry.
   * @return the actual updated bytes of this entry
   */
  public byte[] getData() {
    return data;
  }
}
