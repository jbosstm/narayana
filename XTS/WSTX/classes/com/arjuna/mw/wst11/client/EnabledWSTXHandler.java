/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.mw.wst11.client;

import jakarta.xml.ws.handler.soap.SOAPMessageContext;

/**
 * This handler is used when <code>default-context-propagation</code> is enabled. It handles every message unless WSTXFeature is
 * disabled.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class EnabledWSTXHandler extends AbstractWSTXHandler {

    /**
     * @see com.arjuna.mw.wst11.client.AbstractWSTXHandler#isContextPropagationEnabled(SOAPMessageContext)
     *
     * @return false if WSTXFeature is present and is disabled, true otherwise.
     */
    @Override
    protected boolean isContextPropagationEnabled(SOAPMessageContext context) {
        return !WSTXFeature.DISABLED_VALUE.equals(context.get(WSTXFeature.REQUEST_CONTEXT_KEY));
    }

}