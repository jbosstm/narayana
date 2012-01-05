package org.jboss.jbossts.xts.environment;

import com.arjuna.common.internal.util.propertyservice.ConcatenationPrefix;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

import java.util.ArrayList;
import java.util.List;

/**
 * bean storing WSCF implementation configuration values derived from the xts properties file, system property
 * settings and, in case we are running inside JBossAS the xts bean.xml file
 */
@PropertyPrefix(prefix = "org.jboss.jbossts.xts.")
public class WSCFEnvironmentBean
{
    /**
     * list of names of classes which provide protocol implementations, currently either high level services
     * or context factories. these are culled from the xts proeprties file by scanning for properties  with
     * the requisite prefix. The are injected from the beans.xml as a list of names. 
     */
    @ConcatenationPrefix(prefix="org.jboss.jbossts.xts.protocolImplementation")
    private volatile List<String> protocolImplementations = new ArrayList<String>();

    public List<String> getProtocolImplementations() {
        return protocolImplementations;
    }

    public void setProtocolImplementations(List<String> protocolImplementations) {
        this.protocolImplementations = protocolImplementations;
    }
}
