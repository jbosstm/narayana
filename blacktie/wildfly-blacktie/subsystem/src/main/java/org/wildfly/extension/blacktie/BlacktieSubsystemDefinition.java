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

import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;
import org.wildfly.extension.blacktie.configuration.Attribute;

/**
 * @author <a href="mailto:zfeng@redhat.com">Amos Feng</a>
 *
 */
public class BlacktieSubsystemDefinition extends SimpleResourceDefinition {
    public static final BlacktieSubsystemDefinition INSTANCE = new BlacktieSubsystemDefinition();

    protected static final SimpleAttributeDefinition SOCKET_BINDING =
            new SimpleAttributeDefinitionBuilder(Attribute.SOCKET_BINDING.getLocalName(), ModelType.STRING, true)
                    .setAllowExpression(false)
                    .setXmlName(Attribute.SOCKET_BINDING.getLocalName())
                    .setFlags(AttributeAccess.Flag.RESTART_JVM)
                    .build();

    protected static final SimpleAttributeDefinition CONNECTION_FACTORYNAME =
            new SimpleAttributeDefinitionBuilder(Attribute.CONNECTION_FACTORYNAME.getLocalName(), ModelType.STRING, true)
                    .setAllowExpression(false)
                    .setXmlName(Attribute.CONNECTION_FACTORYNAME.getLocalName())
                    .setFlags(AttributeAccess.Flag.RESTART_JVM)
                    .build();


    private BlacktieSubsystemDefinition() {
        super(BlacktieSubsystemExtension.SUBSYSTEM_PATH,
                BlacktieSubsystemExtension.getResourceDescriptionResolver(null),
                BlacktieSubsystemAdd.INSTANCE,
                BlacktieSubsystemRemove.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(CONNECTION_FACTORYNAME, null, new ReloadRequiredWriteAttributeHandler(CONNECTION_FACTORYNAME));
        resourceRegistration.registerReadWriteAttribute(SOCKET_BINDING, null, new ReloadRequiredWriteAttributeHandler(SOCKET_BINDING));

    }
}
