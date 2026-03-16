package org.rhq.plugins.jbossts;

import java.util.Iterator;
import java.util.Map;

import org.rhq.core.domain.configuration.Property;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.plugins.jmx.MBeanResourceComponent;

public class XMBeanResourceComponent extends MBeanResourceComponent<MBeanResourceComponent> {
    @Override
    public void updateResourceConfiguration(ConfigurationUpdateReport report) {
        // the default implementation ignores the readOnly attribute on the resource-configuration property so remove them from the report
        Map<String, PropertyDefinition> propDefs = getResourceContext().getResourceType().getResourceConfigurationDefinition().getPropertyDefinitions();
        Iterator<Property> piter = report.getConfiguration().getProperties().iterator();

        while (piter.hasNext()) {
            Property p = piter.next();

            if (propDefs.containsKey(p.getName()) && propDefs.get(p.getName()).isReadOnly()) {
                piter.remove();
//                report.getConfiguration().remove(p.getName());
            }
        }

        super.updateResourceConfiguration(report);
    }
}
