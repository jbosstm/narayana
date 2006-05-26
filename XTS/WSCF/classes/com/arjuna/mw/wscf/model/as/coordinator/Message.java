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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Message.java,v 1.2 2005/05/19 12:13:21 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.as.coordinator;

import com.arjuna.mw.wscf.common.Qualifier;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * Whenever a coordinator must contact a participant, it sends a protocol
 * specific notification message.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Message.java,v 1.2 2005/05/19 12:13:21 nmcl Exp $
 * @since 1.0.
 */

public interface Message
{

    /**
     * @exception SystemException Thrown if any error occurs.
     *
     * @return The unique identification of this notification. The participant
     * may use this as the only information necessary to process the request
     * from the coordinator.
     */

    public String messageName () throws SystemException;

    /**
     * Many notifications may be processed simply on the name of the message.
     * In those situations where this is not the case, additional protocol
     * specific data may be send and obtained via this method.
     *
     * @exception SystemException Thrown if any error occurs.
     *
     * @return The coordination specific data, or null if there is none.
     */

    public Object coordinationSpecificData () throws SystemException;

    /**
     * @exception SystemException Thrown if any error occurs.
     *
     * @return Any qualifiers associated with the notification, or null.
     * For example, "only vote to commit if you can remain in this state for
     * 24 hours".
     */

    public Qualifier[] qualifiers () throws SystemException;
    
}

