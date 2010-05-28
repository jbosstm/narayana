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

import java.util.Set;
import java.util.HashSet;

public class TxnStoreDiscoveryComponent implements ResourceDiscoveryComponent
{
    public Set discoverResources(ResourceDiscoveryContext context) throws InvalidPluginConfigurationException, Exception
    {
        Set<DiscoveredResourceDetails> txns = new HashSet<DiscoveredResourceDetails>();
        String key = context.getDefaultPluginConfiguration().getSimpleValue("key", "TxnLog1");
        String name = context.getDefaultPluginConfiguration().getSimpleValue("name", "Transaction Log");
        String version = context.getDefaultPluginConfiguration().getSimpleValue("version", "0.1");
        String description = context.getDefaultPluginConfiguration().getSimpleValue("description", "Storage for Transaction Logs");

        DiscoveredResourceDetails res =  new DiscoveredResourceDetails(
                context.getResourceType(), key, name, version, description,
                context.getDefaultPluginConfiguration(), null);

        txns.add(res);

        return txns;
    }
}
