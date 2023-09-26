/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.txbridge.outbound;

import jakarta.xml.ws.handler.MessageContext;

/**
 * This handler is used when <code>default-context-propagation</code> is enabled. It handles every message unless
 * JTAOverWSATFeature is disabled.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 * @param <C>
 */
public class EnabledJTAOverWSATHandler<C extends MessageContext> extends AbstractJTAOverWSATHandler<C> {

    /**
     * @see org.jboss.jbossts.txbridge.outbound.AbstractJTAOverWSATHandler#isContextPropagationEnabled(MessageContext)
     *
     * @return false if JTAOverWSATFeature is present and is disabled, true otherwise.
     */
    @Override
    protected boolean isContextPropagationEnabled(C context) {
        return !JTAOverWSATFeature.DISABLED_VALUE.equals(context.get(JTAOverWSATFeature.REQUEST_CONTEXT_KEY));
    }

}