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

import java.io.*;
import java.util.LinkedList;

/**
 * This class is used to handle important information with log files.
 * <p>
 * The class provides both read and write operations to retrieve or
 * add new information to a log file. To manage this an object of
 * <code>RandomAccessFile</code> class is used and data are written
 * in binary format (<code>writeLong, writeInt</code>). The option
 * given by a <code>RandomAccessFile</code> object to define whether
 * to open the file in <code>r</code> or <code>rw</code> mode is useful
 * to disallow any writes when reading the <code>log</code>.
 *
 * @author Ioannis Ganotis
 * @version Jun 17, 2008
 */
public class RecordsLogger implements Serializable
{
  private String filename;
  transient private RandomAccessFile raf;

  /**
   * Constructor to create <code>RecordsLogger</code> objects that will
   * allow to handle <code>log</code> related information.
   * <p>
   * The constructor creates file with the given <code>filename</code>.
   * The access mode is defined to be in <code>rw</code> mode as when
   * the <code>XAResourceManager</code> opens the log to write the
   * <code>updatedBytes</code>. When it finishes, it closes the
   * <code>log</code> and if in recovery phase, it re-opens the log
   * in <code>r</code> mode and retrieves all the log entries.   *
   *
   * @param filename the name of the log file to store to or retrieve from,
   *                 necessary information
   * @throws IOException if an I/O error occurs
   */
  public RecordsLogger(String filename) throws IOException {
    this.filename = filename;
    raf = new RandomAccessFile(filename, "rw");
  }

  /**
   * Returns the name used to create this log file.
   * @return a <code>String</code> which represents the name of
   *         the log file.
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Opens the log file in <code>r</code> mode and reads all the
   * log entries. After that, the file is closed.
   *
   * @return a list with all the log entries contained in the log file
   * @throws IOException if an I/O error occurs
   */
  public synchronized LinkedList<LogEntry> readAllRecords() throws IOException {
    close();
    raf = new RandomAccessFile(filename, "r");
    LinkedList<LogEntry> records = new LinkedList<LogEntry>();
    boolean eof = false;
    do {
      try {
        long pos = raf.readLong();
        int len = raf.readInt();
        byte[] data = new byte[len];
        raf.read(data);
        LogEntry le = new LogEntry(pos, len, data);
        records.add(le);
      } catch (EOFException eofe) {
        eof = true;
      }
    } while (!eof);
    raf.close();
    return records;
  }

  /**
   * Appends the log file by adding a new <code>LogEntry</code> at
   * the end. It does not actually write the <code>LogEntry</code>
   * as an object but only the necessary information it contains.
   *
   * @param le the <code>LogEntry</code> to write to the log file
   * @throws IOException if an I/O error occurs
   */
  public synchronized void addInfo(LogEntry le) throws IOException {
    raf.writeLong(le.getPosition());
    raf.writeInt(le.getRecordLength());
    raf.write(le.getData());
  }

  /**
   * The method gets the <code>FileDescriptor</code> of the
   * <code>raf</code> object and calls the <code>sync</code> method
   * to force log data to be written to disk.
   * <p>
   * The method is used when preparing to commit, to ensure all the
   * updates have been written to the log.
   *
   * @exception IOException if an I/O error occurs
   */
  public void flush() throws IOException {
    FileDescriptor fd = raf.getFD();
    fd.sync();
  }

  /**
   * if the <code>raf</code> object exists close it.
   */
  public synchronized void close() {
    try {
      if (raf != null)
        raf.close();
    } catch (IOException ioe) {
      System.out.println("XXXX Error while processing file: " + filename + " XXXX");
    }
  }

  /**
   * Method to delete the log file with name <code>filename</code>.
   * Is used by the <code>XAResourceManager</code> when it has
   * committed or rolledback and the log is not more needed.
   */
  public synchronized void delete() {
    new File(filename).delete();
  }
}
