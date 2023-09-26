/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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