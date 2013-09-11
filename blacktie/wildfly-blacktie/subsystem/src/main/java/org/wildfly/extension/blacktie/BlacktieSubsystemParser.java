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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;

import java.util.EnumSet;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;
import org.wildfly.extension.blacktie.configuration.Element;
import org.wildfly.extension.blacktie.configuration.Attribute;
import org.wildfly.extension.blacktie.logging.BlacktieLogger;

/**
 * @author <a href="mailto:zfeng@redhat.com">Amos Feng</a>
 *
 */
final class BlacktieSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>,
XMLElementWriter<SubsystemMarshallingContext> {

    @Override
    public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
        if (BlacktieLogger.ROOT_LOGGER.isTraceEnabled()) {
            BlacktieLogger.ROOT_LOGGER.trace("BlacktieSubsystemParser.writeContent");
        }
        context.startSubsystemElement(BlacktieSubsystemExtension.NAMESPACE, false);

        ModelNode node = context.getModelNode();

        writer.writeStartElement(Element.STOMPCONNECT.getLocalName());
        BlacktieSubsystemDefinition.CONNECTION_FACTORYNAME.marshallAsAttribute(node, writer);
        BlacktieSubsystemDefinition.SOCKET_BINDING.marshallAsAttribute(node, writer);
        writer.writeEndElement();

        writer.writeEndElement();
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        if (BlacktieLogger.ROOT_LOGGER.isTraceEnabled()) {
            BlacktieLogger.ROOT_LOGGER.trace("BlacktieSubsystemParser.readElement");
        }
        // no attributes
        if (reader.getAttributeCount() > 0) {
            throw ParseUtils.unexpectedAttribute(reader, 0);
        }
        final ModelNode subsystem = Util.getEmptyOperation(ADD, PathAddress.pathAddress(BlacktieSubsystemExtension.SUBSYSTEM_PATH).toModelNode());
        list.add(subsystem);

        // elements
        final EnumSet<Element> encountered = EnumSet.noneOf(Element.class);

        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            final Element element = Element.forName(reader.getLocalName());

            if (!encountered.add(element)) {
                throw ParseUtils.unexpectedElement(reader);
            }

            if (element.equals(Element.STOMPCONNECT)) {
                parseStompConnectElement(reader, subsystem);
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
        }

    }

    private void parseStompConnectElement(XMLExtendedStreamReader reader, ModelNode subsystem) throws XMLStreamException {
        final int count = reader.getAttributeCount();

        for (int i = 0; i < count; i++) {
            ParseUtils.requireNoNamespaceAttribute(reader, i);

            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));

            switch (attribute) {
                case CONNECTION_FACTORYNAME:
                    BlacktieSubsystemDefinition.CONNECTION_FACTORYNAME.parseAndSetParameter(value, subsystem, reader);
                    break;
                case SOCKET_BINDING:
                    BlacktieSubsystemDefinition.SOCKET_BINDING.parseAndSetParameter(value, subsystem, reader);
                    break;
                default:
                    throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }
        // Handle elements
        ParseUtils.requireNoContent(reader);
    }
}
