package org.rhq.plugins.jbossts;

import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.DeleteResourceFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import java.util.Collection;

/**
 * RHQ representation of a single transaction
 */
public class TransactionComponent extends BaseComponent implements DeleteResourceFacet {
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

    protected EmsConnection getEmsConnection() {
        TransactionEngineComponent tm = (TransactionEngineComponent) getResourceContext().getParentResourceComponent();

        return tm.getEmsConnection();
    }

    public AvailabilityType getAvailability() {
        return (deleted ? AvailabilityType.DOWN : AvailabilityType.UP);
    }

    public void deleteResource() throws Exception {
        try {
            if (!deleted)
                invokeOperation("remove", null);
        } catch (Exception e) {
            // TODO doing this produces a stack trace on the rhq console window - ask the rhq team to provide an error report
//            throw new Exception("Unable to remove transaction: " + e.getMessage());
        }
    }

    public OperationResult invokeOperation(String name, Configuration params)
    {
        if (!deleted) {
            OperationResult res = super.invokeOperation(name, params);

            if (res.getErrorMessage() == null)
                deleted = true;

            return res;
        } else {
            return new OperationResult("Transaction has already been deleted");
        }
    }

    /**
     * Transaction participants are represented by MBeans whose ObjectName is prefixed
     * by the MBean name of the transaction. Using this fact this method performs
     * an MBean query to select participants of this transaction only
     * @return Ems Beans representing this transactions participants
     */
    public Collection<EmsBean> getParticipants() {
        return getEmsConnection().queryBeans(getResourceContext().getResourceKey() + ",puid=*");
    }
}
