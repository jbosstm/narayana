package org.rhq.plugins.jbossts;

import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.attribute.EmsAttribute;
import org.mc4j.ems.connection.bean.operation.EmsOperation;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.*;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

abstract public class BaseComponent implements ResourceComponent, MeasurementFacet, OperationFacet {
    private ResourceContext context;

    abstract protected EmsConnection getEmsConnection();

    protected ResourceContext getResourceContext() {
        return context;
    }

    public void start(ResourceContext context) throws InvalidPluginConfigurationException {
        this.context = context;
    }

    public void stop() {
    }

    public AvailabilityType getAvailability() {
        return AvailabilityType.UP;
    }

    protected EmsBean getBean() {
        EmsConnection conn = getEmsConnection();

        return conn.getBean(context.getResourceKey());
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception
    {
        EmsBean bean = getBean();
        String[] props =  new String[requests.size()];
        MeasurementScheduleRequest[] reqs = requests.toArray(new MeasurementScheduleRequest[requests.size()]);

        for (int i = 0; i < props.length; i++)
            props[i] = reqs[i].getName();

        List<String> reqProps = Arrays.asList(props);
        List<EmsAttribute> al = bean.refreshAttributes(reqProps);

        EmsAttribute[] aa = al.toArray(new EmsAttribute[al.size()]);

        for (EmsAttribute emsAttr : aa) {
            int index = reqProps.indexOf(emsAttr.getName());

            //assert (index != -1);
            if (index != -1) {
                MeasurementScheduleRequest msr = reqs[index];

                if (msr.getDataType().equals(DataType.MEASUREMENT))
                    report.addData(new MeasurementDataNumeric(msr, Double.valueOf(emsAttr.getValue().toString())));
                else if (msr.getDataType().equals(DataType.TRAIT))
                    report.addData(new MeasurementDataTrait(msr, emsAttr.getValue().toString()));
            }
        }
    }

    public OperationResult invokeOperation(String name, Configuration params)
    {
        OperationResult result = new OperationResult();

        try {
            EmsBean bean = getBean();
            EmsOperation op = bean.getOperation(name);
            Object res = op.invoke(new Object[0]);
            result.setSimpleResult("operation returned " + res);
            result.setErrorMessage(null);
        } catch (Exception e) {
            result.setErrorMessage(e.getMessage() == null ? e.getClass().getName() :e.getMessage());
        }

        return result;
    }
}