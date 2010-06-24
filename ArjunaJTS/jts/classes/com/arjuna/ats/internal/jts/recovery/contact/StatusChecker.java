/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: StatusChecker.java 2342 2006-03-30 13:06:17Z  $
 *
 */

package com.arjuna.ats.internal.jts.recovery.contact;

import com.arjuna.ats.jts.logging.jtsLogger;

import com.arjuna.common.util.logging.*;

import java.util.Hashtable;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.*;

import com.arjuna.ArjunaOTS.*;

import com.arjuna.orbportability.orb.*;
import com.arjuna.orbportability.*;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;

import com.arjuna.ats.jts.utils.Utility;

import com.arjuna.ats.jts.OTSManager;



import org.omg.CosTransactions.*;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.Inactive;

/**
 * Checks the status of a transaction as known to the original process that
 * created it - assuming the transaction still exists.
 *
 *
 * (relies on the fact (true for 2.1) that any ArjunaFactory can be used to
 * find the status of any transaction.
 *
 * Singleton class
 */

public class StatusChecker
{
  /* the interface of this class is intended to allow for possible
   * future extension to do statuschecking by other (non-corba) means
   *
   * On the other hand, there is some nasty interlinking between bits of this
   * class - some redesign into separate classes would make things easier to
   * follow.
   */

 // lookup the relevant factory according to the uid of the FactoryContactItem
private Hashtable _itemFromUid;

private static StatusChecker _checker;

/**
 * create a static singleton to do the work for the static methods
 */
static
{
    _checker = new StatusChecker();
}


/**
 * get the status in the original process, given the uid of the contact
 * item (which is the uid of the process)
 */
public static Status get_status(Uid transactionUid, Uid itemUid) throws Inactive
{
    if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("StatusChecker.get_status(" + transactionUid + ", " + itemUid + ")");
    }

    return _checker.checkOriginalStatus(transactionUid, itemUid, true);
}

/**
 * get the current status in the original process, given the uid of the contact
 * item (which is the uid of the process). Note that this method is used by the
 * GenericRecoveryCoordinator code only.
 */

public static Status get_current_status(Uid transactionUid, Uid itemUid) throws Inactive
{
    if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("StatusChecker.get_current_status(" + transactionUid + ", " + itemUid + ")");
    }

    return _checker.checkOriginalStatus(transactionUid, itemUid, false);
}

/**
 * why isn't this private
 */
public StatusChecker()
{
    _itemFromUid = new Hashtable();
}


/**
 * Check the status of a transaction when the contact item uid is known.
 * This method *must* only be called from replay_completion, since it
 * relies upon this fact to differentiate between a committed or rolled back
 * transaction in the event of finding no intentions list in the object
 * store.
 *
 * @return the status of the transaction as known in the original process.
 * @throws Inactive if the original process is no longer active.
 */

public Status checkOriginalStatus (Uid transactionUid, Uid itemUid, boolean checkTheObjectStore)
    throws Inactive
{
    if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("StatusChecker.checkOriginalStatus(" + transactionUid + ", " + itemUid + ", " + checkTheObjectStore + ")");
    }

    FactoryContactItem item = getItem(itemUid);

    if (item != null)
    {
	return getStatus(transactionUid, item, checkTheObjectStore);
    }
    else
    {
	// null item implies long-dead process
	throw new Inactive();
    }
}

/**
 *  try to get the status from a factory and convert to our way.
 *  factory must not be null
 *  itemUid is the store id as in _itemFromUid
 */

