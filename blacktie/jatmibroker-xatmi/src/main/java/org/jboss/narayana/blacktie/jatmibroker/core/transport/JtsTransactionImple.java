package org.jboss.narayana.blacktie.jatmibroker.core.transport;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionException;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionImpl;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Unavailable;

import com.arjuna.ats.internal.jta.transaction.jts.AtomicTransaction;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.ORBManager;

/**
 * Wrapper class for running JBossTS transactions in an application server
 */
public class JtsTransactionImple extends TransactionImple {
    private static final Logger log = LogManager.getLogger(JtsTransactionImple.class);
    private static TransactionManager tm;
    private static final String IORTag = "IOR";

    /**
     * Construct a transaction based on an OTS control
     * 
     * @param wrapper the wrapped OTS control
     */
    public JtsTransactionImple(ControlWrapper wrapper) {
        super(new AtomicTransaction(wrapper));
    }

    /**
     * check whether the calling thread is associated with a transaction
     * 
     * @return true if there is transaction on the callers thread
     * @throws NamingException
     */
    public static boolean hasTransaction() {
        if (hasTransactionManager()) {
            try {
                return tm.getTransaction() != null;
            } catch (SystemException e) {
                return false;
            }
        } else {
            return TransactionImpl.current() != null;
        }
    }

    private static boolean hasTransactionManager() {
        try {
            return (getTransactionManager() != null);
        } catch (NamingException e) {
            return false;
        }
    }

    /**
     * Associated a transaction with the callers thread
     * 
     * @param ior IOR for the corresponding OTS transaction, must not be null
     * @param control
     * 
     * @throws SystemException
     * @throws IllegalStateException
     * @throws InvalidTransactionException
     * @throws ConfigurationException
     * @throws TransactionException 
     */
    public static void resume(String ior) throws InvalidTransactionException, IllegalStateException, SystemException,
            ConfigurationException, TransactionException {
        log.debug("resume control");
        if (hasTransactionManager()) {
            Transaction tx = controlToTx(ior);
            tm.resume(tx);
        } else {
            TransactionImpl transaction = new TransactionImpl(ior);
            TransactionImpl.resume(transaction);
        }
    }

    /**
     * Dissassociate the transaction currently associated with the callers thread
     * 
     * @return the dissassociated transaction
     * @throws SystemException
     */
    public static void suspend() throws SystemException {
        log.debug("suspend");
        if (hasTransactionManager()) {
            log.debug("suspending current");
            tm.suspend();
        } else {
            TransactionImpl.current().suspend();
        }
    }

    /**
     * Convert an IOR representing an OTS transaction into a JTA transaction
     * 
     * @param orb
     * 
     * @param ior the CORBA reference for the OTS transaction
     * @return a JTA transaction that wraps the OTS transaction
     */
    private static Transaction controlToTx(String ior) {
        log.debug("controlToTx: ior: " + ior);

        ControlWrapper cw = createControlWrapper(ior);
        TransactionImple tx = (TransactionImple) TransactionImple.getTransactions().get(cw.get_uid());

        if (tx == null) {
            log.debug("controlToTx: creating a new tx - wrapper: " + cw);
            tx = new JtsTransactionImple(cw);
            putTransaction(tx);
        }

        return tx;
    }

    /**
     * Lookup the JTA transaction manager
     * 
     * @return the JTA transaction manager in the VM
     * @throws NamingException
     */
    private synchronized static TransactionManager getTransactionManager() throws NamingException {
        if (tm == null) {
            Context ctx = new InitialContext();
            tm = (TransactionManager) ctx.lookup("java:/TransactionManager");
        }

        return tm;
    }

    /**
     * If the current transaction represents an OTS transaction then return it IOR
     * 
     * @return the IOR or null if the current transaction is not an OTS transaction
     * @throws NamingException
     * @throws org.omg.CORBA.SystemException
     * @throws SystemException
     * @throws Unavailable
     */
    public static String getTransactionIOR() throws org.omg.CORBA.SystemException, SystemException, Unavailable {
        log.debug("getTransactionIOR");

        TransactionImpl curr = TransactionImpl.current();
        if (curr != null) {
            log.debug("have JtsTransactionImple");
            return curr.getControlIOR();
        } else if (hasTransaction()) {
            log.debug("have tx mgr");
            Transaction tx = tm.getTransaction();
            log.debug("have arjuna tx");
            TransactionImple atx = (TransactionImple) tx;
            ControlWrapper cw = atx.getControlWrapper();
            log.debug("lookup control");
            Control c = cw.get_control();
            String ior = ORBManager.getORB().orb().object_to_string(c);
            log.debug("getTransactionIOR: ior: " + ior);
            return ior;
        } else {
            return null;
        }
    }

    private static ControlWrapper createControlWrapper(String ior) {
        org.omg.CORBA.Object obj = ORBManager.getORB().orb().string_to_object(ior);

        Control control = org.omg.CosTransactions.ControlHelper.narrow(obj);
        if (control == null)
            log.warn("createProxy: ior not a control");

        return new ControlWrapper(control);
    }

    // public static org.omg.CORBA.ORB getDefaultORB() {
    // if (haveORB())
    // try {
    // return ORBManager.getORB().orb();
    // } catch (Throwable t) {
    // }
    //
    // return null;
    // }
}
