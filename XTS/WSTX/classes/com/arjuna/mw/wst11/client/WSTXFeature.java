/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */

package com.arjuna.mw.wst11.client;

import javax.xml.ws.BindingProvider;

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