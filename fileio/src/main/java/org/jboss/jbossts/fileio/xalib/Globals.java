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
package org.jboss.jbossts.fileio.xalib;

/**
 * This class defines some global constants used by the
 * <code>XA_lib</code>.
 * 
 * @author Ioannis Ganotis
 * @version Jun 12, 2008
 */
public class Globals
{
  //-------------------- txfiles --------------------
  public static final String LOG_FOLDER_PATH = "Logging";
  public static final String LOCKS_FOLDER_PATH = "Locks/";
  public static final int THREAD_TIMEOUT = 15000;
  public static final int TX_GROUPS = 20;
  // Used in Lock policies
  public static final int NO_MOD_LOCK = 100;
  public static final int REFUSE_LOCK = -100;
  public static final int UPDATE_OLD_LOCK = 50;
  public static final int ADD_NEW_LOCK = 1;
  public static final int MOVE_LOCK_BOUNDS = 200;
  // Recovery
  public static final long RECOVERY_ID = -100;

  // -------------------- txdirs --------------------
  public static final String WORK_DIR_NAME = "txDir_work";
}