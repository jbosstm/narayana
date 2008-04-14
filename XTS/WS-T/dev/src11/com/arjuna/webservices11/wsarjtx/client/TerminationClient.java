package com.arjuna.webservices11.wsarjtx.client;

import com.arjuna.schemas.ws._2005._10.wsarjtx.*;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adinn
 * Date: Nov 16, 2007
 * Time: 6:02:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class TerminationClient
{
    // we need a service per thread

    private static ThreadLocal<TerminationParticipantService> terminationParticipantService = new ThreadLocal<TerminationParticipantService>();

    private static ThreadLocal<TerminationCoordinatorService> terminationCoordinatorService = new ThreadLocal<TerminationCoordinatorService>();

    private static TerminationParticipantService getTerminationParticipantService()
    {
        if (terminationParticipantService.get() == null) {
            terminationParticipantService.set(new TerminationParticipantService());
        }
        return terminationParticipantService.get();
    }

    private static TerminationCoordinatorService getTerminationCoordinatorService()
    {
        if (terminationCoordinatorService.get() == null) {
            terminationCoordinatorService.set(new TerminationCoordinatorService());
        }
        return terminationCoordinatorService.get();
    }

    public static TerminationParticipantPortType getTerminationParticipantPort(AddressingProperties addressingProperties)
    {
        TerminationParticipantService service = getTerminationParticipantService();
        TerminationParticipantPortType port = service.getPort(TerminationParticipantPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
	    requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addressingProperties);
	    // jbossws should do this for us . . .
	    requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addressingProperties);

        return port;
    }

    public static TerminationCoordinatorPortType getRegistrationPort(AddressingProperties addressingProperties)
    {
        TerminationCoordinatorService service = getTerminationCoordinatorService();
        TerminationCoordinatorPortType port = service.getPort(TerminationCoordinatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
	    requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addressingProperties);
	    // jbossws should do this for us . . .
	    requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addressingProperties);

        return port;
    }
}
