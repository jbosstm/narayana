package org.jboss.jbossts.xts.environment;

import com.arjuna.common.internal.util.propertyservice.ConcatenationPrefix;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

import java.util.ArrayList;
import java.util.List;

/**
 * bean holding the configuration proeprty settings which identify intiialisation routines
 * to be run during XTS startup and shutdown
 */

@PropertyPrefix(prefix = "org.jboss.jbossts.xts.initialisation.")
public class XTSEnvironmentBean
{
    /**
     * the list of XTS recovery modules to be installed at startup and removed at shutdown
     */
    @ConcatenationPrefix(prefix="org.jboss.jbossts.xts.initialisation.xtsInitialisation")
    private volatile List<String> xtsInitialisations = new ArrayList<String>();

    public List<String> getXtsInitialisations() {
        return xtsInitialisations;
    }

    public void setXtsInitialisations(List<String> xtsInitialisations) {
        this.xtsInitialisations = xtsInitialisations;
    }
}
