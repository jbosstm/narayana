/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
* SharedTimer.java
*
* Copyright (c) 2004 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* $Id: SharedTimer.java 2342 2006-03-30 13:06:17Z  $
*/
package com.arjuna.common.util;

import java.util.Timer;

/**
 * Provides a shared Timer to save resources.
 *
 * From the doc of java.util.Timer:
 * Corresponding to each Timer object is a single background thread that is used to execute all of the timer's tasks,
 * sequentially. Timer tasks should complete quickly. If a timer task takes excessive time to complete, it "hogs" the
 * timer's task execution thread. This can, in turn, delay the execution of subsequent tasks, which may "bunch up" and
 * execute in rapid succession when (and if) the offending task finally completes.
 *
 * @author  Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $
 */
public class SharedTimer {

    /**
     * create a timer that uses a daemon thread to back up its logic.
     */
    static private Timer timer = new Timer(true);

    public static Timer getTimer() {
        return timer;
    }
}
