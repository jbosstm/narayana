/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.internal.arjuna.objectstore.hornetq;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreAPI;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.io.IOException;
import java.io.SyncFailedException;
import java.util.HashSet;
import java.util.Set;

/* transaction-jboss-beans.xml:

    <bean name="HornetqJournalEnvironmentBean" class="com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqJournalEnvironmentBean">
        <property name="storeDir">${jboss.server.data.dir}/tx-object-store/HornetqJournalStore</property>
    </bean>
    <bean name="HornetqJournalStore" class="com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqJournalStore">
        <constructor>
            <parameter><inject bean="HornetqJournalEnvironmentBean"/></parameter>
        </constructor>
    </bean>
    <bean name="HornetqObjectStoreAdaptor" class="com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqObjectStoreAdaptor">
        <constructor>
            <parameter><inject bean="HornetqJournalStore"/></parameter>
        </constructor>
    </bean>
    <bean name="TxStoreManager" class="com.arjuna.ats.arjuna.objectstore.StoreManager">
        <constructor>
            <parameter><inject bean="HornetqObjectStoreAdaptor"/></parameter>
            <parameter><null/></parameter>
        </constructor>
    </bean>

    TODO wire to RecMgr/TxMgr lifecycle deps

*/

/**
 * Adaptor class that wraps the store to make it look like an ObjectStore.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-03
 */
public class HornetqObjectStoreAdaptor implements ObjectStoreAPI
{
    private final HornetqJournalStore store;

    // used for standalone bootstrap via StoreManager
    public HornetqObjectStoreAdaptor() throws IOException {

        HornetqJournalEnvironmentBean envBean = BeanPopulator.getDefaultInstance(HornetqJournalEnvironmentBean.class);

        this.store = new HornetqJournalStore(envBean);
    }

    // used for beans wiring type bootstrap when running embedded.
    public HornetqObjectStoreAdaptor(HornetqJournalStore store) {
        this.store = store;
    }

