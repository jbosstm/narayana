package org.rhq.plugins.jbossts;

import org.mc4j.ems.connection.EmsConnection;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.operation.OperationResult;

/**
 * RHQ representation of a Transaction Participant
 */
public class ParticipantComponent extends BaseComponent {

    protected EmsConnection getEmsConnection() {
        TransactionComponent tm = (TransactionComponent) getResourceContext().getParentResourceComponent();

        return tm.getEmsConnection();
    }

    public OperationResult invokeOperation(String name, Configuration params) {
        OperationResult res = new OperationResult();

        if (name.equals("setStatus")) {
            try {
                getBean().getAttribute("Status").setValue("PREPARED");
//                Object rv = getBean().getOperation(name).invoke(new Object[] {params.getSimpleValue("status", "PREPARED")});

                res.setSimpleResult("Operation succeeded");
            } catch (Exception e) {
                res.setErrorMessage("Operation failed: " + (e.getMessage() == null ? e.getClass().getName() :e.getMessage()));
            }
        }

        return res;
    }
}