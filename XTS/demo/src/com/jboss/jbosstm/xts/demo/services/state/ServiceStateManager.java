package com.jboss.jbosstm.xts.demo.services.state;

import com.jboss.jbosstm.xts.demo.services.theatre.TheatreState;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import static com.jboss.jbosstm.xts.demo.services.state.ServiceStateConstants.*;

/**
 * An abstract class extended by the web service manager classes which provides a simple capability
 * for persistent state management.
 *
 * The web services need to maintain a copy of the current service state on the local disk. They also need
 * to be able to update the service state by first persisting a copy of the new state and then either
 * using it to replace the current version (commit) or throwing it away. In either case the updates
 * have to be done under the control of the web service transaction coordinator. Effectively, a service
 * manager derived from this class has the ability to operate like a simple transactional resource.
 */
public abstract class ServiceStateManager<T extends ServiceState> {
    protected ServiceStateManager()
    {
        transactions = new Hashtable<Object, T>();
        restoreState();
    }

    protected void putState(Object txId, T state)
    {
        transactions.put(txId, state);
    }

    protected T getState(Object txId)
    {
        return transactions.get(txId);
    }

    protected void removeState(Object txId)
    {
        transactions.remove(txId);
    }

    protected boolean isLocked()
    {
        return (preparedTxID != null);
    }

    protected Object getLockID()
    {
        return preparedTxID;
    }

    protected boolean isLockID(Object txId)
    {
        if (preparedTxID != null) {
            return preparedTxID.equals(txId);
        } else {
            return txId == null;
        }
    }

    protected void lock(Object preparedTxID)
    {
        this.preparedTxID = preparedTxID;
    }

    protected void unlock()
    {
        this.preparedTxID = null;
        this.notifyAll();
    }

    protected T getPreparedState()
    {
        if (preparedTxID != null) {
            return transactions.get(preparedTxID);
        }
        
        return null;
    }
    /**
     * persist the prepared state for a transaction including the transaction id and the derived state
     * containing the modified booking information
     * @param txId
     * @param childState
     * @throws java.io.IOException
     */
    public void writeShadowState(Object txId, T childState) throws IOException
    {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(getShadowStateFilename());
            oos = new ObjectOutputStream(fos);
            oos.writeObject(txId);
            oos.writeObject(childState);
        } finally {
            if (oos != null) {
                oos.close();
            } else if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * delete any persisted prepared state
     */
    public void clearShadowState(Object txId)
    {
        File shadowFile = new File(getShadowStateFilename());

        if (shadowFile.exists()) {
            shadowFile.delete();
        }
    }

    /**
     * install the persisted prepared state as the persisted current state
     */
    public void commitShadowState(Object txId)
    {
        File stateFile = new File(getStateFilename());
        File shadowFile = new File(getShadowStateFilename());
        shadowFile.renameTo(stateFile);
    }

    /**
     * load and install the current persisted service state at the same time re-establishing any shadow
     * state and lock if appropriate. if no persisted state exists then set current state to null.
     */

    private void restoreState()
    {
        File file = new File(getStateFilename());
        File shadowFile = new File(getShadowStateFilename());
        // we should only have
        // 1 no files
        // 2 a state file
        // 3 a state file and shadow file

        T current = null;
        T shadow = null;
        Object shadowTxId = null;

        FileInputStream fis = null;
        ObjectInputStream ois = null;
        if (file.exists()) {
            //
            try {
                fis = new FileInputStream(file);
                ois = new ObjectInputStream(fis);
                // ignore committed tx id
                ois.readObject();
                current = (T) ois.readObject();
                ois.close();
            } catch (IOException e) {
                System.out.println("error : unable to read current service state " + e);
            } catch (ClassNotFoundException e) {
                System.out.println("error : unknown class reading current service state " + e);
            }
        }

        try {
            if (ois != null) {
                ois.close();
            } else if (fis != null) {
                fis.close();
            }
        } catch (Exception e) {
            // ignore
        }

        fis = null;
        ois = null;

        if (shadowFile.exists()) {
            //
            try {
                fis = new FileInputStream(shadowFile);
                ois = new ObjectInputStream(fis);
                // we need the prepared tx id
                shadowTxId = ois.readObject();
                shadow = (T) ois.readObject();
                // flag this prpeared state for deletion by default
                // if we find out we need when processing recovery records it will be reset
                rollbackPreparedTx = true;
            } catch (IOException e) {
                System.out.println("error : unable to read shadow restaurant manager state " + e);
                shadowTxId = null;
            } catch (ClassNotFoundException e) {
                System.out.println("error : unknown class reading shadow restaurant manager state " + e);
                shadowTxId = null;
            }
        }

        try {
            if (ois != null) {
                ois.close();
            } else if (fis != null) {
                fis.close();
            }
        } catch (Exception e) {
            // ignore
        }

        if (current != null) {
            // see if we need to install any shadow satte
            if (shadow != null) {
                // reestablish lock which means we cannot proceed with any transactions until recovery kicks
                // in either to roll us forward or back
                assert current.isParentOf(shadow);
                putState(shadowTxId, shadow);
                preparedTxID = shadowTxId;
            } else {
                // no locking required
                preparedTxID = null;
            }
        }

        currentState = current;
    }

    /**
     * remove details of all currently known transactions
     */
    protected void clearTransactions() {
        Enumeration<Object> keys = transactions.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            transactions.remove(key);
            System.out.println("deleted prepared data for transaction " + key);
        }
    }

