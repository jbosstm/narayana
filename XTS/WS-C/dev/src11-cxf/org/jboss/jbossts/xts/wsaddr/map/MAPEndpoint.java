package org.jboss.jbossts.xts.wsaddr.map;

import org.w3c.dom.Element;

import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.addressing.ReferenceParametersType;

/**
 * MAPEndpoint is a wrapper class which works with class MAP. This is the JBossWS CXF implementation.
 */
public class MAPEndpoint
{
    MAPEndpoint(EndpointReferenceType implementation)
    {
        this.implementation = implementation;
    }

    public String getAddress()
    {
        return implementation.getAddress().getValue();
    }

    public void addReferenceParameter(Element element)
    {
        ReferenceParametersType refParams = implementation.getReferenceParameters();
        if (refParams == null) {
            refParams = new ReferenceParametersType();
            implementation.setReferenceParameters(refParams);
        }
        refParams.getAny().add(element);
    }

    EndpointReferenceType getImplementation()
    {
        return implementation;
    }

    private EndpointReferenceType implementation;
}
