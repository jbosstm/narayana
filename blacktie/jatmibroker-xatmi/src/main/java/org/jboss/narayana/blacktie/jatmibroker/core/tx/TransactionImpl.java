/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.blacktie.jatmibroker.core.tx;

import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.AtmiBrokerEnvXML;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.OrbManagement;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.Terminator;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;
import org.omg.CosTransactions.Unavailable;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

public class TransactionImpl {
    private static final Logger log = LogManager.getLogger(TransactionImpl.class);
    static TransactionFactory transactionFactory;
    private int timeout;
    protected Control control;
    private Terminator terminator;

    private Hashtable _childThreads;
    private boolean active = true;
    private OrbManagement orbManagement;

    public void finalize() throws Throwable {
        // TODO use ThreadActionData.purgeAction(this); not popAction
        ThreadActionData.popAction();
        super.finalize();
    }

    public TransactionImpl(int aTimeout) throws TransactionException, NotFound, CannotProceed,
            org.omg.CosNaming.NamingContextPackage.InvalidName, InvalidName, AdapterInactive, ConfigurationException {
        log.debug("TransactionImpl constructor");

        if (current() != null)
            throw new TransactionException("Nested transactions are not supported");

        timeout = aTimeout;

        control = null;
        terminator = null;

        try {
        	AtmiBrokerEnvXML client = new AtmiBrokerEnvXML();
            Properties properties = client.getProperties();
            orbManagement = OrbManagement.getInstance(properties);
            String toLookup = (String) properties.get("blacktie.trans.factoryid");
            org.omg.CORBA.Object aObject = orbManagement.getNamingContextExt().resolve_str(toLookup);
            transactionFactory = TransactionFactoryHelper.narrow(aObject);
        } catch (org.omg.CORBA.UserException cue) {
            throw new TransactionException(cue.getMessage(), cue);
        }
        
        log.debug(" creating Control");
        control = transactionFactory.create(timeout);
        ThreadActionData.pushAction(this);
        log.debug(" created Control " + control);

        setTerminator(control);
    }

    public TransactionImpl(String controlIOR) throws ConfigurationException, TransactionException {
        TransactionImpl curr = current();

        timeout = -1;

        try {
        	AtmiBrokerEnvXML client = new AtmiBrokerEnvXML();
            Properties properties = client.getProperties();
            orbManagement = OrbManagement.getInstance(properties);
        } catch (org.omg.CORBA.UserException cue) {
            throw new TransactionException(cue.getMessage(), cue);
        }

        org.omg.CORBA.Object obj = orbManagement.getOrb().string_to_object(controlIOR);

        if (curr != null) {
            log.debug("current() != null comparing IORs");
            String pIOR = curr.getControlIOR();
            org.omg.CORBA.Object pObj = orbManagement.getOrb().string_to_object(pIOR);

            log.debug("pIOR=" + pIOR + " pObj=" + pObj);
            if (pObj != null && pObj._is_equivalent(obj)) {
                log.debug("Different IORs same object");
                ThreadActionData.popAction();
            } else {
                log.info("Different IORs and different object");
                throw new TransactionException("Nested transactions are not supported");
            }
        }

        control = org.omg.CosTransactions.ControlHelper.narrow(obj);

        ThreadActionData.pushAction(this);

        setTerminator(control);
    }

    public Status getStatus() throws Unavailable {
        return control.get_coordinator().get_status();
    }

    public boolean equals(java.lang.Object obj) {
        if (obj instanceof TransactionImpl) {
            TransactionImpl other = (TransactionImpl) obj;

            return control.equals(other.control);
        }

        return false;
    }

    private void setTerminator(Control c) throws TransactionException {
        try {
            terminator = control.get_terminator();
            log.debug("Terminator is " + terminator);
        } catch (Unavailable e) {
            throw new TransactionException("Could not get the terminator", e);
        }
    }

