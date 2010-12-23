package org.rhq.plugins.jbossts;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.plugins.jmx.MBeanResourceComponent;

import java.util.HashSet;
import java.util.Set;

/**
 * A discovery class for transaction managers - there should generally be only one
 */
public class TransactionEngineDiscovery implements ResourceDiscoveryComponent<MBeanResourceComponent> {
    ResourceDiscoveryContext<MBeanResourceComponent> context;

    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<MBeanResourceComponent> context) throws InvalidPluginConfigurationException {
        Set<DiscoveredResourceDetails> engines = new HashSet<DiscoveredResourceDetails>();
        String key = context.getDefaultPluginConfiguration().getSimpleValue("key", "TransactionManager1");
        String name = context.getDefaultPluginConfiguration().getSimpleValue("name", "Transaction Engine");
        String version = "0.2"; //context.getDefaultPluginConfiguration().getSimpleValue("Version", "0.1");
        String description = context.getDefaultPluginConfiguration().getSimpleValue("description", "JBossTS Transaction Management");

        DiscoveredResourceDetails res =  new DiscoveredResourceDetails(
                context.getResourceType(), key, name, version, description,
                context.getDefaultPluginConfiguration(), null);

        this.context = context;

        engines.add(res);

        return engines;
    }
}
