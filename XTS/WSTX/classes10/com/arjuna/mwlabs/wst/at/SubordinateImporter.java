package com.arjuna.mwlabs.wst.at;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.wsc.InvalidCreateParametersException;
import com.arjuna.wsc.ContextFactoryMapper;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.subordinate.SubordinateCoordinator;
import com.arjuna.mwlabs.wst.at.context.TxContextImple;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;

import java.util.HashMap;

/**
 * class to manage association of incoming AT transactions with subordinate AT transactions
 * coordinated by the local coordination servcie
 */
public class SubordinateImporter
{
    private static HashMap<String, TxContext> subordinateContextMap = new HashMap<String, TxContext>();

    /**
     * handle on the local 1.1 context factory implementation
     */
    private static ContextFactoryImple atContextFactory = (ContextFactoryImple) ContextFactoryMapper.getFactory().getContextFactory(AtomicTransactionConstants.WSAT_PROTOCOL);

    public static TxContext importContext(CoordinationContextType cc)
    {
        // get the subordinate transaction manager to install any existing
        // subordinate tx for this one or create and install a new one.
        final String identifier = cc.getIdentifier().getValue();
        TxContext subordinateTxContext = subordinateContextMap.get(identifier);
        if (subordinateTxContext == null) {
            // create a context for a local coordinator
            CoordinationContextType context = null;
            try {
                context = atContextFactory.create(AtomicTransactionConstants.WSAT_PROTOCOL, 0L, cc);
            } catch (InvalidCreateParametersException e) {
                // should not happen
            }
            subordinateTxContext = new TxContextImple(context);
            subordinateContextMap.put(identifier, subordinateTxContext);

            // register a cleanup callback with the subordinate transactionso that the entry gets removed
            // when the transcation commits or rolls back

            String subordinateId = context.getIdentifier().getValue().substring(4); // remove "urn:" prefix
            SubordinateCoordinator.SubordinateCallback callback = new SubordinateCoordinator.SubordinateCallback() {
                public String parentId = identifier;
                public void run()
                {
                    subordinateContextMap.remove(parentId);
                }
            };
            SubordinateCoordinator.addCallback(subordinateId, callback);
        }

        return subordinateTxContext;
    }
}
