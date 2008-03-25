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

package org.jboss.transactions.xts.recovery;

import com.arjuna.ats.arjuna.gandiva.inventory.Inventory;

/**
 * Module specific class that is responsible for adding any implementations
 * to the inventory.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Implementations.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */
public class Implementations {

    public static synchronized boolean added ()
    {
        return _added;
    }

    public static synchronized void initialise ()
    {
        if (!_added)
        {
            // WS-AT Participant records.
            Inventory.inventory().addToList(new ParticipantRecordSetup());

            _added = true;
        }
    }

    private Implementations ()
    {
    }

    private static boolean _added = false;

    static
    {
        initialise();
    }

}
