package org.rhq.plugins.jbossts;


import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Class for discovering transactions known to the parent Transaction Manager
 */
public class TransactionDiscovery implements ResourceDiscoveryComponent
{
    /**
     *
     * @param context the context includes such things as the parent resource as well as configuration from the
     * rhq descriptor
     * @return a set of transactions that are executing a transaction termination protocol
     */
    public Set discoverResources(ResourceDiscoveryContext context)
    {
        Set<DiscoveredResourceDetails> transactions = new HashSet<DiscoveredResourceDetails>();
        TransactionEngineComponent parent = (TransactionEngineComponent) context.getParentResourceComponent();
        String version = context.getDefaultPluginConfiguration().getSimpleValue("Version", "0.2");
        String description = context.getDefaultPluginConfiguration().getSimpleValue("description", "A terminating transaction");

        // the parent component representing the Transaction Manager knows how to discover completing transaction
        for (EmsBean txn : parent.getTransactions()) {
            String on = txn.getBeanName().getCanonicalName();

            /*
             * transaction records contain an itype name component whereas participants also
             * contain a puid name component:
             * "jboss.jta:type=ObjectStore,itype=<typename>,uid=<uid>,puid=<uid>"
             */
            if (on.indexOf("puid") == -1 && on.indexOf("itype") != -1)
                transactions.add(new DiscoveredResourceDetails(
                    context.getResourceType(), on, on, version,
                    description, context.getDefaultPluginConfiguration(), null));
        }

        return transactions;
    }
}
