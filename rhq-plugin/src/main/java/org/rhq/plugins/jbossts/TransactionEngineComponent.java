/*
* Jopr Management Platform
* Copyright (C) 2005-2008 Red Hat, Inc.
* All rights reserved.
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License, version 2, as
* published by the Free Software Foundation, and/or the GNU Lesser
* General Public License, version 2.1, also as published by the Free
* Software Foundation.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License and the GNU Lesser General Public License
* for more details.
*
* You should have received a copy of the GNU General Public License
* and the GNU Lesser General Public License along with this program;
* if not, write to the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*/
package org.rhq.plugins.jbossts;

import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.operation.EmsOperation;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.ConfigurationUpdateStatus;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.operation.OperationResult;
import org.rhq.plugins.jmx.MBeanResourceComponent;

import java.util.Collection;
import java.util.Collections;

/**
 * The management view of a Transaction Manager instance. By extending from MBeanResourceComponent
 * the RHQ framework makes available an Ems (a kind of JMX wrapper) connection to the target system
 * being managed.
 */
public class TransactionEngineComponent extends MBeanResourceComponent {
    public static final String OS_BEAN = "jboss.jta:type=ObjectStore";
    public static final String CE_BEAN = "jboss.jta:name=CoordinatorEnvironmentBean";
    public static final String CORE_BEAN = "jboss.jta:name=CoreEnvironmentBean";

    public static final String STATS_PROP = "EnableStatistics";
    public static final String BUILD_ID_PROP = "BuildId";
    public static final String BUILD_VER_PROP = "BuildVersion";

    @Override
    public Configuration loadResourceConfiguration() {
        Configuration config = new Configuration();
        EmsBean bean1 = getEmsConnection().getBean(CE_BEAN);
        EmsBean bean2 = getEmsConnection().getBean(CORE_BEAN);

        config.put(new PropertySimple(STATS_PROP, bean1.getAttribute(STATS_PROP).getValue()));
        config.put(new PropertySimple(BUILD_ID_PROP, bean2.getAttribute(BUILD_ID_PROP).getValue()));
        config.put(new PropertySimple(BUILD_VER_PROP, bean2.getAttribute(BUILD_VER_PROP).getValue()));

        return config;
    }

    @Override
    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
        PropertySimple configProp = report.getConfiguration().getSimple(STATS_PROP);
        EmsBean bean = getEmsConnection().getBean(CE_BEAN);

        try {
            bean.getAttribute(STATS_PROP).setValue(configProp.getBooleanValue());
            report.setStatus(ConfigurationUpdateStatus.SUCCESS);
        } catch (Exception e) {
            report.setStatus(ConfigurationUpdateStatus.FAILURE);
            report.setErrorMessage("Error enabling/disabling statistics: " + e);
        }
    }

    @Override
    public OperationResult invokeOperation(String name, Configuration parameters) throws Exception {
        if ("probe".equals(name)) {
            Collection<EmsBean> transactions = getTransactions();

            return new OperationResult("Found " + transactions.size() + " transactions");
        } else {
            return super.invokeOperation(name, parameters);
        }
    }

    /**
     * Connect to the target JVM and find all completing transactions by
     * invoking the probe() method of the ObjectStoreBrowser MBean
     *
     * @return transactions encapsulated as Ems Beans
     */
    public Collection<EmsBean> getTransactions() {
        try {
            // ask the MBean to update its view of which transactions are present
            EmsOperation op = getEmsConnection().getBean(OS_BEAN).getOperation("probe");

            op.invoke(new Object[0]);

            return getEmsConnection().queryBeans(OS_BEAN + ",*");
        } catch (Exception e) {
            log.info("MBean query error: " + e);

            return Collections.EMPTY_LIST;
        }
    }
}
