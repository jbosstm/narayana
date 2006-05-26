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
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * TestUtil.java
 */

package com.arjuna.wsc.tests;

import javax.xml.namespace.QName;

public class TestUtil
{
    public static final String COORDINATION_TYPE                           = "http://wsc.example.org/coordination-type";
    public static final String UNKNOWN_COORDINATION_TYPE                   = "http://wsc.example.org/unknown-coordination-type";
    public static final String INVALID_CREATE_PARAMETERS_COORDINATION_TYPE = "http://wsc.example.org/invalid-create-parameters-coordination-type";

    public static final String PROTOCOL_IDENTIFIER                         = "http://wsc.example.org/protocol-identifier";
    public static final String UNKNOWN_PROTOCOL_IDENTIFIER                 = "http://wsc.example.org/unknown-protocol-identifier";
    public static final String ALREADY_REGISTERED_PROTOCOL_IDENTIFIER      = "http://wsc.example.org/already-registered-protocol-identifier";
    public static final String INVALID_PROTOCOL_PROTOCOL_IDENTIFIER        = "http://wsc.example.org/invalid-protocol-protocol-identifier";
    public static final String INVALID_STATE_PROTOCOL_IDENTIFIER           = "http://wsc.example.org/invalid-state-protocol-identifier";
    public static final String NO_ACTIVITY_PROTOCOL_IDENTIFIER             = "http://wsc.example.org/no-activity-protocol-identifier";

    public static final String PROTOCOL_PARTICIPANT_SERVICE                = "http://wsc.example.org/protocol-participant-service";
    public static final String PROTOCOL_COORDINATOR_SERVICE                = "http://wsc.example.org/protocol-coordinator-service";

    public static final String TEST_EXTENSION_VALUE                        = "TestExtensionValue";
    /**
     * The Namespace.
     */
    public static final String TEST_NAMESPACE = "http://example.org/test" ;
    /**
     * The namespace prefix.
     */
    public static final String TEST_PREFIX = "test" ;
    /**
     * The ExtensionValue element.
     */
    public static final String TEST_ELEMENT_EXTENSION_VALUE = "ExtensionValue" ;
    /**
     * The ExtensionValue QName.
     */
    public static final QName TEST_ELEMENT_EXTENSION_VALUE_QNAME = new QName(TEST_NAMESPACE, TEST_ELEMENT_EXTENSION_VALUE, TEST_PREFIX) ;
}
