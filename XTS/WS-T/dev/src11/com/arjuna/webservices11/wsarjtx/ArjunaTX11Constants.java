/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
package com.arjuna.webservices11.wsarjtx;

import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;

import javax.xml.namespace.QName;

/**
 * Interface containing Arjuna WS constants.
 */
public interface ArjunaTX11Constants
{

    /**
     * The termination participant service name.
     */
    public String TERMINATION_PARTICIPANT_SERVICE_NAME = "TerminationParticipantService" ;
    /**
     * The termination participant service qname.
     */
    public QName TERMINATION_PARTICIPANT_SERVICE_QNAME = new QName(ArjunaTXConstants.WSARJTX_NAMESPACE, TERMINATION_PARTICIPANT_SERVICE_NAME, ArjunaTXConstants.WSARJTX_PREFIX) ;

    /**
     * The termination participant soap service port name
     */
     public String TERMINATION_PARTICIPANT_PORT_NAME = "TerminationParticipantPortType";
    /**
     * The termination participant soap service port qname
     */
     public QName TERMINATION_PARTICIPANT_PORT_QNAME = new QName(ArjunaTXConstants.WSARJTX_NAMESPACE, TERMINATION_PARTICIPANT_PORT_NAME, ArjunaTXConstants.WSARJTX_PREFIX);

    /**
     * The termination coordinator service name.
     */
    public String TERMINATION_COORDINATOR_SERVICE_NAME = "TerminationCoordinatorService" ;
    /**
     * The termination coordinator service qname.
     */
    public QName TERMINATION_COORDINATOR_SERVICE_QNAME = new QName(ArjunaTXConstants.WSARJTX_NAMESPACE, TERMINATION_COORDINATOR_SERVICE_NAME, ArjunaTXConstants.WSARJTX_PREFIX) ;

    /**
     * The termination coordinator soap service port name
     */
     public String TERMINATION_COORDINATOR_PORT_NAME = "TerminationCoordinatorPortType";
    /**
     * The termination coordinator soap service port qname
     */
     public QName TERMINATION_COORDINATOR_PORT_QNAME = new QName(ArjunaTXConstants.WSARJTX_NAMESPACE, TERMINATION_COORDINATOR_PORT_NAME, ArjunaTXConstants.WSARJTX_PREFIX);
}