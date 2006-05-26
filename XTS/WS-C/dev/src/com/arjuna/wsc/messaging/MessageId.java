/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Created on 15-Jan-2005
 */
package com.arjuna.wsc.messaging;

import java.rmi.dgc.VMID;

/**
 * A simple message id generator, used when UID is inaccessible.
 * @author kevin
 */
public class MessageId
{
    /**
     * Prevent instantiation.
     */
    private MessageId()
    {
    }

    /**
     * Get the next message identifier.
     * @return The next message identifier.
     */
    public static String getMessageId()
    {
        return new VMID().toString() ;
    }
}