    public String getControlIOR() {
        return orbManagement.getOrb().object_to_string(control);
    }

    public static TransactionImpl current() {
        log.trace("Getting current");
        return ThreadActionData.currentAction();
    }

    public Control getControl() {
        log.debug("TransactionImpl getControl");
        return control;
    }

    public void commit() throws TransactionException {
        log.debug("TransactionImpl commit");

        try {
            log.debug("calling commit");
            terminator.commit(true);
            active = false;
            ThreadActionData.popAction();
            log.debug("called commit on terminator");
        } catch (Exception e) {
            // TODO build an TransactionImpl hierarchy so we can perform
            // better
            // error reporting
            // presume abort and dissassociate the tx from the the current
            // thread
            active = false;
            ThreadActionData.popAction();

            throw new TransactionException("Could not commit the transaction: " + e.getMessage(), e);
        }
    }

    public void rollback() throws TransactionException {
        log.debug("TransactionImpl rollback");

        try {
            terminator.rollback();
            active = false;
            ThreadActionData.popAction();
            log.debug("called rollback on terminator");
        } catch (Exception e) {
            // presume abort and dissassociate the tx from the the current
            // thread
            active = false;
            ThreadActionData.popAction();

            throw new TransactionException("Could not rollback the transaction: " + e.getMessage(), e);
        }
    }

    public void rollback_only() throws TransactionException {
        log.debug("TransactionImpl rollback_only");

        try {
            control.get_coordinator().rollback_only();
            log.debug("tx marked rollback only");
        } catch (Unavailable e) {
            throw new TransactionException("Tx Manager unavailable for set rollback only", e);
        } catch (Exception e) {
            throw new TransactionException("Error setting rollback only", e);
        }
    }

    /**
     * Add the specified thread to the list of threads associated with this transaction.
     * 
     * @return <code>true</code> if successful, <code>false</code> otherwise.
     */
    public final boolean addChildThread(Thread t) {
        if (t == null)
            return false;

        synchronized (this) {
            // if (actionStatus <= ActionStatus.ABORTING)
            if (active) {
                if (_childThreads == null)
                    _childThreads = new Hashtable();

                // TODO _childThreads.put(ThreadUtil.getThreadId(t), t); //
                // makes sure so we don't get duplicates

                return true;
            }
        }

        return false;
    }

    /**
     * Remove a child thread. The current thread is removed.
     * 
     * @return <code>true</code> if successful, <code>false</code> otherwise.
     */
    public final boolean removeChildThread() // current thread
    {
        return removeChildThread(ThreadUtil.getThreadId());
    }

    /**
     * Remove the specified thread from the transaction.
     * 
     * @return <code>true</code> if successful, <code>false</code> otherwise.
     */
    public final boolean removeChildThread(String threadId) {
        if (threadId == null)
            return false;

        synchronized (this) {
            if (_childThreads != null) {
                _childThreads.remove(threadId);
                return true;
            }
        }

        return false;
    }

    public final TransactionImpl parent() {
        return null;
    }

    /**
     * Suspend the transaction association from the invoking thread. When this operation returns, the thread will not be
     * associated with a transaction.
     * 
     * @return a handle on the current TransactionImpl (if any) so that the thread can later resume association if required.
     */
    public static final TransactionImpl suspend() {
        TransactionImpl curr = ThreadActionData.currentAction();

        if (curr != null)
            ThreadActionData.purgeActions();

        return curr;
    }

    /**
     * Resume transaction association on the current thread. If the specified transaction is null, then this is the same as
     * doing a suspend. If the current thread is associated with a transaction then that association will be lost.
     * 
     * @param act the transaction to associate.
     * @return <code>true</code> if association is successful, <code>false</code> otherwise.
     */
    public static final boolean resume(TransactionImpl act) {
        if (act == null)
            suspend();
        else
            ThreadActionData.restoreActions(act);
        return true;
    }
}
