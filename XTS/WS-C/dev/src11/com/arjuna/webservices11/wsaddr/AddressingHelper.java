package com.arjuna.webservices11.wsaddr;

import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsaddr.map.*;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.namespace.QName;
import java.util.Map;

/**
 * The complete addressing context.
 * @author kevin
 */
public class AddressingHelper
{
    protected AddressingHelper()
    {
    }

    public static MAP createOneWayResponseContext(final MAP map, final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        MAPBuilder builder = MAPBuilder.getBuilder();
        final MAP responseMap = builder.newMap();
        // ok just calling initializeAsDestination directly fails when the FaultTo/ReplyTo/From
        // contains reference parameter elements. these get installed into the target element extensions
        // list for insertion into the outgoing message. however, the insertion fails.
        // JBossWS decides they can be inserted as is without copying because they are SOAP
        // elements but this ignores the fact that they have a DOM node attached. when the
        // appendElement is called it barfs because the target and source belong to different
        // documents. we patch this by copying the FaultTo or ReplyTo or from here if need be.
        MAPEndpoint epref = map.getReplyTo();
        if (isNoneAddress(epref)) {
            epref = map.getFrom();
        }
        responseMap.initializeAsDestination(epref);
        responseMap.setMessageID(messageID) ;

        if (map.getMessageID() != null)
        {
            MAPRelatesTo relatesTo = builder.newRelatesTo(map.getMessageID(), REPLY_RELATIONSHIP_TYPE_QNAME);
            responseMap.setRelatesTo(relatesTo);
        }

        return responseMap ;
    }

    /**
     * Create an addressing context that represents an inline reply to the specified addressing context.
     * @param map The addressing context being replied to.
     * @param messageID The message id of the new message.
     * @return The reply addressing context.
     */
    public static MAP createResponseContext(final MAP map, final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        MAPBuilder builder = MAPBuilder.getBuilder();
        final MAP responseMap = builder.newMap();
        MAPEndpoint epref = map.getReplyTo();
        if (isNoneAddress(epref)) {
            epref = map.getFrom();
        }
        responseMap.initializeAsDestination(epref);
        responseMap.setMessageID(messageID) ;

        if (map.getMessageID() != null)
        {
            MAPRelatesTo relatesTo = builder.newRelatesTo(map.getMessageID(), REPLY_RELATIONSHIP_TYPE_QNAME);
            responseMap.setRelatesTo(relatesTo);
        }

        return responseMap ;
    }

    /**
     * Create an addressing context that represents a fault to the specified addressing context.
     * @param map The addressing context being replied to.
     * @param messageID The message id of the new message.
     * @return The fault addressing context.
     *
     * N.B. Still need to do From, Action, ReplyTo, FaultTo if needed.
     */
    public static MAP createFaultContext(final MAP map, final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        MAPBuilder builder = MAPBuilder.getBuilder();
        final MAP faultMap = builder.newMap();
        // ok just calling initializeAsDestination directly fails when the FaultTo/ReplyTo/From
        // contains reference parameter elements. these get installed into the target element extensions
        // list for insertion into the outgoing message. however, the insertion fails.
        // JBossWS decides they can be inserted as is without copying because they are SOAP
        // elements but this ignores the fact that they have a DOM node attached. when the
        // appendElement is called it barfs because the target and source belong to different
        // documents. we patch this by copying the FaultTo or ReplyTo or from here if need be.
        MAPEndpoint epref= map.getFaultTo();
        if (isNoneAddress(epref)) {
            epref = map.getReplyTo();
            if (isNoneAddress(epref)) {
                epref = map.getFrom();
            }
        }
        faultMap.initializeAsDestination(epref);
        faultMap.setMessageID(messageID) ;

        if (map.getMessageID() != null)
        {
            MAPRelatesTo relatesTo = builder.newRelatesTo(map.getMessageID(), REPLY_RELATIONSHIP_TYPE_QNAME);
            faultMap.setRelatesTo(relatesTo);
        }

        return faultMap ;
    }

