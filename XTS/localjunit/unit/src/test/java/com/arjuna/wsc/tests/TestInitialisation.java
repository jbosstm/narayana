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
