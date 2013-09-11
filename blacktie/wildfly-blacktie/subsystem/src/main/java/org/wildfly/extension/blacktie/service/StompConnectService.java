/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.wildfly.extension.blacktie.service;

import org.codehaus.stomp.jms.StompConnect;
import org.jboss.as.network.SocketBinding;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.blacktie.logging.BlacktieLogger;

/**
 * @author <a href="mailto:zfeng@redhat.com">Amos Feng</a>
 *
 */
public class StompConnectService implements Service<StompConnectService> {
    private InjectedValue<SocketBinding> stompBindingInjector = new InjectedValue<SocketBinding>();
    private String connectionFactoryName;
    private String uri;
    private StompConnect connect;

    public StompConnectService(String connectionFactoryName) {
        this.connectionFactoryName = connectionFactoryName;
    }

    public InjectedValue<SocketBinding> getInjectedSocketBinding() {
        return stompBindingInjector;
    }

    @Override
    public StompConnectService getValue() throws IllegalStateException, IllegalArgumentException {
        if (BlacktieLogger.ROOT_LOGGER.isTraceEnabled()) {
            BlacktieLogger.ROOT_LOGGER.trace("StompConnectService.getValue");
        }
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if (BlacktieLogger.ROOT_LOGGER.isTraceEnabled()) {
            BlacktieLogger.ROOT_LOGGER.trace("StompConnectService.start");
        }

        final SocketBinding stompBinding = stompBindingInjector.getValue();
        String bindAddress = stompBinding.getAddress().getHostAddress();
        int port = stompBinding.getPort();

        uri = "tcp://" + bindAddress + ":" + port;

        try {
            BlacktieLogger.ROOT_LOGGER.info("Starting StompConnect " + uri);
            connect = new StompConnect();
            connect.setUri(uri);
            connect.setXAConnectionFactoryName(connectionFactoryName);
            connect.start();
            BlacktieLogger.ROOT_LOGGER.info("Started StompConnect " + connect.getUri());
        }catch(Exception e) {
            throw new StartException(e);
        }

    }

    @Override
    public void stop(StopContext context) {
        if (BlacktieLogger.ROOT_LOGGER.isTraceEnabled()) {
            BlacktieLogger.ROOT_LOGGER.trace("StompConnectService.stop");
        }
        try {
            connect.stop();
            BlacktieLogger.ROOT_LOGGER.info("Stopped StompConnect " + uri);
        } catch(Exception e) {
            BlacktieLogger.ROOT_LOGGER.error("Stopping StompConnect failed with " + e);
        }
    }
}
