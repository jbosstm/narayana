package com.arjuna.mwlabs.wst11.ba;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.mwlabs.wst11.ba.context.TxContextImple;
import com.arjuna.mwlabs.wst11.ba.ContextFactoryImple;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.subordinate.SubordinateBACoordinator;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.wsc11.ContextFactoryMapper;
import com.arjuna.wsc.InvalidCreateParametersException;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContext;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

import java.util.HashMap;

/**
 * class to manage association of incoming BA transactions with subordinate BA transactions
 * coordinated by the local coordination service
 */
public class SubordinateImporter
{
    private static HashMap<String, TxContext> subordinateContextMap = new HashMap<String, TxContext>();

    /**
     * handle on the local 1.1 context factory implementation
     */
    private static ContextFactoryImple baContextFactory = (ContextFactoryImple) ContextFactoryMapper.getFactory().getContextFactory(BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME);

    public static TxContext importContext(CoordinationContextType cc)
    {
        // get the subordinate transaction manager to install any existing
        // subordinate tx for this one or create and install a new one.
        final String identifier = cc.getIdentifier().getValue();
        TxContext subordinateTxContext = subordinateContextMap.get(identifier);
        if (subordinateTxContext == null) {
            // create a context for a local coordinator
            CoordinationContext context = null;
            try {
                context = baContextFactory.create(BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME, 0L, cc, false);
            } catch (InvalidCreateParametersException e) {
                // should not happen
            }
            subordinateTxContext = new TxContextImple(context);
            subordinateContextMap.put(identifier, subordinateTxContext);

            // register a cleanup callback with the subordinate transactionso that the entry gets removed
            // when the transcation commits or rolls back

            String subordinateId = context.getIdentifier().getValue().substring(4); // remove "urn:" prefix
            SubordinateBACoordinator.SubordinateCallback callback = new SubordinateBACoordinator.SubordinateCallback() {
                public String parentId = identifier;
                public void run()
                {
                    subordinateContextMap.remove(parentId);
                }
            };
            SubordinateBACoordinator.addCallback(subordinateId, callback);
        }

        return subordinateTxContext;
    }
}