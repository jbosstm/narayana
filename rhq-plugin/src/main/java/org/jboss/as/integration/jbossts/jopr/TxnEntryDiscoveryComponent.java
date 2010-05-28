/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010
 * @author JBoss Inc.
 */
package org.jboss.as.integration.jbossts.jopr;

import org.rhq.core.pluginapi.inventory.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.ObjectName;
import java.util.*;

public class TxnEntryDiscoveryComponent implements ResourceDiscoveryComponent
{
    static final Log log = LogFactory.getLog(TxnEntryDiscoveryComponent.class);

    public Set discoverResources(ResourceDiscoveryContext context)
    {
        Set<DiscoveredResourceDetails> txns = new HashSet<DiscoveredResourceDetails>();
        String version = context.getDefaultPluginConfiguration().getSimpleValue("version", "0.1");
        String description = context.getDefaultPluginConfiguration().getSimpleValue("description", "A terminating transaction");

        TxnStoreComponent parent = (TxnStoreComponent) context.getParentResourceComponent();
        Collection<ObjectName> components = parent.getComponents();

        for (ObjectName on : components) {
            txns.add(new DiscoveredResourceDetails(
                    context.getResourceType(), on.getCanonicalName(), on.getCanonicalName(), version,
                    description, context.getDefaultPluginConfiguration(), null));
        }

        return txns;
    }
}
