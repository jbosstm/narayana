/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.context.soap;

import org.w3c.dom.Element;

import com.arjuna.mw.wsas.context.Context;

/**
 */

public interface SOAPContext extends Context
{
    /**
     * Serialise the SOAP context into a DOM node.
     * @param element The element to contain the serialisation.
     * @return the element added.
     */
    public Element serialiseToElement(final Element element);

    /**
     * Initialise the implementation using the parameter provided.
     */
    public void initialiseContext(Object obj);
}