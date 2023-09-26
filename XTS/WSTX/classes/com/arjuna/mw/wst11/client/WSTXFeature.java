/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.mw.wst11.client;

import jakarta.xml.ws.BindingProvider;

import org.jboss.ws.api.configuration.AbstractClientFeature;

/**
 * Web service feature is used to enable or disable WS-AT/WS-BA context propagation for specific port.
 *
 * @author <a href="mailto:paul.robinson@redhat.com">Paul Robinson</a>
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public final class WSTXFeature extends AbstractClientFeature {

    /**
     * Key to store WSTXFeature's enabled/disabled value in SOAP header.
     */
    public static final String REQUEST_CONTEXT_KEY = "WSTXFeature";

    /**
     * Value to indicate that WSTXFeature is enabled.
     */
    public static final String ENABLED_VALUE = "true";

    /**
     * Value to indicate that WSTXFeature is disabled.
     */
    public static final String DISABLED_VALUE = "false";

    /**
     * Default constructor creates instance of enabled WSTXFeature.
     */
    public WSTXFeature() {
        this(true);
    }

    /**
     * Parametrised constructor creates either enabled or disabled WSTXFeature based on <code>enabled</code> parameter.
     *
     * @param enabled true to create enabled WSTXFeature, false to create disabled WSTXFeature.
     */
    public WSTXFeature(final boolean enabled) {
        super(WSTXFeature.class.getName());
        this.enabled = enabled;
    }

    /**
     * Sets <code>REQUEST_CONTEXT_KEY</code> value to <code>ENABLED_VALUE</code> if WSTXFeature is enabled.
     *
     * Sets <code>REQUEST_CONTEXT_KEY</code> value to <code>DISABLED_VALUE</code> if WSTXFeature is disabled and
     * <code>REQUEST_CONTEXT_KEY</code> value was not already set by another feature.
     */
    @Override
    protected void initializeBindingProvider(BindingProvider bp) {
        if (enabled) {
            bp.getRequestContext().put(REQUEST_CONTEXT_KEY, ENABLED_VALUE);
        } else {
            if (!bp.getRequestContext().containsKey(REQUEST_CONTEXT_KEY)) {
                // Disable handler only if another feature does not require it.
                bp.getRequestContext().put(REQUEST_CONTEXT_KEY, DISABLED_VALUE);
            }
        }
    }

}