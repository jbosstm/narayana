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

import org.rhq.core.domain.configuration.ConfigurationUpdateStatus;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.operation.OperationResult;
import org.rhq.core.domain.measurement.*;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;

import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryExp;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class TxnStoreComponent extends JMXClient
{
    private ObjectName objectName;
    private Collection<ObjectName> transactions;

    @Override
    public void start(ResourceContext context)
    {
        super.start(context);

        objectName = TxnConstants.OS_MBEAN;
        transactions = Collections.EMPTY_LIST;
    }

    public Collection<ObjectName> getComponents()
    {
        QueryExp query = Query.eq( Query.attr( "Participant" ), Query.value( false ) );

        try {
            // tell the mbean to update its view of which transactions are present
            log.debug("Updating view of Transactions");
            invokeOperation(objectName, "probe");
            transactions = conn.queryNames(new ObjectName("jboss.jta:type=ObjectStore,*"), query);
        } catch (Exception e) {
            log.info("MBean query error: " + e);
        }

//        String scope = "jboss.jta:type=ObjectStore,participant=false,*";
//        transactions = queryNames(scope, null);

        return transactions;
    }

    @Override
    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests)
    {
        try {
            if (objectName != null)
                getValues(report, requests, objectName);

        } catch (Exception e) {
            log.info("MBean log entry lookup error: " + e.getMessage());
        }
    }

    @Override
    public Configuration loadResourceConfiguration()
    {
        Configuration config = new Configuration();
        ConfigurationDefinition configDef = context.getResourceType().getResourceConfigurationDefinition();

        try {
            updateConfig(config, TxnConstants.OSENVBEAN, configDef.getPropertiesInGroup("ObjStoreConfiguration"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return config;
    }

    @Override
    public void updateResourceConfiguration(ConfigurationUpdateReport report)
    {
        report.setStatus(ConfigurationUpdateStatus.SUCCESS);
        updateResourceConfiguration(report, "ObjStoreConfiguration", TxnConstants.OSENVBEAN);
    }

    @Override
    public OperationResult invokeOperation(String name, Configuration params)
    {
        String result = "Unsupported operation or invalid Transaction Store";

        if (name.equals("refresh") && objectName != null) {
            int sz = transactions.size();

            try {
                invokeOperation(objectName, "probe");
                result = "Transaction count changed from " + sz + " to " + transactions.size();
            } catch (Exception e) {
                result = e.getMessage() == null ? e.getClass().getName() :e.getMessage();
            }
        }

        return new OperationResult(result);
    }
}