    /**
     * Create an addressing context that represents a request to the specified address.
     * @param address TheendpointReference target address.
     * @param messageID The message id of the new message.
     * @return The addressing context.
     *
     * N.B. Still need to do From, Action, ReplyTo, FaultTo if needed.
     */
    public static MAP createRequestContext(final String address, final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        MAPBuilder builder = MAPBuilder.getBuilder();
        final MAP requestMap = builder.newMap();
        requestMap.setTo(address);
        if (messageID != null) {
            requestMap.setMessageID(messageID);
        } else {
            // client does not care about id but we have to set some id or WSA will complain

            final String dummyID = MessageId.getMessageId();

            requestMap.setMessageID(dummyID);
        }
        return requestMap;
    }

    /**
     * Create an addressing context that represents a request to the specified address.
     * @param address TheendpointReference target address.
     * @param messageID The message id of the new message.
     * @param action The action of the new message.
     * @return The addressing context.
     *
     * N.B. Still need to do From, Action, ReplyTo, FaultTo if needed.
     */
    public static MAP createRequestContext(final String address, final String action, final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        MAPBuilder builder = MAPBuilder.getBuilder();
        final MAP requestMap = builder.newMap();
        requestMap.setTo(address);
        if (messageID != null) {
            requestMap.setMessageID(messageID);
        } else {
            // client does not care about id but we have to set some id or WSA will complain

            final String dummyID = MessageId.getMessageId();

            requestMap.setMessageID(dummyID);
        }
        requestMap.setAction(action);

        return requestMap;
    }

    /**
     * Create an addressing context that represents a notification to the specified context.
     * @param map The addressing properties used to derive the notification addressing context.
     * @param messageID The message id of the new message.
     * @return The notification addressing context.
     *
     * N.B. Still need to do From, Action, ReplyTo, FaultTo if needed.
     */
    public static MAP createRequestContext(final MAP map, final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        MAPBuilder builder = MAPBuilder.getBuilder();
        final MAP requestMap = builder.newMap();
        // ok just calling initializeAsDestination directly fails when the FaultTo/ReplyTo/From
        // contains reference parameter elements. these get installed into the target element extensions
        // list for insertion into the outgoing message. however, the insertion fails.
        // JBossWS decides they can be inserted as is without copying because they are SOAP
        // elements but this ignores the fact that they have a DOM node attached. when the
        // appendElement is called it barfs because the target and source belong to different
        // documents. we patch this by copying the FaultTo or ReplyTo or from here if need be.
        MAPEndpoint epref = map.getReplyTo();
        if (isNoneAddress(epref)) {
            epref = map.getFrom();
        }
        requestMap.initializeAsDestination(epref);
        if (messageID != null)
        {
            requestMap.setMessageID (messageID);
        }

        if (map.getMessageID() != null)
        {
            MAPRelatesTo relatesTo = builder.newRelatesTo(map.getMessageID(), REPLY_RELATIONSHIP_TYPE_QNAME);
            requestMap.setRelatesTo(relatesTo);
        }

        return requestMap;
    }

    /**
     * Create an addressing context specifying only the message id for a notification.
     * @param messageID The message id of the new message.
     * @return The notification addressing context.
     */
    public static MAP createNotificationContext(final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        MAPBuilder builder = MAPBuilder.getBuilder();
        final MAP requestMap = builder.newMap();
        requestMap.setMessageID (messageID);
        requestMap.setAction("");
        String noneURI = builder.newConstants().getNoneURI();
        requestMap.setReplyTo(builder.newEndpoint(noneURI));

        return requestMap;
    }


    public static void installActionMessageID(MAP map, final String action, final String messageID)
    {
        // requestProperties has been derived from an endpoint so To and ReferenceParameters will be set. we
        // need to install the action and messageID

        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        MAPBuilder builder = MAPBuilder.getBuilder();
        map.setMessageID(messageID);
        map.setAction(action);
    }


    public static void installCallerProperties(MAP map, MAP requestMap)
    {
        // requestMap has been derived from an endpoint so To and ReferenceParameters will be set. we
        // need to install alll the other fields supplied in map

        String uri;
        MAPRelatesTo relatesTo;
        MAPEndpoint epr;
        uri = map.getAction();
        if (uri != null) {
            requestMap.setAction(uri);
        }
        uri = map.getMessageID();
        if (uri != null) {
            requestMap.setMessageID(uri);
        }
        epr = map.getFrom();
        if (epr != null) {
            requestMap.setFrom(epr);
        }
        epr = map.getFaultTo();
        if (epr != null) {
            requestMap.setFaultTo(epr);
        }
        epr = map.getReplyTo();
        if (epr != null) {
            requestMap.setReplyTo(epr);
        }
        relatesTo = map.getRelatesTo();
        if (relatesTo != null) {
            requestMap.setRelatesTo(relatesTo);
        }
    }
    