    /*****************************************************************************/
    /* Recovery methods maintaining consistency of local and  WSAT/WSBA state    */
    /*****************************************************************************/
    /**
     * called by the AT and BA recovery modules to notify the manager that a participant associated with
     * a specific AT or BA transaction has been recovered from a participant log record.
     * @param txID
     * @param txType
     * @return true if there is prepared local state for this transaction which needs to be committed or
     * rolled  back otherwise false
     */
    public boolean recovered(Object txID, int txType)
    {
        // if the recovered transaction id matches the prepared tx we need to let the caller know
        // that there is some local service state to be committed or rolled back. we also need to inhibit
        // deletion at end of scan because it will get removed as recovery progresses.
        if (isLockID(txID)) {
            // we may have crashed after writing a participant record for an AT participant which has
            // prepared, but not yet committed, local changes as part fo the AT 2 phase commit
            //
            // alternatively we may have crashed between prepare and commit of local changes for a
            // BA participant which was in the middle of completing.
            //
            // in the first case the recovery process will roll the prepared chanegs forward or back
            // when the coordinator sends either a COMMIT or an ABORT message.
            //
            // in the second case the recovery process will roll forward the local changes to reflect
            // the fact that the participant has been logged i.e. it will effectively finish the
            // COMPLETE operation. This will initiate sending of COMPLETED messages to the coordinator.
            // If the activity  is still running the coordinator will respond with either CLOSE or
            // COMPENSATE when the client terminates the activity. If the activity has already been
            // cancelled the coordinator will send an invalid transaction fault and the recovery code
            // ill ensure the changes are compensated.

            rollbackPreparedTx = false;
            return true;
        }

        // there is no local state associated with this transaction id so we are not interested
        // in the participant. This can happen when the participant log record is for  a BA participant
        // which has already completed

        return false;
    }

    /**
     * called at the end of the first recovery AT and BA scan to notify the manager that there are no
     * more AT or BA participant recovery records to process. this allows the manager to automatically
     * roll back local prepared state if it is not needed for subsequent recovery
     *
     * @param txType
     */
    public void recoveryScanCompleted(int txType)
    {
        completedScans |= txType;
        
        // if both AT and BA scans are completed and the prepared state is not needed for recovery then
        // whatever transaction created it will have failed before reaching prepare/complete so we can
        // safely roll back the local changes and unlock the service state. otherwise we leave the state
        // locked until the recovery process rolls it forward or back.
        if (completedScans == TX_TYPE_BOTH && rollbackPreparedTx) {
            rollback(getLockID());
        }
    }

