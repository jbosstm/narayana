package org.jboss.jbossts.star.resource;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import org.jboss.jbossts.star.service.Coordinator;

public class RecoveringTransaction extends Transaction {
    public RecoveringTransaction(Uid uid) {
        super(uid);
    }

    protected int lookupStatus() {
        return ActionStatus.COMMITTING;
    }
}
