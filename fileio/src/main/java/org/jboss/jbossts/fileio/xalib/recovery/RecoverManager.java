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
package org.jboss.jbossts.fileio.xalib.recovery;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;

/**
 * This class can be used to recover incomplete transactions after
 * a system crash.
 *
 * @author Ioannis Ganotis
 * @version Aug 28, 2008
 */
public class RecoverManager
{
  /**
   * Constructor to create RecoverManager objects.
   * Every object will try to get a {@link com.arjuna.ats.arjuna.recovery.RecoveryManager}
   * and start a thread on it.
   */
  public RecoverManager() {
    RecoveryManager rm = RecoveryManager.manager();
    rm.startRecoveryManagerThread();
  }

  public static void main (String[] args) {
    new RecoverManager();
  }
}