    /**
     * Prepare local state changes for the supplied transaction
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public boolean prepare(Object txID)
    {
        // ensure that we have seen this transaction before
        T childState = getState(txID);
        if (childState == null) {
            return false;
        }
        // we have a single monolithic state element which means that only one transaction can prepare
        // at any given time. we lock this state at prepare by providing the txId as a locking id. it only
        // gets unlocked when we reach commit or rollback. the equivalent to the lock in memory is the
        // shadow state file on disk.
        synchronized (this) {
            while (isLocked()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            // check no other bookings have been committed

            if (!currentState.isParentOf(childState)) {
                removeState(txID);
                return false;
            }

            // see if we need user confirmation

            if (!confirmPrepare()) {
                removeState(txID);
                return false;
            }

            // ok, so lock the state against other prepare/commits

            lock(txID);
        }
        // if we got here then no other changes have invalidated our booking and we have locked out
        // further changes until commit or rollback occurs. we write the derived child state to the
        // shadow state file before returning. if we crash after the write we will detect the shadow
        // state at reboot and restore the lock.
        try {
            writeShadowState(txID, childState);
            return true;
        } catch (Exception e) {
            clearShadowState(txID);
            synchronized (this) {
                removeState(txID);
                unlock();
            }
            System.err.println("RestaurantManager.prepareSeats(): Error attempting to prepare transaction: " + e);
            return false;
        }
    }

    /**
     * commit local state changes for the supplied transaction
     *
     * @param txID
     */
    public void commit(Object txID)
    {
        synchronized (this) {
            // if there is a shadow state with this id then we need to copy the shadow state file over to the
            // real state file. it may be that there is no shadow state because this is a repeated commit
            // request. if so then we must have committed earlier so there is no harm done.
            if (isLockID(txID)) {
                commitShadowState(txID);
                // update the current state with the prepared state.
                currentState = getPreparedState();
                unlock();
            }
            removeState(txID);
        }
    }

    /**
     * roll back local state changes for the supplied transaction
     * @param txID
     */
    public void rollback(Object txID)
    {
        synchronized (this) {
            removeState(txID);
            if (isLockID(txID)) {
                clearShadowState(txID);
                unlock();
            }
            this.notifyAll();
        }
    }

    /**
     * identify the name of file used to store the current service state
      * @return the name of the file used to store the current service state
     */
    public abstract String getStateFilename();

    /**
     * identify the name of file used to store the shadow service state
      * @return the name of the file used to store the shadow service state
     */
    public abstract String getShadowStateFilename();

    /**
     * method called during prepare of local state changes allowing the user to force a prepare failue
     * @return true if the prepare shoudl succeed and false if it should fail
     */
    public abstract boolean confirmPrepare();

    /**
     * the latest version of the service state which includes a version id
     * this state object is always stored on disk in the current state file
     * a prepared version of a derived child state may also exist on disk in the
     * shadow state file
     */
    protected T currentState;

    /**
     * flag used to indicate that a prepare is in progress. updates to restaurantState may not proceed
     * until this is false and even then only if the updated value for the restaurant state is derived
     * from the current state. changes to this field must be guarded by synchronizing on the manager instance.
     */

    private Object preparedTxID;

    /**
     * The transactions we know about and their associated derived states.
     */
    private Hashtable<Object, T> transactions;

    /**
     * mask identifying whether we have completed an AT recovery scan or a BA recovery scan
     * when both scans have completed it is safe to roll back any remaining prepared state
     * changes since there can be no associated participant.
     */
    protected int completedScans = TX_TYPE_NONE;

    /**
     * Flag which determines whether we have to roll back any prepared changes to the server. We roll back
     * changes for an AT or BA participant if there is no associated log record because the participant never
     * prepared or completed, respectively. If we see a log record for an AT participant we leave the prepared
     * state behind since the AT participant has prepared and may still commit.
     */
    private boolean rollbackPreparedTx;
}
