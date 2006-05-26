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
 * $Id: Protocols.java,v 1.6 2004/12/21 09:49:14 kconner Exp $
 */

package com.arjuna.mw.wst.common;

/**
 */

public interface Protocols
{

    public static final String AtomicTransaction = "http://schemas.xmlsoap.org/ws/2004/10/wsat";
    public static final String BusinessActivityAtomic = "http://schemas.xmlsoap.org/ws/2004/10/wsba/AtomicOutcome";
    public static final String BusinessActivityMixed = "http://schemas.xmlsoap.org/ws/2004/10/wsba/MixedOutcome";

    /*
     * The AtomicTransaction subprotocols.
     */

    public static final String Completion = "http://schemas.xmlsoap.org/ws/2004/10/wsat/Completion";
    public static final String DurableTwoPhaseCommit = "http://schemas.xmlsoap.org/ws/2004/10/wsat/Durable2PC";
    public static final String VolatileTwoPhaseCommit = "http://schemas.xmlsoap.org/ws/2004/10/wsat/Volatile2PC";

    /*
     * The BusinessActivity subprotocols.
     */

    public static final String BusinessAgreementWithParticipantCompletion = "http://schemas.xmlsoap.org/ws/2004/10/wsba/ParticipantCompletion";
    public static final String BusinessAgreementWithCoordinatorCompletion = "http://schemas.xmlsoap.org/ws/2004/10/wsba/CoordinatorCompletion";
    public static final String BusinessAgreementTermination = "http://schemas.arjuna.com/ws/2005/10/wsarj/BATermination";
    
}
