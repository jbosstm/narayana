package com.arjuna.webservices11.wsaddr;

import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarj.ArjunaConstants;

import javax.xml.ws.addressing.*;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.List;
import java.util.Iterator;

import org.w3c.dom.Element;

/**
 * The complete addressing context.
 * @author kevin
 */
public class AddressingHelper
{
    protected AddressingHelper()
    {
    }

    public static AddressingProperties createOneWayResponseContext(final AddressingProperties addressingProperties, final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        final AddressingProperties responseProperties = builder.newAddressingProperties();
        // ok just calling initializeAsDestination directly fails when the FaultTo/ReplyTo/From
        // contains reference parameter elements. these get installed into the target element extensions
        // list for insertion into the outgoing message. however, the insertion fails.
        // JBossWS decides they can be inserted as is without copying because they are SOAP
        // elements but this ignores the fact that they have a DOM node attached. when the
        // appendElement is called it barfs because the target and source belong to different
        // documents. we patch this by copying the FaultTo or ReplyTo or from here if need be.
        EndpointReference epref = addressingProperties.getReplyTo();
        if (isNoneAddress(epref)) {
            epref = addressingProperties.getFrom();
        }
        if (!isNoneAddress(epref)) {
            epref = patchEndpointReference(epref);
        }
        responseProperties.initializeAsDestination(epref);
        responseProperties.setMessageID(makeURI(builder, messageID)) ;

        if (addressingProperties.getMessageID() != null)
        {
           Relationship rel = builder.newRelationship(addressingProperties.getMessageID().getURI());
           responseProperties.setRelatesTo(new Relationship[] { rel });
        }

        return responseProperties ;
    }

    /**
     * Create an addressing context that represents an inline reply to the specified addressing context.
     * @param addressingProperties The addressing context being replied to.
     * @param messageID The message id of the new message.
     * @return The reply addressing context.
     */
    public static AddressingProperties createResponseContext(final AddressingProperties addressingProperties, final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        final AddressingProperties responseProperties = builder.newAddressingProperties();
        // ok just calling initializeAsDestination directly fails when the FaultTo/ReplyTo/From
        // contains reference parameter elements. these get installed into the target element extensions
        // list for insertion into the outgoing message. however, the insertion fails.
        // JBossWS decides they can be inserted as is without copying because they are SOAP
        // elements but this ignores the fact that they have a DOM node attached. when the
        // appendElement is called it barfs because the target and source belong to different
        // documents. we patch this by copying the FaultTo or ReplyTo or from here if need be.
        EndpointReference epref = addressingProperties.getReplyTo();
        if (isNoneAddress(epref)) {
            epref = addressingProperties.getFrom();
        }
        if (!isNoneAddress(epref)) {
            epref = patchEndpointReference(epref);
        }
        responseProperties.initializeAsDestination(epref);
        responseProperties.setMessageID(makeURI(builder, messageID)) ;

        if (addressingProperties.getMessageID() != null)
        {
           Relationship rel = builder.newRelationship(addressingProperties.getMessageID().getURI());
           responseProperties.setRelatesTo(new Relationship[] { rel });
        }

        return responseProperties ;
    }

    /**
     * Create an addressing context that represents a fault to the specified addressing context.
     * @param addressingProperties The addressing context being replied to.
     * @param messageID The message id of the new message.
     * @return The fault addressing context.
     *
     * N.B. Still need to do From, Action, ReplyTo, FaultTo if needed.
     */
    public static AddressingProperties createFaultContext(final AddressingProperties addressingProperties, final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        final AddressingProperties faultProperties = builder.newAddressingProperties();
        // ok just calling initializeAsDestination directly fails when the FaultTo/ReplyTo/From
        // contains reference parameter elements. these get installed into the target element extensions
        // list for insertion into the outgoing message. however, the insertion fails.
        // JBossWS decides they can be inserted as is without copying because they are SOAP
        // elements but this ignores the fact that they have a DOM node attached. when the
        // appendElement is called it barfs because the target and source belong to different
        // documents. we patch this by copying the FaultTo or ReplyTo or from here if need be.
        EndpointReference epref= addressingProperties.getFaultTo();
        if (isNoneAddress(epref)) {
            epref = addressingProperties.getReplyTo();
            if (isNoneAddress(epref)) {
                epref = addressingProperties.getFrom();
            }
        }
        if (!isNoneAddress(epref)) {
            epref = patchEndpointReference(epref);
            faultProperties.initializeAsDestination(epref);
        } else {
            faultProperties.initializeAsDestination(getNoneAddress());
        }
        faultProperties.setMessageID(makeURI(builder, messageID)) ;

        if (addressingProperties.getMessageID() != null)
        {
           Relationship rel = builder.newRelationship(addressingProperties.getMessageID().getURI());
           faultProperties.setRelatesTo(new Relationship[] { rel });
        }

        return faultProperties ;
    }

