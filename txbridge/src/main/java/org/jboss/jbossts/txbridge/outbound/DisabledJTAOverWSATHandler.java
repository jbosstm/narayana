/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.txbridge.outbound;

import jakarta.xml.ws.handler.MessageContext;

/**
 * This handler is used when <code>default-context-propagation</code> is disabled. It handles messages only if
 * JTAOverWSATFeature is enabled.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 * @param <C>
 */
public class DisabledJTAOverWSATHandler<C extends MessageContext> extends AbstractJTAOverWSATHandler<C> {

    /**
     * @see org.jboss.jbossts.txbridge.outbound.AbstractJTAOverWSATHandler#isContextPropagationEnabled(MessageContext)
     *
     * @return true if JTAOverWSATFeature is present and is enabled, false otherwise.
     */
    @Override
    protected boolean isContextPropagationEnabled(C context) {
        return JTAOverWSATFeature.ENABLED_VALUE.equals(context.get(JTAOverWSATFeature.REQUEST_CONTEXT_KEY));
    }

}