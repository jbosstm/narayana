package com.arjuna.webservices11.wsaddr.map;

import org.w3c.dom.Element;

import javax.xml.ws.addressing.*;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.Map;

/**
 * Message Addressing Properties is a wrapper class for the stack-specific addressing properties
 * classes implemented by JBossWS Native and CXF. It is used to localize dependence upon the WS
 * stack. This is the JBossWS Native specific implementation
 */

public class MAP
{
    /**
     * JBossWS Native specific constructor
     * @param implementation
     */
    MAP(AddressingProperties implementation)
    {
        this.implementation = implementation;
    }

    public String getTo() {
        AttributedURI to = implementation.getTo();
        return (to != null ? to.getURI().toString() : null);
    }

    public MAPEndpoint getFrom() {
        EndpointReference from = implementation.getFrom();
        return (from != null ?  new MAPEndpoint(from) : null);
    }

    public String getMessageID() {
        AttributedURI messageId = implementation.getMessageID();
        return (messageId != null ? messageId.getURI().toString() : null);
    }

    public String getAction() {
        AttributedURI action = implementation.getAction();
        return (action != null ? action.getURI().toString() : null);
    }

    public MAPEndpoint getFaultTo() {
        EndpointReference faultTo = implementation.getFaultTo();
        return (faultTo != null ?  new MAPEndpoint(faultTo) : null);
    }

    public MAPEndpoint getReplyTo()
    {
        EndpointReference replyTo = implementation.getReplyTo();
        return (replyTo != null ?  new MAPEndpoint(replyTo) : null);
    }

    public MAPRelatesTo getRelatesTo()
    {
        MAPBuilder builder = MAPBuilder.getBuilder();
        Relationship[] relationship = implementation.getRelatesTo();
        if (relationship != null) {
            Relationship relatesTo = relationship[0];
            return builder.newRelatesTo(relatesTo.getID().toString(), relatesTo.getType());
        } else {
            return null;
        }
    }

    public void setTo(String address)
    {
        if (address != null) {
            try {
                AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
                AttributedURI uri = builder.newURI(address);
                implementation.setTo(uri);
            } catch (URISyntaxException e) {
                // should not happen
            }
        } else {
            implementation.setTo(null);
        }
    }

    public void setFrom(MAPEndpoint epref)
    {
        if (epref != null) {
            implementation.setFrom(epref.getImplementation());
        } else {
            implementation.setFrom(null);
        }
    }

    public void setMessageID(String messageID)
    {
        if (messageID != null) {
            try {
                AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
                AttributedURI uri = builder.newURI(messageID);
                implementation.setMessageID(uri);
            } catch (URISyntaxException e) {
                // should not happen
            }
        } else {
            implementation.setMessageID(null);
        }
    }

    public void setAction(String action)
    {
        if (action != null) {
        try {
            AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
            AttributedURI uri = builder.newURI(action);
            implementation.setAction(uri);
        } catch (URISyntaxException e) {
            // should not happen
        }
        } else {
            implementation.setAction(null);
        }
    }

    public void setReplyTo(MAPEndpoint epref)
    {
        if (epref != null) {
            implementation.setReplyTo(epref.getImplementation());
        } else {
            implementation.setReplyTo(null);
        }
    }

    public void setFaultTo(MAPEndpoint epref)
    {
        if (epref != null) {
            implementation.setFaultTo(epref.getImplementation());
        } else {
            implementation.setFaultTo(null);
        }
    }

    public void setRelatesTo(MAPRelatesTo relatesTo)
    {
        if (relatesTo != null) {
            try {
                AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
                Relationship[] relationships = new Relationship[1];
                String relatesToId = relatesTo.getRelatesTo();
                URI uri = new URI(relatesToId);
                Relationship relationship = builder.newRelationship(uri);
                relationship.setType(relatesTo.getType());
                relationships[0] = relationship;
                implementation.setRelatesTo(relationships);
            } catch (URISyntaxException e) {
                // should not happen
            }
        } else {
            implementation.setRelatesTo(null);
        }
    }

    public void addReferenceParameter(Element refParam)
    {
        implementation.getReferenceParameters().addElement(refParam);
        // cxf needs to do
        // implementtaion.getToEndpointReference().getReferenceParameters().getAny().add(refParam);
    }

    public void initializeAsDestination(MAPEndpoint epref)
    {
        implementation.initializeAsDestination(epref.getImplementation());
    }


    public void installOutboundMap(Map<String, Object> requestContext, MAP map)
    {
        AddressingProperties addressingProperties = map.implementation;

        requestContext.put(MAPConstants.CLIENT_ADDRESSING_PROPERTIES, addressingProperties);
	    requestContext.put(MAPConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addressingProperties);
    }
    /**
     * the wrapped instance which this class delegates to
     */
    private AddressingProperties implementation;
}
