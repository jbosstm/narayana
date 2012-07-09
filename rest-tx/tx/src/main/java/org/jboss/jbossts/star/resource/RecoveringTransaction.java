package org.jboss.jbossts.star.resource;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;

public class RecoveringTransaction extends Transaction {
    public RecoveringTransaction(Uid uid) {
        super(uid);
    }

    protected int lookupStatus() {
        return ActionStatus.COMMITTING;
    }
}
