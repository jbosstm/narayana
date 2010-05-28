package org.jboss.as.integration.jbossts.jopr;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.ConfigurationUpdateStatus;
import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.ResourceContext;

import javax.management.*;
import java.io.IOException;
import java.util.Set;

public class TxnEngineComponent extends JMXClient {
	private ObjectName coreEnvObjName;
	private ObjectName statsObjName;

	@Override
	public void start(ResourceContext context) {
		super.start(context);

		 coreEnvObjName = TxnConstants.COREEBEAN;
		 statsObjName = TxnConstants.STATBEAN;
	}

    @Override
    public AvailabilityType getAvailability() {
        try {
            conn.getMBeanInfo(new ObjectName("jboss:service=TransactionManager"));
            return AvailabilityType.UP;
        } catch (Exception e) {
            return AvailabilityType.DOWN;
        }
    }

    @Override
	public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) {
		try {
			super.getValues(report, requests, coreEnvObjName);
			super.getValues(report, requests, statsObjName);
		} catch (Exception e) {
			log.info("TxnEngineComponent lookup error: " + e.getMessage());
		}
	}

	@Override
	public Configuration loadResourceConfiguration()
	{
		Configuration config = new Configuration();
		ConfigurationDefinition configDef = context.getResourceType().getResourceConfigurationDefinition();

		try {
			updateConfig(config, TxnConstants.CEBEAN, configDef.getPropertiesInGroup("CommonConfiguration"));
            updateConfig(config, TxnConstants.COREEBEAN, configDef.getPropertiesInGroup("CommonConfiguration"));

			updateConfig(config, TxnConstants.JTAEBEAN, configDef.getPropertiesInGroup("EngineConfiguration"));
			updateConfig(config, TxnConstants.JTAEBEAN, configDef.getPropertiesInGroup("EngineConfigurationClasses"));
			updateConfig(config, TxnConstants.CEBEAN, configDef.getPropertiesInGroup("CoordinatorConfiguration"));
			updateConfig(config, TxnConstants.REBEAN, configDef.getPropertiesInGroup("RecoveryConfiguration"));
			updateConfig(config, TxnConstants.COREEBEAN, configDef.getPropertiesInGroup("CoreEngineConfiguration"));

			/* TODO The following beans are not registered - if they are required then register them in transaction-jboss-beans.xml
			updateConfig(config, JDBCBEAN, configDef.getPropertiesInGroup("JDBCConfiguration"));
			updateConfig(config, JTSBEAN) configDef.getPropertiesInGroup("JTSConfiguration"));
			updateConfig(config, ORBPBEAN) configDef.getPropertiesInGroup("OrbPortabilityConfiguration"));
			 */				  
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}


		return config;
	}

	@Override
	public void updateResourceConfiguration(ConfigurationUpdateReport report)
	{
        report.setStatus(ConfigurationUpdateStatus.SUCCESS);
		updateResourceConfiguration(report, "CommonConfiguration", TxnConstants.CEBEAN, TxnConstants.COREEBEAN);
		updateResourceConfiguration(report, "EngineConfiguration", TxnConstants.JTAEBEAN);
		updateResourceConfiguration(report, "EngineConfigurationClasses", TxnConstants.JTAEBEAN);
		updateResourceConfiguration(report, "CoordinatorConfiguration", TxnConstants.CEBEAN);
		updateResourceConfiguration(report, "RecoveryConfiguration", TxnConstants.REBEAN);
		updateResourceConfiguration(report, "CoreEngineConfiguration", TxnConstants.COREEBEAN);
	}
}
