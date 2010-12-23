package org.rhq.plugins.jbossts;

import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A class for discovering which participants are involved in a particular transaction
 */
public class ParticipantDiscovery implements ResourceDiscoveryComponent
{
    /**
     * @param context information about the containing transaction together with information available in
     * the rhq descriptor
     * @return a the participants involved in this transaction
     */
    public Set discoverResources(ResourceDiscoveryContext context)
    {
        Set<DiscoveredResourceDetails> participants = new HashSet<DiscoveredResourceDetails>();
        TransactionComponent parent = (TransactionComponent) context.getParentResourceComponent();
        String version = context.getDefaultPluginConfiguration().getSimpleValue("Version", "0.2");
        String description = context.getDefaultPluginConfiguration().getSimpleValue("description", "A Transaction Participant");

        for (EmsBean participant : parent.getParticipants()) {
            String on = participant.getBeanName().getCanonicalName();

            participants.add(new DiscoveredResourceDetails(
                    context.getResourceType(), on, on, version,
                    description, context.getDefaultPluginConfiguration(), null));
        }

        return participants;
    }
}