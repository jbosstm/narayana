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

import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.domain.measurement.*;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
import org.rhq.core.pluginapi.operation.OperationResult;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryExp;
import java.util.*;

public class TxnEntryComponent extends JMXClient
{
    private ObjectName objectName;
    /*
      * There are two ways to delete the transaction, via:
      * 1) invokeOperation, or
      * 2) deleteResource
      * But deleteResource does not provide any way of notifying the caller that the operation
      * failed and the rhq console would then be missing the transaction (until the next
      * update).
      * So, until it's fixed, record whether or not it has already been deleted
      */
    private boolean deleted;

    @Override
    public void start(ResourceContext context) throws InvalidPluginConfigurationException
    {
        super.start(context);
//        TxnStoreComponent store = (TxnStoreComponent) context.getParentResourceComponent();
        try {
            objectName = new ObjectName(context.getResourceKey());
        } catch (MalformedObjectNameException e) {
            log.warn("Invalid transaction mbean name: " + context.getResourceKey());
            throw new InvalidPluginConfigurationException(e);
        }
    }
    
    @Override
    public AvailabilityType getAvailability() {
        return (deleted ? AvailabilityType.DOWN : AvailabilityType.UP);
    }

    @Override
    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests)
    {
        try {
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
    public OperationResult invokeOperation(String name, Configuration params)
    {
        String result = "Unsupported operation or invalid Transaction Store";

        if (name.equals("remove")) {
            try {
                Object res = invokeOperation(objectName, "remove");
                result = "operation returned " + res;
                deleted = true;
            } catch (Exception e) {
                result = e.getMessage() == null ? e.getClass().getName() :e.getMessage();
            }
        }

        return new OperationResult(result);
    }

	public void deleteResource() throws Exception {
		try {
            if (!deleted) {
			    invokeOperation(objectName, "remove");

                deleted = true;
        	    log.debug("Transaction removed from log store: " + objectName);
            }
		} catch (Exception e) {
            // TODO doing this produces a stack trace on the rhq console window - ask the rhq team to provide an error report
        	log.debug("Unable to remove transaction from log store: " + e.getMessage());
			throw new Exception("Unable to remove transaction: " + e.getMessage());
		}
	}

    public Collection<ObjectName> getParticipants() {
        QueryExp query = Query.eq( Query.attr( "Participant" ), Query.value( true ) );
        String scope = objectName.getCanonicalName() + ",*";

        try {
            return conn.queryNames(new ObjectName(scope), query);
        } catch (Exception e) {
            log.info("MBean query error: " + e);
            return Collections.EMPTY_SET;
        }
    }
}
