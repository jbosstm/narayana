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

package org.wildfly.extension.blacktie;

import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.network.SocketBinding;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.wildfly.extension.blacktie.configuration.Attribute;
import org.wildfly.extension.blacktie.logging.BlacktieLogger;
import org.wildfly.extension.blacktie.service.StompConnectService;

/**
 * @author <a href="mailto:zfeng@redhat.com">Amos Feng</a>
 *
 */
final class BlacktieSubsystemAdd extends AbstractBoottimeAddStepHandler {
    static final BlacktieSubsystemAdd INSTANCE = new BlacktieSubsystemAdd();

    private BlacktieSubsystemAdd() {
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        if(BlacktieLogger.ROOT_LOGGER.isTraceEnabled()) {
            BlacktieLogger.ROOT_LOGGER.trace("BlacktieSubsystemAdd.populateModel");
        }

        BlacktieSubsystemDefinition.CONNECTION_FACTORYNAME.validateAndSet(operation, model);
        BlacktieSubsystemDefinition.SOCKET_BINDING.validateAndSet(operation, model);
    }

    @Override
    protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
                    throws OperationFailedException {
        if(BlacktieLogger.ROOT_LOGGER.isTraceEnabled()) {
            BlacktieLogger.ROOT_LOGGER.trace("BlacktieSubsystemAdd.performBoottime");
        }

        BlacktieLogger.ROOT_LOGGER.info("Starting Blacktie Susbsystem");

        final String connectionFactoryName = model.get(Attribute.CONNECTION_FACTORYNAME.getLocalName()).asString();
        final String socketBindingName = model.get(Attribute.SOCKET_BINDING.getLocalName()).asString();

        final StompConnectService stompConnectService = new StompConnectService(connectionFactoryName);
        final ServiceBuilder<StompConnectService> stompConnectServiceBuilder = context
                .getServiceTarget()
                .addService(BlacktieSubsystemExtension.STOMPCONNECT, stompConnectService)
                .addListener(verificationHandler)
                .addDependency(SocketBinding.JBOSS_BINDING_NAME.append(socketBindingName), SocketBinding.class,
                        stompConnectService.getInjectedSocketBinding());

        stompConnectServiceBuilder.setInitialMode(ServiceController.Mode.ACTIVE);

        final ServiceController<StompConnectService> stompConnectServiceController = stompConnectServiceBuilder.install();

        if (newControllers != null) {
            newControllers.add(stompConnectServiceController);
        }
    }
}
