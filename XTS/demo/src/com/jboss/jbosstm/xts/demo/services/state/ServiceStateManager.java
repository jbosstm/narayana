package com.jboss.jbosstm.xts.demo.services.state;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;

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
     * load and restore the current persisted service state at the same time re-establishing any shadow
     * state and lock if appropiate. if no persisted state exists then return null.
     *
     * n.b. the subclass must ensure this is only called during initialization of the service
     */

    protected T restoreState()
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
            } catch (IOException e) {
                System.out.println("error : unable to read shadow restaurant manager state " + e);
            } catch (ClassNotFoundException e) {
                System.out.println("error : unknown class reading shadow restaurant manager state " + e);
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

        return current;
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

    /**
     * bit mask identifying no tx
     */
    public static final int TX_TYPE_NONE = 0;
    /**
     * bit mask identifying a WS-AT tx
     */
    public static final int TX_TYPE_AT = 1;
    /**
     * bit mask identifying a WS-BA tx
     */
    public static final int TX_TYPE_BA = 2;

    /**
     * bit mask identifying the union of both TX types
     */
    public static final int TX_TYPE_BOTH = TX_TYPE_AT | TX_TYPE_BA;

    /**
     * method for use by the AT and BA recovery modules to notify that the initial recovery scan
     * has completed. once both scans have been performed the manager can safely delete any
     * remaining prepared state for which there is no corresponding participant recovery record
     * @param scanType
     */
    public synchronized void recoveryScanCompleted(int scanType)
    {
        completedScans |= scanType;
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
    protected int completedScans = 0;
}