private Status getStatus (Uid transactionUid, FactoryContactItem item, boolean checkTheObjectStore) throws Inactive
{
    if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("StatusChecker.getStatus(" + transactionUid + ", " + item + ", " + checkTheObjectStore + ")");
    }

    ArjunaFactory factory = item.getFactory();

    if (factory != null)
    {
	Status otsStatus = Status.StatusUnknown;
	boolean originalDead = false;

	try
	{
	    otid_t otid = Utility.uidToOtid(transactionUid);

	    otsStatus = factory.getCurrentStatus(otid);

	    if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("StatusChecker.getStatus("+transactionUid+") - current status = "+Utility.stringStatus(otsStatus));
        }

	    /*
	     * If the factory doesn't know about the transaction, then
	     * check the object store for the intentions list. If not
	     * present, then the transaction must have rolled back.
	     * If present, then we don't know what's going on, since the
	     * factory should still have a reference to the transaction!
	     */

	    if (otsStatus == Status.StatusNoTransaction)
	    {
		otsStatus = factory.getStatus(otid);

		if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("StatusChecker.getStatus("+transactionUid+") - stored status = "+Utility.stringStatus(otsStatus));
        }

		switch (otsStatus.value())
		{
		case Status._StatusNoTransaction:
		    /*
		     * A definitive NoTransaction means rolled back because of
		     * presumed abort protocol.
		     */

		    //		    return Status.StatusRolledBack;
		    return otsStatus;
		case Status._StatusUnknown:
		    return otsStatus;
		default: {
            /*
                * We got an answer! This probably means that the
                * factory has just finished with the transaction, but
                * the state hasn't been removed by the file system yet
                * - we don't sync the removal to improve performance.
                */

            jtsLogger.i18NLogger.warn_recovery_contact_StatusChecker_3(transactionUid);

            otsStatus = Status.StatusUnknown;
        }
		    break;
		}
	    }

	    if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("StatusChecker.getStatus("+transactionUid+") - Status = "+Utility.stringStatus(otsStatus));
        }

	    item.markAsAlive();
	} catch ( NO_IMPLEMENT ex_noimp) {
	    // the original application has died

	    if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("StatusChecker.getStatus("+transactionUid+") - NO_IMPLEMENT = dead");
        }

	    originalDead = true;

	// orbix seems to count unreachable as transient. Over infinite time, all
	// addresses are valid
	} catch ( TRANSIENT ex_trans) {

	    if (ORBInfo.getOrbEnumValue() == ORBType.JACORB)
	    {
		    // the original application has (probably) died
		    if (jtsLogger.logger.isDebugEnabled()) {
                jtsLogger.logger.debug("StatusChecker.getStatus("+transactionUid+") - TRANSIENT = dead");
            }
		    originalDead = true;
	    }

	} catch ( COMM_FAILURE ex_comm) {
	    /*
	     * Probably the original application has died, but only just - do
	     * not mark either way.
	     */
	    if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("StatusChecker.getStatus("+transactionUid+") - COMM_FAILURE = live");
        }

	} catch ( OBJECT_NOT_EXIST ex_noobj) {
	    // the original process must have gone away, and another one
	    // come up in the same place
	    // (or, just possibly, the original closed the ots)
	    originalDead = true;
	    if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("StatusChecker.getStatus("+transactionUid+") - OBJECT_NOT_EXIST = dead");
        }

	} catch ( BAD_PARAM ex_badparam) {
        jtsLogger.i18NLogger.warn_recovery_contact_StatusChecker_9();
	    // the transactionUid is invalid !
	} catch ( NoTransaction ex_notran) {
        jtsLogger.i18NLogger.warn_recovery_contact_StatusChecker_10();
	    // the transactionUid is invalid !
	    // no transaction
	} catch ( SystemException ex_corba ) {
	    // why did this happen ?
        jtsLogger.i18NLogger.warn_recovery_contact_StatusChecker_11(ex_corba);
	} catch ( Exception ex_other) {
	    // this really shouldn't happen
        jtsLogger.i18NLogger.warn_recovery_contact_StatusChecker_12(ex_other);
	}

	if (originalDead)
	{
	    item.markAsDead();

	    // use Inactive as an indication that the parent process
	    // has gone

	    throw new Inactive();
	}
	else
	{
	    return otsStatus;
	}
    }
    else
    {
	// factory in item is null - process already dead
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("StatusChecker.getStatus("+transactionUid+") -  no factory, process previously dead");
    }

	/*
	 * In which case we can use the current, in process local factory, to
	 * look at the object store and get the status from that. At present
	 * all factories can look at the entire object store on a machine, so
	 * this will work. If a factory is limited to only a portion of the object
	 * store then we may need to create an explicit factory that has "global"
	 * knowledge.
	 */

        if ( checkTheObjectStore )
        {
            try
            {
		Status s = OTSManager.factory().getStatus(transactionUid);

		/*
		 * If the status is committing or rolling back from a dead
		 * (local) process then we can direct recovery now.
		 */

		if (s == Status.StatusCommitting)
		    return Status.StatusCommitted;
		else
		{
		    if (s == Status.StatusRollingBack)
			return Status.StatusRolledBack;
		}

		return s;
            }
            catch (NoTransaction e1)
            {
                return Status.StatusNoTransaction;
            }
            catch (SystemException e2)
            {
                return Status.StatusUnknown;
            }
        }
        else
        {
            throw new Inactive();
        }
    }
}


/**
 * find the IOR for the ArjunaFactory whose FactoryContactItem was saved with
 * this uid. It is possible this Uid was created after the last scan, so if
 * it isn't in the hashtable, look for real directly.
 */

private FactoryContactItem getItem (Uid uid)
{
    FactoryContactItem theItem = null;
    theItem = getKnownItem(uid);
    if (theItem == null) {
	// not previously known - see if it exists now
	theItem = getNewItem(uid);

	if (theItem == null) {

        // if it's still null, either something has gone wrong
        // - how did it get in the recoverycoordkey when the
        //   factory was unknown
        // or it's very old and been fully deleted

        jtsLogger.i18NLogger.warn_recovery_contact_StatusChecker_14(uid);
        // treat as long-dead process - return null
    }
    }

    return theItem;
}

private FactoryContactItem getKnownItem(Uid uid)
{
    FactoryContactItem theItem = null;

    try {
	theItem = (FactoryContactItem) _itemFromUid.get(uid);
	return theItem;
    } catch (ClassCastException ex) {
        jtsLogger.i18NLogger.warn_recovery_contact_StatusChecker_15(uid, ex);
        return null;
    }
}

private FactoryContactItem getNewItem (Uid uid)
{
    FactoryContactItem item = FactoryContactItem.recreate(uid);
    if (item != null) {
	// enter in the uid hashtable
	_itemFromUid.put(uid,item);
    }
    return item;
}


}