    @Override
    public void start()
    {
        try {
            store.start();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop()
    {
        try {
            store.stop();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read the object's shadowed state.
     *
     * @param u  The object to work on.
     * @param tn The type of the object to work on.
     * @return the state of the object.
     */
    @Override
    public InputObjectState read_uncommitted(Uid u, String tn) throws ObjectStoreException
    {
        throw new ObjectStoreException("This should never be called");
    }

    /**
     * Remove the object's uncommitted state.
     *
     * @param u  The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean remove_uncommitted(Uid u, String tn) throws ObjectStoreException
    {
        throw new ObjectStoreException("This should never be called");
    }


    private String ensureTypenamePrefix(String typeName)
    {
        if(!typeName.startsWith("/")) {
            typeName = "/"+typeName;
        }
        return typeName;
    }

    /**
     * Read the object's committed state.
     *
     * @param u  The object to work on.
     * @param typeName The type of the object to work on.
     * @return the state of the object.
     */
    @Override
    public InputObjectState read_committed(Uid u, String typeName) throws ObjectStoreException
    {
        typeName = ensureTypenamePrefix(typeName);

        return store.read_committed(u, typeName);
    }

    /**
     * Remove the object's committed state.
     *
     * @param u  The object to work on.
     * @param typeName The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean remove_committed(Uid u, String typeName) throws ObjectStoreException
    {
        typeName = ensureTypenamePrefix(typeName);

        return store.remove_committed(u, typeName);
    }

    /**
     * Hide the object's state in the object store. Used by crash
     * recovery.
     *
     * @param u  The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean hide_state(Uid u, String tn) throws ObjectStoreException
    {
        throw new ObjectStoreException("This should never be called");
    }

    /**
     * Reveal a hidden object's state.
     *
     * @param u  The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean reveal_state(Uid u, String tn) throws ObjectStoreException
    {
        throw new ObjectStoreException("This should never be called");
    }

    /**
     * Commit the object's state in the object store.
     *
     * @param u  The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean commit_state(Uid u, String tn) throws ObjectStoreException
    {
        throw new ObjectStoreException("This should never be called");
    }

    /**
     * @param u  The object to query.
     * @param typeName The type of the object to query.
     * @return the current state of the object's state (e.g., shadowed,
     *         committed ...) [StateStatus]
     */
    @Override
    public int currentState(Uid u, String typeName) throws ObjectStoreException
    {
        typeName = ensureTypenamePrefix(typeName);

        if( store.contains(u, typeName)) {
            return StateStatus.OS_COMMITTED;
        } else {
            return StateStatus.OS_UNKNOWN;
        }
    }


    /**
     * Write a copy of the object's uncommitted state.
     *
     * @param u    The object to work on.
     * @param tn   The type of the object to work on.
     * @param buff The state to write.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean write_uncommitted(Uid u, String tn, OutputObjectState buff) throws ObjectStoreException
    {
        throw new ObjectStoreException("This should never be called");
    }

    /**
     * Write a new copy of the object's committed state.
     *
     * @param u    The object to work on.
     * @param typeName   The type of the object to work on.
     * @param buff The state to write.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean write_committed(Uid u, String typeName, OutputObjectState buff) throws ObjectStoreException
    {
        typeName = ensureTypenamePrefix(typeName);

        return store.write_committed(u, typeName, buff);
    }

    @Override
    public boolean allObjUids(String typeName, InputObjectState foundInstances) throws ObjectStoreException
    {
        typeName = ensureTypenamePrefix(typeName);

        return allObjUids(typeName, foundInstances, StateStatus.OS_UNKNOWN);
    }

    /**
     * Obtain all of the Uids for a specified type.
     *
     * @param typeName    The type to scan for.
     * @param foundInstances The object state in which to store the Uids
     * @param matchState    The file type to look for (e.g., committed, shadowed). [StateStatus]
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean allObjUids(String typeName, InputObjectState foundInstances, int matchState) throws ObjectStoreException
    {
        boolean result = true;

        typeName = ensureTypenamePrefix(typeName);

        Uid[] uids = store.getUidsForType(typeName);

        OutputObjectState buffer = new OutputObjectState();

        try
        {
            if(uids != null && (matchState == StateStatus.OS_UNKNOWN || matchState == StateStatus.OS_COMMITTED))
            {
                for (Uid uid: uids)
                {
                    UidHelper.packInto(uid, buffer);
                }
            }
            UidHelper.packInto(Uid.nullUid(), buffer);
        }
        catch (IOException e)
        {
            throw new ObjectStoreException("TODO");
        }

        foundInstances.setBuffer(buffer.buffer());

        return result;
    }


    /**
     * Obtain all types of objects stored in the object store.
     *
     * @param foundTypes The state in which to store the types.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean allTypes(InputObjectState foundTypes) throws ObjectStoreException
    {
        boolean result = true;

        String[] knownTypes = store.getKnownTypes();
        Set<String> typeSet = new HashSet<String>();

        if (knownTypes == null || knownTypes.length == 0)
            return true;

        OutputObjectState buffer = new OutputObjectState();

        try
        {
            for (String typeName: knownTypes)
            {
                if(typeName.startsWith("/")) {
                    typeName = typeName.substring(1);
                }

                if(typeName.contains("/")) {
                    String value = "";
                    String[] parents = typeName.split("/");
                    for(String parent : parents) {
                        if(parent.length() == 0) {
                            continue;
                        }
                        if(value.length() > 0) {
                            value = value+"/";
                        }
                        value = value+parent;
                        if(!typeSet.contains(value)) {
                            typeSet.add(value);
                            buffer.packString(value);
                        }
                    }
                } else {
                    buffer.packString(typeName);
                }
            }
            buffer.packString("");
        }
        catch (IOException e)
        {
            throw new ObjectStoreException(e);
        }

        foundTypes.setBuffer(buffer.buffer());

        return result;
    }

    /**
     * Some object store implementations may be running with automatic
     * sync disabled. Calling this method will ensure that any states are
     * flushed to disk.
     */
    @Override
    public void sync() throws SyncFailedException, ObjectStoreException
    {
        // null-op in this impl.
    }

    /**
     * @return the "name" of the object store. Where in the hierarchy it appears, e.g., /ObjectStore/MyName/...
     */
    @Override
    public String getStoreName()
    {
        return store.getStoreName();
    }

    @Override
    public boolean fullCommitNeeded()
    {
        return false;
    }

    /**
     * Is the current state of the object the same as that provided as the last
     * parameter?
     *
     * @param u  The object to work on.
     * @param tn The type of the object.
     * @param st The expected type of the object. [StateType]
     * @return <code>true</code> if the current state is as expected,
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean isType(Uid u, String tn, int st) throws ObjectStoreException
    {
        return false;
    }
}