    public static void installFaultTo(MAP map, MAPEndpoint epReference, InstanceIdentifier identifier)
    {
        MAPBuilder builder = MAPBuilder.getBuilder();
        MAPEndpoint from = builder.newEndpoint(epReference.getAddress());
        InstanceIdentifier.setEndpointInstanceIdentifier(from, identifier);
        map.setFaultTo(from);
    }

    public static void installFromFaultTo(MAP map, MAPEndpoint epReference, InstanceIdentifier identifier)
    {
        MAPBuilder builder = MAPBuilder.getBuilder();
        MAPEndpoint from = builder.newEndpoint(epReference.getAddress());
        InstanceIdentifier.setEndpointInstanceIdentifier(from, identifier);
        map.setFrom(from);
        MAPEndpoint faultTo = builder.newEndpoint(epReference.getAddress());
        InstanceIdentifier.setEndpointInstanceIdentifier(faultTo, identifier);
        map.setFaultTo(faultTo);
    }

    public static boolean isNoneReplyTo(MAP map)
    {
        return isNoneAddress(map.getReplyTo());
    }

    public static boolean isNoneAddress(MAPEndpoint epref)
    {
        if (epref != null) {
            String noneAddress = getNoneAddress().getAddress();
            String eprefAddress = epref.getAddress();

            return noneAddress.equals(eprefAddress);
        } else {
            return true;
        }
    }

    public static void installNoneReplyTo(MAP map)
    {
        map.setReplyTo(getNoneAddress());
    }

    /**
     * JBossWS Native version to configure request context with MAP, to and action
     * @param requestContext
     * @param map
     * @param to
     * @param action
     */
    public static void configureRequestContext(Map<String, Object> requestContext, MAP map, String to, String action)
    {
        configureRequestContext(requestContext, map);
        configureRequestContext(requestContext, to, action);
    }

    /**
     * JBossWS Native version to configure request context with MAP
     * @param requestContext
     */
    public static void configureRequestContext(Map<String, Object> requestContext, MAP map)
    {
        map.installOutboundMap(requestContext, map);
    }

    /**
     * JBossWS Native version to configure request context with to and action
     * @param requestContext
     * @param to
     * @param action
     */
    public static void configureRequestContext(Map<String, Object> requestContext, String to, String action) {
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, to);
        if (action != null) {
            // need to set soap action header based upon what the client asks for
            requestContext.put(BindingProvider.SOAPACTION_URI_PROPERTY, action);
        }
    }
    
    /**
     * retrieve the inbound server message address properties attached to a message context
     * @param ctx the server message context
     * @return
     */
    public static MAP inboundMap(MessageContext ctx)
    {
        return MAPBuilder.getBuilder().inboundMap(ctx);
    }

    /**
     * retrieve the outbound client message address properties attached to a message request map
     * @param ctx the client request properties map
     * @return
     */
    public static MAP outboundMap(Map<String, Object>  ctx) {
        return MAPBuilder.getBuilder().outboundMap(ctx);
    }

    private static MAPEndpoint noneAddress = null;

    private static synchronized MAPEndpoint getNoneAddress()
    {
        if (noneAddress == null) {
            MAPBuilder builder = MAPBuilder.getBuilder();
            MAPConstants mapConstants = builder.newConstants();
            String noneURI = mapConstants.getNoneURI();
            noneAddress = builder.newEndpoint(noneURI);
        }

        return noneAddress;
    }

    private static String REPLY_RELATIONSHIP_TYPE_NS = "org.jboss.jbossts.xts";
    private static String REPLY_RELATIONSHIP_TYPE_STRING = "reply";
    private static QName REPLY_RELATIONSHIP_TYPE_QNAME = new QName(REPLY_RELATIONSHIP_TYPE_NS, REPLY_RELATIONSHIP_TYPE_STRING);
}
