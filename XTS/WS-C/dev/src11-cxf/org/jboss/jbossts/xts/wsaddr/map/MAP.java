package org.jboss.jbossts.xts.wsaddr.map;

import org.w3c.dom.Element;

import org.apache.cxf.ws.addressing.*;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Message Addressing Properties is a wrapper class for the stack-specific addressing properties
 * classes implemented by JBossWS Native and CXF. It is used to localize dependence upon the WS
 * stack. This is the JBossWS CXF specific implementation
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
        AttributedURIType to = implementation.getTo();
        return (to != null ? to.getValue() : null);
    }

    public MAPEndpoint getFrom() {
        EndpointReferenceType from = implementation.getFrom();
        return (from != null ?  new MAPEndpoint(from) : null);
    }

    public String getMessageID() {
        AttributedURIType messageId = implementation.getMessageID();
        return (messageId != null ? messageId.getValue() : null);
    }

    public String getAction() {
        AttributedURIType action = implementation.getAction();
        return (action != null ? action.getValue() : null);
    }

    public MAPEndpoint getFaultTo() {
        EndpointReferenceType faultTo = implementation.getFaultTo();
        return (faultTo != null ?  new MAPEndpoint(faultTo) : null);
    }

    public MAPEndpoint getReplyTo()
    {
        EndpointReferenceType replyTo = implementation.getReplyTo();
        return (replyTo != null ?  new MAPEndpoint(replyTo) : null);
    }

    public MAPRelatesTo getRelatesTo()
    {
        MAPBuilder builder = MAPBuilder.getBuilder();
        RelatesToType relatesTo = implementation.getRelatesTo();
        if (relatesTo != null) {
            String type = relatesTo.getRelationshipType();
            int index = type.indexOf("}");
            String ns = type.substring(1, index+1);
            String name = type.substring(index+1);
            return builder.newRelatesTo(relatesTo.getValue(), new QName(ns, name));
        } else {
            return null;
        }
    }

    public void setTo(String address)
    {
        if (address != null) {
            EndpointReferenceType epref = new EndpointReferenceType();
            AttributedURIType uri = new AttributedURIType();
            uri.setValue(address);
            epref.setAddress(uri);
            implementation.setTo(epref);
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
            AttributedURIType uri = new AttributedURIType();
            uri.setValue(messageID);
            implementation.setMessageID(uri);
        } else {
            implementation.setMessageID(null);
        }
    }

    public void setAction(String action)
    {
        if (action != null) {
            AttributedURIType uri = new AttributedURIType();
            uri.setValue(action);
            implementation.setAction(uri);
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
            RelatesToType relatesToImpl = new RelatesToType();
            relatesToImpl.setValue(relatesTo.getRelatesTo());
            relatesToImpl.setRelationshipType(relatesTo.getType().toString());
            implementation.setRelatesTo(relatesToImpl);
        } else {
            implementation.setRelatesTo(null);
        }
    }

    public void addReferenceParameter(Element refParam)
    {
        implementation.getToEndpointReference().getReferenceParameters().getAny().add(refParam);
    }

    public void initializeAsDestination(MAPEndpoint epref)
    {
        if (epref == null)
         throw new IllegalArgumentException("Invalid null endpoint reference");

        implementation.setTo(epref.getImplementation());
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
