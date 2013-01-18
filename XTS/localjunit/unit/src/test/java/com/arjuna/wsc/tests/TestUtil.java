/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
