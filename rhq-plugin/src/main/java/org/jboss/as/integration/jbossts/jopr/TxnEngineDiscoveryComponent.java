package org.jboss.as.integration.jbossts.jopr;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import javax.management.MalformedObjectNameException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

public class TxnEngineDiscoveryComponent implements ResourceDiscoveryComponent {
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext context) throws InvalidPluginConfigurationException {
        Set<DiscoveredResourceDetails> engines = new HashSet<DiscoveredResourceDetails>();
        String key = context.getDefaultPluginConfiguration().getSimpleValue("key", "TxnEngine1");
        String name = context.getDefaultPluginConfiguration().getSimpleValue("name", "Transaction Engine");
        String version = context.getDefaultPluginConfiguration().getSimpleValue("version", "0.1");
        String description = context.getDefaultPluginConfiguration().getSimpleValue("description", "JBossTS Transaction Engine Management");

        try {
            TxnConstants.setJMXUrl(context.getDefaultPluginConfiguration().getSimpleValue("jmxurl", TxnConstants.getJMXUrl()));
        } catch (MalformedURLException e) {
            throw new InvalidPluginConfigurationException(e);
        } catch (MalformedObjectNameException e) {
            throw new InvalidPluginConfigurationException(e);
        }

        DiscoveredResourceDetails res =  new DiscoveredResourceDetails(
                context.getResourceType(), key, name, version, description,
                context.getDefaultPluginConfiguration(), null);

        engines.add(res);

        return engines;
    }
}
