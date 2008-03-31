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

package com.arjuna.ats.internal.jts;

import org.omg.CosTransactions.*;
import org.omg.CORBA.SystemException;

import com.arjuna.ArjunaOTS.*;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.utils.Helper;
import com.arjuna.ats.arjuna.common.Uid;

/**
 * a wrapper used to wrap a ControlImple or a Control when attempting to remove it from the TransactionReaper
 * transactions list. this wrapper ensures that hashcode and equals calls compare correctly while performing
 * as few remote invocations as possible to do the comparison.
 */
public class PseudoControlWrapper
{
    // public API

    /**
     * wrap a Control in a wrapper which can be used to test for equality against ControlWrapper instances
     * n.b. the Control must have been determined to be a non-local control by the caller
     *
     * @param control
     */
    public PseudoControlWrapper(Control control)
    {
        _coordinator = null;    // for non-local control -- only compute when needed
        _control = control;
        _uid = null;            // for non-local control -- only compute when needed
        _local = null;
        _hashCode = computeHashCode();
    }

    /**
     * wrap a ControlImple in a wrapper which can be used to test for equality against ControlWrapper instances
     *
     * @param controlImple
     */
    public PseudoControlWrapper(ControlImple controlImple)
    {
        _coordinator = null;    // for non-local control -- only compute when needed
        _control = null;
        _uid = null;            // for non-local control -- only compute when needed
        _local = controlImple;
        _hashCode = computeHashCode();
    }

    /**
     * this returns the same hashcode as ControlWrapper for a given Control or ControlImple
     *
     * @return
     */
    public int hashCode()
    {
        return _hashCode;
    }

    /**
     * Test whether this PseudoControlWrapper wraps the same Control as a ControlWrapper
     *
     * The equality rules differ depending upon whether this wraps a local or remote Control
     *
     * if the wrapped control is local then the local uid can be compared against the uid cached in the ControlWrapper
     *
     * if the wrapped control is non-local then the test depends upon whether the ControlWrapper wraps a local
     * or remote control. in the former case a uid comparison can be used. in the latter case the coordinators of
     * the two controls must be obtained and a call to coord.is_same_transaction(othercoord) must be used.
     *
     * @caveats this comparison is only defined against instances of ControlWrapper because this class should only be
     * used to wrap a control for comparison against an entry in the TransactionReaper's list of controls and should
     * only compare equal to an entry which is a ControlWrapper for the same Control. as such it breaks all the
     * rules for an equals implementation and should not be expected to work anywhere else.
     *
     *
     */
    public boolean equals(Object o)
    {
        if (o instanceof ControlWrapper) {
            ControlWrapper wrapper = (ControlWrapper)o;

            if (_local != null) {
                // the other guys uid will already have been computed so just do a uid -- uid comparison
                return _local.get_uid().equals(wrapper.get_uid());
            } else if (_control == null) {
                return false;
            } else if (wrapper.isLocal()) {
                // the other control is local so a UID comparison must be ok
                try
                {
                    // make sure we have our uid
                    if (_uid == null) {
                        _uid = computeUid();
                    }
                    return _uid.equals(wrapper.get_uid());
                } catch (Exception e) {
                    return false;
                }
            } else {
				/*
				 * Trying to compare two non-local controls -- assuming the previous implementation was correct we
				 * need to do a full comparison of this control's coordinator against the wrapper control's coordinator
				 * TODO why can't we just get the two UIDs and compare them?
				 */
                try {
                    // make sure we have our coordinator
                    if (_coordinator == null) {
                        _coordinator = computeCoordinator();
                    }
                    return _coordinator.is_same_transaction(wrapper.get_coordinator());
                } catch (Exception e) {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * fetch the coordinator for the wrapped non-local control
     * @return the remote coordinator
     * @throws Exception if the coordinator cannot be obtained e.g. because the control is no longer valid
     */
    private Coordinator computeCoordinator()
            throws Exception
    {
        return _control.get_coordinator();

    }

    /**
     * fetc the uid for the wrapped non-local control
     * @return the uid
     * @throws Exception if the coordinator cannot be obtained e.g. because the control is no longer valid
     */
    private Uid computeUid()
            throws Exception
    {
        if (_coordinator == null) {
            _coordinator = computeCoordinator();
        }

        UidCoordinator uidCoord = Helper.getUidCoordinator(_coordinator);

        return Helper.getUid(uidCoord);
    }

    /**
     * compute the hashcode for the wrapped local or remote control using the same algorithm as ControlWrapper
     * @return the hashcode
     */
    private int computeHashCode()
    {
        try {
            if (_local != null) {
                return _local.getImplHandle().hash_transaction();
            } else if (_control != null) {
                Coordinator coord = _control.get_coordinator();

                return coord.hash_transaction();
            }
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * the wrapped local control or null if the control is non-local
     */
    private ControlImple _local;
    /**
     * the wrapped non-local control or null if the control is local
     */
    private Control _control;
    /**
     * coordinator for the wrapped non-local control computed lazily as needed
     */
    private Coordinator _coordinator;
    /**
     * uid for the wrapped non-local control computed lazily as needed
     */
    private Uid _uid;
    /**
     * hashcode for the wrapped control
     */
    private int _hashCode;
}
