package org.jboss.jbossts.xts.bridge.at;

import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.subordinate.SubordinateCoordinator;
import com.arjuna.mwlabs.wst11.at.ContextFactoryImple;
import com.arjuna.mwlabs.wst11.at.context.TxContextImple;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst.exceptions.UnknownTransactionException;
import com.arjuna.wsc11.ContextFactoryMapper;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.wst.SystemException;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;

/**
 * An API class for use by the JTA ==> AT bridge manager providing a wrapper around a subordinate transaction.
 * Static methods are provided to create and register a WS-AT subordinate transaction or to locate a recovered
 * transaction. The returned wrapper allows a client to drive the coordinator through prepare, (phase 2) commit
 * and/or rollback, to access the transaction id under which the coordinator is registered and, if the transaction
 * has not been recovered, to obtain a resumable tx context for the transaction.
 *
 * n.b. this class only supports bridging to WS-AT 1.1 transactions.
 */
public class BridgeWrapper
{
    /**
     * cached reference to the WS-AT 1.1. context factory - we only support bridging to WS-AT 1.1 subordinate
     * transactions
     */
    private static ContextFactoryImple contextFactory =
            (ContextFactoryImple)ContextFactoryMapper.getFactory().getContextFactory(AtomicTransactionConstants.WSAT_PROTOCOL);

    /**
     * this class handles all creation of bridged transactions 
     */

    private BridgeWrapper()
    {
    }

    /**
     * create an AT 1.1 subordinate transaction, associate it with the AT 1.1. registry then return a
     * BridgedTransaction wrapper allowing the transaction to be driven through prepare, commit
     * and/or rollback and providing access to the transaction id and a context which can be used to
     * resume the transaction.
     * @param expires the timeout for the bridged-to transaction or 0 if no timeout is required
     * @param isSecure true if AT 1.1. protocol messages for the bridged-to transaction should employ
     * secure communications, otherwise false
     * @return a wrapper for the bridged-to transaction
     * @throws SystemException
     */
    public static BridgeWrapper create(long expires, boolean isSecure)
    {
        // the AT 1.1 context factory provides us with a means to create the required data.

        ContextFactoryImple.BridgeTxData bridgeTxData = contextFactory.createBridgedTransaction(expires, isSecure);
        if (bridgeTxData != null) {
            BridgeWrapper bridgeWrapper = new BridgeWrapper();

            bridgeWrapper.context = new TxContextImple(bridgeTxData.context);
            bridgeWrapper.coordinator = bridgeTxData.coordinator;
            bridgeWrapper.id = bridgeTxData.identifier;
            return bridgeWrapper;
        } else {
            return null;
        }
    }

    /**
     * recreate a wrapper for a bridged-to WS-AT 1.1 transaction recovered from the log
     * @param identifier the identifier of a previously created bridged-to transaction
     * @return a wrapper for the bridged-to transaction or null if it may still be awaiting recovery
     * @throws UnknownTransactionException if recovery has been performed and no transaction with the
     * given identifier has been foung in the log
     */
    public static BridgeWrapper recover(String identifier) throws UnknownTransactionException
    {
        SubordinateCoordinator coordinator = SubordinateCoordinator.getRecoveredCoordinator(identifier);
        if (coordinator != null) {
            BridgeWrapper bridgeWrapper = new BridgeWrapper();
            bridgeWrapper.context = null;
            bridgeWrapper.coordinator =coordinator;
            bridgeWrapper.id = identifier;
            return bridgeWrapper;
        } else {
            XTSATRecoveryManager recoveryManager = XTSATRecoveryManager.getRecoveryManager();
            if (recoveryManager.isCoordinatorRecoveryStarted()) {
                throw new UnknownTransactionException();
            } else {
                return null;
            }
        }
    }

    /**
     * obtain the identifier for the bridged-to transaction
     * @return the identifier for the bridged-to transaction
     */
    public String getIdentifier()
    {
        return id;
    }

    /**
     * obtain a resumable transaction context for the bridged-to transaction
     * @return a resumable transaction context
     * @throws UnknownTransactionException if this transaction has been recovered from the log and hence
     * has no associated transaction context.
     */
    public TxContext getContext() throws UnknownTransactionException
    {
        if (context != null) {
            return context;
        } else {
            throw new UnknownTransactionException();
        }
    }

    /**
     * initiate synchronization beforeCompletion processing for the bridged-to transaction
     *
     * @return true if the beforeCompletion succeeds otherwise false.
     */
    public boolean prepareVolatile()
    {
        return coordinator.prepareVolatile();
    }

    /**
     * prepare the bridged-to transaction
     * @return the result of preparing the transaction
     */

	public int prepare ()
	{
        return coordinator.prepare();
	}
    

    /**
     * initiate synchronization afterCompletion processing for the bridged-to transaction following a
     * successful commit
     */
    public void commitVolatile()
    {
        coordinator.commitVolatile();
    }

    /**
     * perform a phase 2 commit for the bridged-to transaction
     */

	public void commit ()
	{
		coordinator.commit();
	}

    /**
     * initiate synchronization afterCompletion processing for the bridged-to transaction following a
     * rollback
     */
    public void rollbackVolatile()
    {
        coordinator.rollbackVolatile();
    }

    /**
     * rollback the bridged-to transaction
     */
	public void rollback ()
	{
        coordinator.rollback();
	}

    private SubordinateCoordinator coordinator;
    private TxContext context;
    private String id;
}
