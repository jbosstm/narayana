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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Environment.java,v 1.6 2004/09/09 08:48:48 kconner Exp $
 */

package com.arjuna.mw.wst.common;

/**
 */

public interface Environment
{

    public static final String COORDINATOR_URL = "com.arjuna.mw.wst.coordinatorURL";

    public static final String ACTIVATION_REQUESTER = "com.arjuna.mw.wst.activationrequester";
    public static final String ACTIVATION_COORDINATOR = "com.arjuna.mw.wst.activationcoordinator";
    public static final String REGISTRATION_REQUESTER = "com.arjuna.mw.wst.registrationrequester";

    public static final String COMPLETION_PARTICIPANT = "com.arjuna.mw.wst.at.completionparticipant";
    public static final String COMPLETION_WITH_ACK_PARTICIPANT = "com.arjuna.mw.wst.at.completionwithackparticipant";

    public static final String DURABLE_TWOPC_DISPATCHER = "com.arjuna.mw.wst.at.durabletwopcdispatcher";
    public static final String DURABLE_TWOPC_PARTICIPANT = "com.arjuna.mw.wst.at.durabletwopcparticipant";
    public static final String VOLATILE_TWOPC_DISPATCHER = "com.arjuna.mw.wst.at.volatiletwopcdispatcher";
    public static final String VOLATILE_TWOPC_PARTICIPANT = "com.arjuna.mw.wst.at.volatiletwopcparticipant";

    public static final String BUSINESSAGREEMENTWPC_DISPATCHER = "com.arjuna.mw.wst.ba.businessagreementwpcdispatcher";
    public static final String BUSINESSAGREEMENTWPC_PARTICIPANT = "com.arjuna.mw.wst.ba.businessagreementwpcparticipant";
    public static final String BUSINESSAGREEMENTWCC_DISPATCHER = "com.arjuna.mw.wst.ba.businessagreementwccispatcher";
    public static final String BUSINESSAGREEMENTWCC_PARTICIPANT = "com.arjuna.mw.wst.ba.businessagreementwccparticipant";

    public static final String TERMINATOR_PARTICIPANT = "com.arjuna.mw.wst.ba.businessactivityterminatorparticipant";
    public static final String TERMINATOR_COORDINATOR = "com.arjuna.mw.wst.ba.businessactivityterminatorcoordinator";

}
