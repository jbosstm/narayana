package com.arjuna.webservices11.wsaddr.map;

import javax.xml.ws.addressing.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * MAPBuilder is a helper class used to create objects used with class MAP. This is the JBossWS Native
 * implementation
 */
public class MAPBuilder
{
    public static MAPBuilder getBuilder() {
        return theBuilder;
    }

    public MAP newMap() {
        AddressingProperties implementation = addressingBuilder.newAddressingProperties();
        return new MAP(implementation);
    }

    /**
     * retrieve the inbound server message address properties attached to a message context
     * @param ctx the server message context
     * @return
     */
    public MAP inboundMap(MessageContext ctx)
    {
        AddressingProperties implementation = (AddressingProperties)ctx.get(MAPConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        return newMap(implementation);
    }

    /**
     * retrieve the outbound client message address properties attached to a message request map
     * @param ctx the client request properties map
     * @return
     */
    public MAP outboundMap(Map<String, Object> ctx) {
        AddressingProperties implementation = (AddressingProperties)ctx.get(MAPConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
        return newMap(implementation);
    }

    // n.b. this is package public only!
    MAP newMap(AddressingProperties implementation) {
        return new MAP(implementation);
    }

    public MAPConstants newConstants() {
        AddressingConstants implementation = addressingBuilder.newAddressingConstants();
        return new MAPConstants(implementation);
    }

    public MAPEndpoint newEndpoint(String address) {
        try {
            URI uri = new URI(address);
            EndpointReference implementation = addressingBuilder.newEndpointReference(uri);
            return new MAPEndpoint(implementation);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public MAPRelatesTo newRelatesTo(String id, QName type) {
        return new MAPRelatesTo(id, type);
    }

    private MAPBuilder()
    {
        AddressingBuilder implementation = AddressingBuilder.getAddressingBuilder();
        this.addressingBuilder = implementation;
    }

    private AddressingBuilder addressingBuilder;

    private static MAPBuilder theBuilder = new MAPBuilder();
}