    // patch the case where we have an epref with a single reference parameter which is an
    // Arjuna TX InstanceIdentifier
    private static EndpointReference patchEndpointReference(EndpointReference epr)
    {
        ReferenceParameters refParams = epr.getReferenceParameters();
        List<Object> list = refParams.getElements();
        /*
        Object obj;
        if (list.size() == 1 && ((obj = list.get(0)) instanceof Element)) {
            Element element = (Element) obj;
            if (ArjunaConstants.WSARJ_NAMESPACE.equals(element.getNamespaceURI()) &&
                    ArjunaConstants.WSARJ_ELEMENT_INSTANCE_IDENTIFIER.equals(element.getLocalName())) {
                String identifier = element.getFirstChild().getNodeValue();
                // ok, cerate a copy of the epref with a new reference parameter element
                AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
                EndpointReference newEpr = builder.newEndpointReference(epr.getAddress().getURI());
                InstanceIdentifier.setEndpointInstanceIdentifier(newEpr, identifier);
                return newEpr;
            }
        }
        */
        if (list.size() > 0) {
            Iterator iterator = list.iterator();
            // ok, create a copy of the epref with a cloned reference parameter list
            AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
            EndpointReference newEpr = builder.newEndpointReference(epr.getAddress().getURI());
            ReferenceParameters newRefParams = newEpr.getReferenceParameters();
            while (iterator.hasNext()) {
                SOAPElement element = (SOAPElement) iterator.next();
                SOAPElement newElement = cloneElement(element);
                newRefParams.addElement(newElement);
            }
            return newEpr;
        } else {
            return epr;
        }
    }

