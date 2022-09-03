package com.arjuna.mw.wst11.client;

import java.util.Set;

import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public abstract class AbstractWSTXHandler implements SOAPHandler<SOAPMessageContext> {

    /**
     * Delegate handler does all the work if context propagation is enabled.
     */
    private final JaxWSHeaderContextProcessor delegateHandler = new JaxWSHeaderContextProcessor();

    /**
     * Delegates message handling to the JaxWSHeaderContextProcessor if either WSTXFeature or default context propagation is
     * enabled.
     *
     * @see com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor#handleMessage(SOAPMessageContext, boolean)
     *
     * @param context
     * @return true
     */
    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        if (isContextPropagationEnabled(context)) {
            return delegateHandler.handleMessage(context, isMustUnderstand(context));
        }

        return true;
    }

    /**
     * Delegates fault handling to the JaxWSHeaderContextProcessor if either WSTXFeature or default context propagation is
     * enabled.
     *
     * @see com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor#handleFault(SOAPMessageContext)
     *
     * @param context
     * @return true
     */
    @Override
    public boolean handleFault(SOAPMessageContext context) {
        if (isContextPropagationEnabled(context)) {
            return delegateHandler.handleFault(context);
        }

        return true;
    }

    /**
     * Delegates to the JaxWSHeaderContextProcessor.
     *
     * @see com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor#close(MessageContext)
     *
     * @param context
     */
    @Override
    public void close(MessageContext context) {
        delegateHandler.close(context);
    }

    /**
     * Delegates to the JaxWSHeaderContextProcessor.
     *
     * @see com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor#getHeaders()
     */
    @Override
    public Set<QName> getHeaders() {
        return delegateHandler.getHeaders();
    }

    /**
     * MustUnderstand parameter of WS-AT/WS-BA header must be set to true if WSTXFeature is enabled.
     *
     * @param context
     * @return
     */
    private boolean isMustUnderstand(SOAPMessageContext context) {
        return WSTXFeature.ENABLED_VALUE.equals(context.get(WSTXFeature.REQUEST_CONTEXT_KEY));
    }

    /**
     * Checks if WSTXHandler should propagate WS-AT/WS-BA context.
     *
     * @param context
     * @return true|false
     */
    protected abstract boolean isContextPropagationEnabled(SOAPMessageContext context);

}
