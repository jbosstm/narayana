package org.jboss.jbossts.txbridge.outbound;

import javax.xml.ws.BindingProvider;

import org.jboss.ws.api.configuration.AbstractClientFeature;

import com.arjuna.mw.wst11.client.WSTXFeature;

/**
 * Web service feature is used to enable or disable JTA context propagation over WS-AT.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class JTAOverWSATFeature extends AbstractClientFeature {

    /**
     * Key to store JTAOverWSATFeature's enabled/disabled value in SOAP header.
     */
    public static final String REQUEST_CONTEXT_KEY = "JTAOverWSATFeature";

    /**
     * Value to indicate that JTAOverWSATFeature is enabled.
     */
    public static final String ENABLED_VALUE = "true";

    /**
     * Value to indicate that JTAOverWSATFeature is disabled.
     */
    public static final String DISABLED_VALUE = "false";

    /**
     * Default constructor creates an instance of enabled JTAOverWSATFeature.
     */
    public JTAOverWSATFeature() {
        this(true);
    }

    /**
     * Parametrised constructor creates either enabled or disabled JTAOverWSATFeature based on <code>enabled</code> parameter.
     *
     * @param enabled true to create enabled JTAOverWSATFeature, false to create disabled JTAOverWSATFeature.
     */
    public JTAOverWSATFeature(final boolean enabled) {
        super(JTAOverWSATFeature.class.getName());
        this.enabled = enabled;
    }

    /**
     * Sets <code>JTAOverWSATFeature.REQUEST_CONTEXT_KEY</code> value to <code>JTAOverWSATFeature.ENABLED_VALUE</code> and
     * <code>WSTXFeature.REQUEST_CONTEXT_KEY</code> value to <code>WSTXFeature.ENABLED_VALUE</code> if JTAOverWSATFeature is
     * enabled. It is because WSTXFeature has to be enabled in order to make JTAOverWSATFeature work.
     *
     * Sets <code>JTAOverWSATFeature.REQUEST_CONTEXT_KEY</code> value to <code>JTAOverWSATFeature.DISABLED_VALUE</code> if
     * JTAOverWSATFeature is disabled.
     */
    @Override
    protected void initializeBindingProvider(BindingProvider bp) {
        if (enabled) {
            bp.getRequestContext().put(REQUEST_CONTEXT_KEY, ENABLED_VALUE);
            bp.getRequestContext().put(WSTXFeature.REQUEST_CONTEXT_KEY, WSTXFeature.ENABLED_VALUE);
        } else {
            bp.getRequestContext().put(REQUEST_CONTEXT_KEY, DISABLED_VALUE);
        }
    }

}
