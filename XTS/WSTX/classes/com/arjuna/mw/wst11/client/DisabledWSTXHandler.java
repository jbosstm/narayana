/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.mw.wst11.client;

import jakarta.xml.ws.handler.soap.SOAPMessageContext;

/**
 * This handler is used when <code>default-context-propagation</code> is disabled. It handles messages only if WSTXFeature is
 * enabled.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class DisabledWSTXHandler extends AbstractWSTXHandler {

    /**
     * @see com.arjuna.mw.wst11.client.AbstractWSTXHandler#isContextPropagationEnabled(SOAPMessageContext)
     *
     * @return true if WSTXFeature is present and is enabled, false otherwise.
     */
    @Override
    protected boolean isContextPropagationEnabled(SOAPMessageContext context) {
        return WSTXFeature.ENABLED_VALUE.equals(context.get(WSTXFeature.REQUEST_CONTEXT_KEY));
    }

}