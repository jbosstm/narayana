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
package org.jboss.jbossts.fileio.xalib.txdirs.exceptions;

import java.io.IOException;

/**
 * Signals that there are incomplete transactions
 * <p>
 * The exception is mainly used by the
 * {@link org.jboss.jbossts.fileio.xalib.txdirs.dir.XADir#close()} method when
 * invoked by the application programmer while there are active transactions.
 *
 * @author Ioannis Ganotis
 * @version Aug 23, 2008
 */
public class IncompleteTransactionsException extends IOException
{
  /**
   * Constructs a <code>IncompleteTransactionsException</code> with
   * <code>null</code> as its error detail message.
   */
  public IncompleteTransactionsException() {
    super();
  }

  /**
   * Constructs a <code>IncompleteTransactionsException</code> with
   * the given <code>msg</code> as its detail message.
   * <p>
   * The string <code>msg</code> may later be retrieved by the
   * {@link Throwable#getMessage()} method of class <code>java.lang.Throwable</code>
   * @param msg the detail message
   */
  public IncompleteTransactionsException(String msg) {
    super(msg);
  }

}