    /**
     * Create an addressing context that represents a request to the specified address.
     * @param address TheendpointReference target address.
     * @param messageID The message id of the new message.
     * @return The addressing context.
     *
     * N.B. Still need to do From, Action, ReplyTo, FaultTo if needed.
     */
    public static AddressingProperties createRequestContext(final String address, final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        final AddressingProperties requestProperties = builder.newAddressingProperties();
        requestProperties.setTo(makeURI(builder, address));
        if (messageID != null) {
            requestProperties.setMessageID(makeURI(builder, messageID));
        } else {
            // client does not care about id but we have to set some id or WSA will complain

            final String dummyID = MessageId.getMessageId();

            requestProperties.setMessageID(makeURI(builder, dummyID));
        }
        return requestProperties;
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
    public static AddressingProperties createRequestContext(final String address, final String action, final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        final AddressingProperties requestProperties = builder.newAddressingProperties();
        requestProperties.setTo(makeURI(builder, address));
        if (messageID != null) {
            requestProperties.setMessageID(makeURI(builder, messageID));
        } else {
            // client does not care about id but we have to set some id or WSA will complain

            final String dummyID = MessageId.getMessageId();

            requestProperties.setMessageID(makeURI(builder, dummyID));
        }
        requestProperties.setAction(makeURI(builder, action));

        return requestProperties;
    }

    /**
     * Create an addressing context that represents a notification to the specified context.
     * @param addressingProperties The addressing properties used to derive the notification addressing context.
     * @param messageID The message id of the new message.
     * @return The notification addressing context.
     *
     * N.B. Still need to do From, Action, ReplyTo, FaultTo if needed.
     */
    public static AddressingProperties createRequestContext(final AddressingProperties addressingProperties, final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        final AddressingProperties requestProperties = builder.newAddressingProperties();
        // ok just calling initializeAsDestination directly fails when the FaultTo/ReplyTo/From
        // contains reference parameter elements. these get installed into the target element extensions
        // list for insertion into the outgoing message. however, the insertion fails.
        // JBossWS decides they can be inserted as is without copying because they are SOAP
        // elements but this ignores the fact that they have a DOM node attached. when the
        // appendElement is called it barfs because the target and source belong to different
        // documents. we patch this by copying the FaultTo or ReplyTo or from here if need be.
        EndpointReference epref = addressingProperties.getReplyTo();
        if (isNoneAddress(epref)) {
            epref = addressingProperties.getFrom();
        }
        if (!isNoneAddress(epref)) {
            epref = patchEndpointReference(epref);
        }
        requestProperties.initializeAsDestination(epref);
        if (messageID != null)
        {
            requestProperties.setMessageID (makeURI(builder, messageID));
        }

        if (addressingProperties.getMessageID() != null)
        {
           Relationship rel = builder.newRelationship(addressingProperties.getMessageID().getURI());
           requestProperties.setRelatesTo(new Relationship[] { rel });
        }

        return requestProperties;
    }

    /**
     * Create an addressing context specifying only the message id for a notification.
     * @param messageID The message id of the new message.
     * @return The notification addressing context.
     */
    public static AddressingProperties createNotificationContext(final String messageID)
    {
        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        final AddressingProperties requestProperties = builder.newAddressingProperties();
        requestProperties.setMessageID (makeURI(builder, messageID));
        requestProperties.setAction(makeURI(builder, ""));
        URI noneURI = URI.create(builder.newAddressingConstants().getNoneURI());
        requestProperties.setReplyTo(builder.newEndpointReference(noneURI));

        return requestProperties;
    }


    public static void installActionMessageID(AddressingProperties addressingProperties, final String action, final String messageID)
    {
        // requestProperties has been derived from an endpoint so To and ReferenceParameters will be set. we
        // need to install the action and messageID

        // create this each time so it uses the current thread classloader
        // this allows the builder class to be redefined via a property
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        addressingProperties.setMessageID(makeURI(builder, messageID));
        addressingProperties.setAction(makeURI(builder, action));
    }


    public static void installCallerProperties(AddressingProperties addressingProperties, AddressingProperties requestProperties)
    {
        // requestProperties has been derived from an endpoint so To and ReferenceParameters will be set. we
        // need to install alll the other fields supplied in addressingProperties

        AttributedURI uri;
        Relationship[] relatesTo;
        EndpointReference epr;
        uri = addressingProperties.getAction();
        if (uri != null) {
            requestProperties.setAction(uri);
        }
        uri = addressingProperties.getMessageID();
        if (uri != null) {
            requestProperties.setMessageID(uri);
        }
        epr = addressingProperties.getFrom();
        if (epr != null) {
            requestProperties.setFrom(epr);
        }
        epr = addressingProperties.getFaultTo();
        if (epr != null) {
            requestProperties.setFaultTo(epr);
        }
        epr = addressingProperties.getReplyTo();
        if (epr != null) {
            requestProperties.setReplyTo(epr);
        }
        relatesTo = addressingProperties.getRelatesTo();
        if (relatesTo != null) {
            requestProperties.setRelatesTo(relatesTo);
        }
    }
    
    public static void installFaultTo(AddressingProperties addressingProperties, EndpointReference epReference, InstanceIdentifier identifier)
    {
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        EndpointReference from = builder.newEndpointReference(epReference.getAddress().getURI());
        InstanceIdentifier.setEndpointInstanceIdentifier(from, identifier);
        addressingProperties.setFaultTo(from);
    }

    public static void installFromFaultTo(AddressingProperties addressingProperties, EndpointReference epReference, InstanceIdentifier identifier)
    {
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        EndpointReference from = builder.newEndpointReference(epReference.getAddress().getURI());
        InstanceIdentifier.setEndpointInstanceIdentifier(from, identifier);
        addressingProperties.setFrom(from);
        EndpointReference faultTo = builder.newEndpointReference(epReference.getAddress().getURI());
        InstanceIdentifier.setEndpointInstanceIdentifier(faultTo, identifier);
        addressingProperties.setFaultTo(faultTo);
    }

    private static EndpointReference noneAddress = null;

    private static synchronized EndpointReference getNoneAddress()
    {
        if (noneAddress == null) {
            AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
            AddressingConstants addressingConstants = builder.newAddressingConstants();
            try {
                URI noneURI = new URI(addressingConstants.getNoneURI());
                noneAddress = builder.newEndpointReference(noneURI);
            } catch (URISyntaxException e) {
                // will not happen
            }
        }

        return noneAddress;
    }

    public static boolean isNoneReplyTo(AddressingProperties addressingProperties)
    {
        return isNoneAddress(addressingProperties.getReplyTo());
    }

    public static boolean isNoneAddress(EndpointReference epref)
    {
        if (epref != null) {
            String noneAddress = getNoneAddress().getAddress().getURI().toString();
            String eprefAddress = epref.getAddress().getURI().toString();

            return noneAddress.equals(eprefAddress);
        } else {
            return true;
        }
    }
    public static void installNoneReplyTo(AddressingProperties addressingProperties)
    {
        addressingProperties.setReplyTo(getNoneAddress());
    }

    public static javax.xml.ws.addressing.AttributedURI makeURI(AddressingBuilder builder, String messageID)
    {
        try {
            return builder.newURI(messageID);
        } catch (URISyntaxException use) {
            return null;
        }
    }

    private static SOAPFactory factory = createSoapFactory();

    private static SOAPFactory createSoapFactory()
    {
        try {
            SOAPFactory factory = SOAPFactory.newInstance();
            return factory;
        } catch (SOAPException e) {
            // TODO log error here (should never happen)
        }
        return null;
    }

    /**
     * create a SOAPElement which is a deep copy of the supplied SOAPElement
     * @param element
     * @return
     */
    private static SOAPElement cloneElement(SOAPElement element)
    {
        try {
            Element copy = (Element)element.cloneNode(true);
            SOAPElement newElement = factory.createElement(copy);
            return newElement;
        } catch (SOAPException e) {
            return element;
        }
    }
}
