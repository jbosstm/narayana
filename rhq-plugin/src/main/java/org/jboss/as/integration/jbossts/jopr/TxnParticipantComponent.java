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

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.operation.OperationResult;

import javax.management.Attribute;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Set;

/**
 * TODO
 */
public class TxnParticipantComponent extends JMXClient
{
    private ObjectName objectName;
    private TxnEntryComponent txn;

    @Override
    public void start(ResourceContext context)
    {
        super.start(context);

        txn = (TxnEntryComponent) context.getParentResourceComponent();

        try {
            objectName = new ObjectName(context.getResourceKey());
        } catch (MalformedObjectNameException e) {
            log.warn("Invalid transaction participant mbean name: " + e);
        }
    }


    @Override
    public AvailabilityType getAvailability() {
        return (txn != null ? txn.getAvailability() : AvailabilityType.DOWN );
    }

    @Override
    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests)
    {
        if (objectName != null)
            try {
                super.getValues(report, requests, objectName);
            } catch (Exception e) {
                log.warn("Error retrieving txn participant attributes: " + e);
            }
    }


    @Override
    public OperationResult invokeOperation(String name, Configuration params) throws Exception
    {
        String result = "Unsupported operation or invalid Transaction Store";

        if (name.equals("setStatus") && objectName != null) {
            try {
                conn.setAttribute(objectName, new Attribute("Status", params.getSimpleValue("status", "PREPARED")));
                //Object res = invokeOperation(objectName, "setStatus", params.getSimpleValue("status", "PREPARED"));
                result = "Operation succeeed"; // + res;
            } catch (Exception e) {
                result = "Operation failed: ";
                result += e.getMessage() == null ? e.getClass().getName() :e.getMessage();
                throw new Exception(result, e);
            }
        }

        return new OperationResult(result);
    }
}
