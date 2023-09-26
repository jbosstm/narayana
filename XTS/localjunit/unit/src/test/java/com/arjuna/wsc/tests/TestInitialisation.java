/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wsc.tests;

import com.arjuna.wsc11.ContextFactoryMapper;
import com.arjuna.wsc11.RegistrarMapper;

/**
 * Initialise the test.
 * @author kevin
 */
public class TestInitialisation
{
    public static void testSetup()
    {
        final ContextFactoryMapper contextFactoryMapper = ContextFactoryMapper.getMapper() ;
        final TestContextFactory testContextFactory = new TestContextFactory(TestUtil.COORDINATION_TYPE) ;

        contextFactoryMapper.addContextFactory(TestUtil.COORDINATION_TYPE, testContextFactory) ;
        contextFactoryMapper.addContextFactory(TestUtil.INVALID_CREATE_PARAMETERS_COORDINATION_TYPE, testContextFactory) ;

        final RegistrarMapper registrarMapper = RegistrarMapper.getFactory() ;
        final TestRegistrar testRegistrar = new TestRegistrar() ;

        registrarMapper.addRegistrar(TestUtil.PROTOCOL_IDENTIFIER, testRegistrar) ;
        registrarMapper.addRegistrar(TestUtil.ALREADY_REGISTERED_PROTOCOL_IDENTIFIER, testRegistrar) ;
        registrarMapper.addRegistrar(TestUtil.INVALID_PROTOCOL_PROTOCOL_IDENTIFIER, testRegistrar) ;
        registrarMapper.addRegistrar(TestUtil.INVALID_STATE_PROTOCOL_IDENTIFIER, testRegistrar) ;
        registrarMapper.addRegistrar(TestUtil.NO_ACTIVITY_PROTOCOL_IDENTIFIER, testRegistrar) ;
    }

    public static void testTeardown()
    {
        final ContextFactoryMapper contextFactoryMapper = ContextFactoryMapper.getMapper() ;
        contextFactoryMapper.removeContextFactory(TestUtil.COORDINATION_TYPE);
        contextFactoryMapper.removeContextFactory(TestUtil.INVALID_CREATE_PARAMETERS_COORDINATION_TYPE);

        final RegistrarMapper registrarMapper = RegistrarMapper.getFactory() ;
        registrarMapper.removeRegistrar(TestUtil.PROTOCOL_IDENTIFIER);
        registrarMapper.removeRegistrar(TestUtil.ALREADY_REGISTERED_PROTOCOL_IDENTIFIER);
        registrarMapper.removeRegistrar(TestUtil.INVALID_PROTOCOL_PROTOCOL_IDENTIFIER);
        registrarMapper.removeRegistrar(TestUtil.INVALID_STATE_PROTOCOL_IDENTIFIER);
        registrarMapper.removeRegistrar(TestUtil.NO_ACTIVITY_PROTOCOL_IDENTIFIER);
    }
}