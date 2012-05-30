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
package org.jboss.jbossts.fileio.xalib.txfiles.exceptions;

import java.io.IOException;

/**
 * Signals that a lock cannot be granted to either read from or write
 * to a Transactional file.
 * <p>
 * The exception is mainly used by {@link org.jboss.jbossts.fileio.xalib.txfiles.file.XAFile} in the attempt of
 * reading from or writing to a file.
 * 
 * @author Ioannis Ganotis
 * @version Jul 28, 2008
 */
public class LockRefusedException extends IOException {
  /**
   * Constructs a <code>LockRefusedException</code> with <code>null</code>
   * as its error detail message.
   */
  public LockRefusedException() {
    super();
  }

  /**
   * Constructs a <code>LockRefusedException</code> with the
   * given <code>msg</code> as its detail message.
   * <p>
   * The string <code>msg</code> may later be retrieved by the
   * {@link Throwable#getMessage()} method of class <code>java.lang.Throwable</code>
   * @param msg the detail message
   */
  public LockRefusedException(String msg) {
    super(msg);
  }
}
