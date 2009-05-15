package org.jboss.jbossts.xts.wsaddr.map;

import org.w3c.dom.Element;

import javax.xml.ws.addressing.EndpointReference;

/**
 * MAPEndpoint is a wrapper class which works with class MAP. This is the JBossWS Native implementation.
 */
public class MAPEndpoint
{
    MAPEndpoint(EndpointReference implementation)
    {
        this.implementation = implementation;
    }

    public String getAddress()
    {
        return implementation.getAddress().getURI().toString();
    }

    public void addReferenceParameter(Element element)
    {
        implementation.getReferenceParameters().addElement(element);
    }

    EndpointReference getImplementation()
    {
        return implementation;
    }

    private EndpointReference implementation;
}
