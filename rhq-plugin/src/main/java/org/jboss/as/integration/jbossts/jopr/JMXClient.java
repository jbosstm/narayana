package org.jboss.as.integration.jbossts.jopr;

import org.rhq.core.domain.measurement.*;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.configuration.ConfigurationUpdateStatus;
import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnector;
import javax.management.*;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.*;

public class JMXClient extends TxnBaseComponent
{
	static Map<String, MBeanInfo> mBeanInfo;
	static Map<String, Map<String, MBeanAttributeInfo>> mBeanPropInfo;

	MBeanServerConnection conn;
	ResourceContext context;

	public JMXClient()
	{
		try {
			getMBeanServerConnection();
		} catch (Exception e) {
			throw new RuntimeException("MBeanServer connection error: " + e.getMessage());
		}
	}

	@Override
	public void start(ResourceContext context)
	{   //context.resourceDiscoveryComponent.
		super.start(context);
		this.context = context;

		initMBeanInfo();
	}

	public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests, ObjectName on) throws Exception
	{
		String[] props =  new String[requests.size()];
		MeasurementScheduleRequest[] reqs = requests.toArray(new MeasurementScheduleRequest[requests.size()]);

		for (int i = 0; i < props.length; i++) {
			props[i] = reqs[i].getName();
		}

		AttributeList al = getAttributes(on, false, props);
		Attribute[] aa = al.toArray(new Attribute[al.size()]);

		for (int i = 0; i < aa.length; i++) {
			if (reqs[i].getDataType().equals(DataType.MEASUREMENT))
				report.addData(new MeasurementDataNumeric(reqs[i], Double.valueOf(aa[i].getValue().toString())));
			else if (reqs[i].getDataType().equals(DataType.TRAIT))
				report.addData(new MeasurementDataTrait(reqs[i], aa[i].getValue().toString()));
		}
	}

	/**
	 * The plugin container will call this method and it needs to obtain the
	 * current configuration of the managed resource. Your plugin will obtain
	 * the managed resource's configuration in your own custom way and populate
	 * the returned Configuration object with the managed resource's
	 * configuration property values.
	 *
	 * @see org.rhq.core.pluginapi.configuration.ConfigurationFacet#loadResourceConfiguration()
	 */
	@Override
	public Configuration loadResourceConfiguration()
	{
		return super.loadResourceConfiguration();
		/*
		Configuration config = new Configuration();
		ConfigurationDefinition configDef = context.getResourceType().getResourceConfigurationDefinition();

		try {
			updateConfig(config, new ObjectName(JTAEBEAN), configDef.getPropertiesInGroup("EngineConfiguration"));
			updateConfig(config, new ObjectName(JTAEBEAN), configDef.getPropertiesInGroup("EngineConfigurationClasses"));
			updateConfig(config, new ObjectName(CEBEAN), configDef.getPropertiesInGroup("CoordinatorConfiguration"));
			updateConfig(config, new ObjectName(REBEAN), configDef.getPropertiesInGroup("RecoveryConfiguration"));
			updateConfig(config, new ObjectName(COREEBEAN), configDef.getPropertiesInGroup("CoreEngineConfiguration"));

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}


		return config;
		*/
	}

	protected void updateConfig(Configuration config, ObjectName on, List<PropertyDefinition> propDefs) throws Exception
	{
		String[] props = new String[propDefs.size()];

		for (int i = 0; i < propDefs.size(); i++)
			props[i] = propDefs.get(i).getName();

		List<Attribute> attrs = conn.getAttributes(on, props).asList();

		for (Attribute a : attrs)
			config.put(new PropertySimple(a.getName(), a.getValue()));
	}
	/**
	 * The plugin container will call this method when it has a new
	 * configuration for your managed resource. Your plugin will re-configure
	 * the managed resource in your own custom way, setting its configuration
	 * based on the new values of the given configuration.
	 *
	 * @see org.rhq.core.pluginapi.configuration.ConfigurationFacet#updateResourceConfiguration(ConfigurationUpdateReport)
	 */
	@Override
	public void updateResourceConfiguration(ConfigurationUpdateReport report)
	{
		report.setStatus(ConfigurationUpdateStatus.SUCCESS);
		updateResourceConfiguration(report, "StatsConfiguration", TxnConstants.CEBEAN);
	}

    /**
     * Update configuration properties
     *
     * @param report configuration properties together with their new values
     * @param groupName only apply changes to properties in this configuration group (see rhq-plugin.xml)
     * @param beans a list of beans that which define the target configuration properties
     *  (currently on the first bean in the list is used)
     */
	public void updateResourceConfiguration(ConfigurationUpdateReport report,
											String groupName, ObjectName ... beans)
	{
		Map<String, PropertySimple> props = report.getConfiguration().getSimpleProperties();
		ConfigurationDefinition configDef = context.getResourceType().getResourceConfigurationDefinition();
		Collection<String> errors = new ArrayList<String> ();
		List<PropertyDefinition> propDefs = configDef.getPropertiesInGroup(groupName);

		for (PropertyDefinition def : propDefs) {
			if (!def.isReadOnly() && props.containsKey(def.getName())) {
				Attribute a;
                MBeanAttributeInfo attrInfo = null;
                ObjectName bean = null;

                for (ObjectName on : beans) {
                    Map<String, MBeanAttributeInfo> mbprops = mBeanPropInfo.get(on.getCanonicalName());

                    if (mbprops != null && (attrInfo = mbprops.get(def.getName())) != null) {
                        bean = on;
                        break;
                    }
                }

                if (bean == null) {
                    errors.add("Property " + def.getName() + " in group " + groupName + ": no MBean with that property");
                } else {
                    try {
                        a = toJMXAttribute(attrInfo, props.get(def.getName()));
                        AttributeList al = setAttributes(bean, a);

                        // did the remote MBean fail the update request?
                        if (al.size() != 1)
                            errors.add("Property " + def.getName() + " in group " + groupName + " with MBean name " + bean.getCanonicalName() + ": failed to update");
                    } catch (Exception e) {
                        errors.add("Property " + def.getName() + " in group " + groupName + " with MBean name " + bean.getCanonicalName() + ": " + e.toString());
                    }
                }
			}
		}

		if (errors.size() != 0) {
			StringBuilder sb = new StringBuilder();
			String prev = report.getErrorMessage();

			for (String e : errors) {
				sb.append(e).append(System.getProperty("line.separator"));
			}

			log.debug(sb);

			if (prev != null)
				sb.append(prev);

			report.setErrorMessage(sb.toString());
			report.setStatus(ConfigurationUpdateStatus.FAILURE);
		}
	}

	Attribute toJMXAttribute(MBeanAttributeInfo mbpropinfo, PropertySimple ps) throws Exception {
		String type = mbpropinfo.getType();

        if (type.startsWith("java.lang."))
            type = type.substring("java.lang.".length());

		if (type.equals("String"))
			return new Attribute(ps.getName(), ps.getStringValue());
		else if (type.equalsIgnoreCase("Boolean"))
			return new Attribute(ps.getName(), ps.getBooleanValue());
		else if (type.equalsIgnoreCase("Long"))
			return new Attribute(ps.getName(), ps.getLongValue());
		else if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("Integer"))
			return new Attribute(ps.getName(), ps.getIntegerValue());
		else if (type.equalsIgnoreCase("Float"))
			return new Attribute(ps.getName(), ps.getFloatValue());
		else if (type.equalsIgnoreCase("Double"))
			return new Attribute(ps.getName(), ps.getDoubleValue());
		else
			throw new Exception("Unsupported object type");
	}

	private void initMBeanInfo() {
		mBeanInfo = new HashMap<String, MBeanInfo> ();
		mBeanPropInfo = new HashMap<String, Map<String, MBeanAttributeInfo>> ();

		try {
			for (ObjectName on : conn.queryNames(new ObjectName("jboss.jta:name=*"), null))
				try {
					MBeanInfo mbi = conn.getMBeanInfo(on);
					mBeanInfo.put(on.getCanonicalName(), mbi);

					Map<String, MBeanAttributeInfo> mbaimap = new HashMap<String, MBeanAttributeInfo> ();

					for (MBeanAttributeInfo mbai : mbi.getAttributes())
						mbaimap.put(mbai.getName(), mbai);

					mBeanPropInfo.put(on.getCanonicalName(), mbaimap);
				} catch (Exception e) {
					System.out.println("MBean info error (" + e.getMessage() + ") for " + on);
				}


		} catch (Exception e) {
			System.out.println("MBeanServer connection error for " + e.getMessage());
		}
	}

	MBeanServerConnection getMBeanServerConnection() throws Exception
	{
		if (conn == null) {
			try {
				JMXServiceURL url = new JMXServiceURL(TxnConstants.JMXURL);
				JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

				conn = jmxc.getMBeanServerConnection();

			} catch (MalformedURLException e) {
				System.out.println("jmx connect exception: " + e);
				throw e;
			} catch (IOException e) {
				System.out.println("jmx connect exception: " + e);
				throw e;
			}
		}

		return conn;
	}

	Object getAttribute(ObjectName objectName, String attr) throws Exception
	{
		return conn.getAttribute(objectName, attr);
	}

	AttributeList getAttributes(ObjectName on, MBeanAttributeInfo[] mbi) throws Exception {
		String[] attrs = new String[mbi.length];

		for (int i = 0; i < mbi.length; i++)
			attrs[i] = mbi[i].getName();

		return conn.getAttributes(on, attrs);
	}

	AttributeList getAttributes(ObjectName on, boolean verbose, String ... attrs) throws MalformedObjectNameException, InstanceNotFoundException, IOException, ReflectionException
	{
		AttributeList attributes = conn.getAttributes(on, attrs);

		if (verbose)
			for (Attribute a : attributes.asList())
				System.out.println(a.getName() + " = " + a.getValue());

		return attributes;
	}

	AttributeList setAttributes(ObjectName on, Attribute ... attrs) throws InstanceNotFoundException, IOException, ReflectionException
	{
		AttributeList attributes = new AttributeList();

		attributes.addAll(Arrays.asList(attrs));

		return conn.setAttributes(on, attributes);
	}

	public MBeanInfo getMBeanInfo(ObjectName on) throws Exception
	{
		MBeanInfo mbi = mBeanInfo.get(on.getCanonicalName());

		return (mbi == null ? conn.getMBeanInfo(on) : mbi);
	}

	public Object invokeOperation(ObjectName on, String method, Object ... params) throws Exception
	{
		return conn.invoke(on, method, params, null);
	}

	public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) {
		try {
			return conn.queryMBeans(name, query);
		} catch (IOException e) {
			log.info("MBean query error: " + e);
		}

		return Collections.EMPTY_SET;
	}

	public Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
		try {
			return conn.queryNames(name, query);	 // conn.queryNames(new ObjectName("jboss.jta:type=ObjectStore,*"), null)
		} catch (IOException e) {
			log.info("MBean query error: " + e);
		}

		return Collections.EMPTY_SET;
	}

	public Set<ObjectInstance> queryMBeans(String name, QueryExp query) {
		try {
			return queryMBeans(new ObjectName(name), query);
		} catch (MalformedObjectNameException e) {
			log.info("MBean query error: " + e);
			return Collections.EMPTY_SET;
		}
	}
	public Set<ObjectName> queryNames(String name, QueryExp query) {
		try {
			return queryNames(new ObjectName(name), query);
		} catch (MalformedObjectNameException e) {
			log.info("MBean query error: " + e);
			return Collections.EMPTY_SET;
		}	}
}
