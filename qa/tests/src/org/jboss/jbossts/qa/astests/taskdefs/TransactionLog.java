/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package org.jboss.jbossts.qa.astests.taskdefs;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility class for interaction with a transaction log
 */
public class TransactionLog
{
    /**
     * Default object type for recoveryStore operations
     */
    public static final String DEFAULT_OBJECT_TYPE = "StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction";

    private RecoveryStore recoveryStore;

    public TransactionLog(String storeDir, String impleType)
    {
        init(storeDir, impleType);
    }

    private void init(String storeDir, String impleType)
    {
		ObjectStoreEnvironmentBean objectStoreEnvironmentBean = arjPropertyManager.getObjectStoreEnvironmentBean();
		objectStoreEnvironmentBean.setObjectStoreDir(storeDir);

        if (impleType != null)
        {
            try
            {
                Class c = Class.forName(impleType);
                
                recoveryStore = (RecoveryStore) c.getDeclaredConstructor().newInstance();
            }
            catch (final Throwable ex)
            {
                ex.printStackTrace();
            }
        }
        else
            recoveryStore = StoreManager.getRecoveryStore();
    }

    /**
     * Remove any committed objects from the storer
     * @param objectType the type of objects that should be removed
     * @return the number of objects that were purged
     * @throws ObjectStoreException the recoveryStore implementation was unable to remove a committed object
     */
    public int clearXids(String objectType) throws ObjectStoreException
    {
        Collection<Uid> uids = getIds(objectType);

        for (Uid uid : uids)
            recoveryStore.remove_committed(uid, objectType);

        return uids.size();
    }

    public Collection<Uid> getIds(String objectType) throws ObjectStoreException
    {
        return getIds(null, objectType);
    }

    /**
     * Get a list object ids for a given object type
     *
     * @param ids holder for the returned uids
     * @param objectType The type of object to search in the recoveryStore for
     * @return all objects of the given type
     * @throws ObjectStoreException the recoveryStore implementation was unable retrieve all types of objects
     */
    public Collection<Uid> getIds(Collection<Uid> ids, String objectType) throws ObjectStoreException
    {
        if (ids == null)
            ids = new ArrayList<Uid> ();


        InputObjectState types = new InputObjectState();

        if (recoveryStore.allTypes(types))
        {
            String theName;

            try
            {
                boolean endOfList = false;

                while (!endOfList)
                {
                    theName = types.unpackString();

                    if (theName.compareTo("") == 0)
                        endOfList = true;
                    else
                    {
                        if (objectType != null && !theName.equals(objectType))
                            continue;

                        InputObjectState uids = new InputObjectState();

                        if (recoveryStore.allObjUids(theName, uids))
                        {
                            Uid theUid = new Uid(Uid.nullUid());

                            try
                            {
                                boolean endOfUids = false;

                                while (!endOfUids)
                                {
                                    theUid = UidHelper.unpackFrom(uids);

                                    if (theUid.equals(Uid.nullUid()))
                                        endOfUids = true;
                                    else
                                        ids.add(theUid);
                                }
                            }
                            catch (Exception e)
                            {
                                // end of uids!
                            }
                        }

                        System.out.println();
                    }
                }
            }
            catch (Exception e)
            {
                System.err.println(e);

                // end of list!
            }
        }

        return ids;
    }

    public int getStatus(Uid uid)
    {
        AtomicAction action = new AtomicAction(uid);

        action.activate();

        return action.status();
    }
}
