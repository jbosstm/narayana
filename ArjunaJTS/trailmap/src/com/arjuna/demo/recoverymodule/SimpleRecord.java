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
 * Copyright (C) 2003, 2004
 * Arjuna Technologies Limited
 * Newcastle upon Tyne, UK
 *
 * $Id: SimpleRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.demo.recoverymodule;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;

import java.io.File;

/**
 * ACHTUNG!: Implementing an <CODE>AbstractRecord</CODE> is a very advanced feature of the transaction service. It should
 * only be performed by users familiar with the all the concepts used in the JBoss Transactions product. Please see the
 * ArjunaCore guide for more information about <CODE>AbstractRecord</CODE>s.
 *
 * An <CODE>AbstractRecord</CODE> is more generic than an XAResource. In the JBoss Transactions product an XAResource
 * is wrapped by an <CODE>AbstractRecord</CODE>. Briefly a programmer may wish to implement their own
 * <CODE>AbstractRecord</CODE> for any of the following reasons:
 * <ol>
 *  <li>Order the transaction calls on their resources. For example, this would allow the programmer to release a lock
 *          after all resources have prepared</li>
 *  <li>Manipulate data with custom persistence/recovery requirements</li>
 *  <li>Take advantage of the nested-transaction aware capabilities</li>
 *  <li>Allow the programmer to merge Records where possible or indicate that records need/need not be added</li>
 *  <li>Use the advanced features offered by implementing your own persistence techniques to offer compensation-oriented
 *          transactions. For example prepare=commit and rollback=compensate</li>
 * </ol>
 *
 * The simple record class shows how the programmer can extend the <CODE>AbstractRecord</AbstractRecord> class to provide
 * their own implementation. This class is very simple, most of the implementations are no-ops. However, the
 * documentation for this class is quite rich and gives pointers as to how an implementor could create a more
 * sophisticated record.
 */
public class SimpleRecord extends AbstractRecord
{
    /**
     * The name of the file used to persist information about this record. It is package local scope so that the
     * <CODE>SimpleRecoveryModule</CODE> can easily find the name of the file to check.
     */
    static String filename = "./RecordState";

    /**
     * Whether this resource is configured to crash or not.
     */
    private boolean m_shouldCrash = false;

    /**
     * Construct a new SimpleRecord. This will initialize whether to crash or not and then will display information
     * about the record and its storage location.
     *
     * @param shouldCrash   Tells the record if it is expected to crash during commit.
     */
    public SimpleRecord(boolean shouldCrash)
    {
        m_shouldCrash = shouldCrash;
        System.out.println("Creating new resource.");
        System.out.println("Data will be persisted in a file at: " + filename);
        System.out.println("The resource " + (m_shouldCrash ? "is" : "is not") + " configured to crash");
    }

    /**
     * This is called by the recovery subsystem to determine the order of transaction management invocations to perform.
     *
     * @return  That this is the first user-defined Record type and as such should be invoked before all other system-
     *          defined Records or other user-defined records.
     *
     * @see     RecordType
     */
    public int typeIs()
    {
        return RecordType.USER_DEF_FIRST0;
    }

    // -----------------------------------------------------------------------------------------------------------------
    //      TRANSACTION MANAGEMENT CALLS
    //          From <CODE>AbstractRecord</CODE>
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * This call can be used by a nested transaction aware resource to propogate information to its parent to inform it
     * of the descision to rollback.
     *
     * @return A state from the class <CODE>TwoPhaseOutcome</CODE indicating the response of this Record.
     *
     * @see TwoPhaseOutcome
     */
    public int nestedAbort()
    {
        return TwoPhaseOutcome.FINISH_OK;
    }

    /**
     * This call can be used by a nested transaction aware resource to propogate information to its parent to inform it
     * of the descision to commit. The parent could then make durable the information it received this resource passed
     * it at prepare time.
     *
     * @return A state from the class <CODE>TwoPhaseOutcome</CODE indicating the response of this Record.
     *
     * @see TwoPhaseOutcome
     */
    public int nestedCommit()
    {
        return TwoPhaseOutcome.FINISH_OK;
    }

    /**
     * This call can be used by a nested transaction aware resource to propogate information to its parent to inform it
     * of the descision to prepare. The parent could then access this information ready for the nestedCommit().
     *
     * @return A state from the class <CODE>TwoPhaseOutcome</CODE indicating the response of this Record.
     *
     * @see TwoPhaseOutcome
     */
    public int nestedPrepare()
    {
        return TwoPhaseOutcome.PREPARE_OK;
    }

    /**
     * This implementation of <CODE>AbstractRecord</CODE> simply deletes the record file.
     *
     * @return A state from the class <CODE>TwoPhaseOutcome</CODE indicating the response of this Record.
     *
     * @see TwoPhaseOutcome
     */
    public int topLevelAbort()
    {
        try
        {
            File fd = new File(filename);
            if (fd.exists())
            {
                if (fd.delete())
                    System.out.println("File Deleted");
            }
        }
        catch (Exception ex)
        {
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    /**
     * This implementation of <CODE>AsbtractRecord</CODE> allows the user to indicate if it should crash or not. If it
     * chooses to crash the VM is signalled to terminate, alternatively if it is to proceed as normal then the file
     * created at prepare time is "commited" (by writing more text into it).
     *
     * @return A state from the class <CODE>TwoPhaseOutcome</CODE indicating the response of this Record.
     *
     * @see TwoPhaseOutcome
     */
    public int topLevelCommit()
    {
        if (m_shouldCrash)
            System.exit(0);

        if (!new File(filename).exists())
        {
            // This should never happen in this example so we do not handle the case, however this could indicate that
            // the prepare had not been called and would be an error
            // The resource should return an error flag, for example: TwoPhaseOutcome.NOT_PREPARED
        }

        try
        {
            java.io.FileOutputStream file = new java.io.FileOutputStream(filename);
            java.io.PrintStream pfile = new java.io.PrintStream(file);
            pfile.println("I'm Committed");
            file.close();
        }
        catch (java.io.IOException ex)
        {
            System.out.println("File error");
            // if topLevelCommit fails then a flag should be returned depending upon the nature of the failure.
            // If it is an unrecoverable failure, then the resource should return one of the heuristic errors
            // (mixed or hazard). If the record has rolled back or committed, then it'll be heuristic rollback
            // or commit respectively. If it's something that can be fixed by recovery, then FINISH_ERROR.

            // For this implementation we do nothing
        }
        return TwoPhaseOutcome.FINISH_OK;
    }

    /**
     * This implementation of top level prepare simply creates a new file and writes some text into it indicating that
     * the transaction is preparing.
     *
     * @return A state from the class <CODE>TwoPhaseOutcome</CODE indicating the response of this Record.
     *
     * @see TwoPhaseOutcome
     */
    public int topLevelPrepare()
    {
        try
        {
            java.io.FileOutputStream file = new java.io.FileOutputStream(filename);
            java.io.PrintStream pfile = new java.io.PrintStream(file);
            pfile.println("I'm prepared");
            file.close();
        }
        catch (java.io.IOException ex)
        {
            System.out.println("File error");
        }
        return TwoPhaseOutcome.PREPARE_OK;
    }

    /**
     * If this abstract record caused a heuristic then it should return an object which implements
     * <CODE>HeuristicInformation</CODE>.
     *
     * @return An object implementing <CODE>HeuristicInformation</CODE>.
     *
     * @see com.arjuna.ats.arjuna.coordinator.HeuristicInformation
     */
    public Object value()
    {
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------
    //      RESOURCE MANAGEMENT CALLS
    //          From <CODE>AbstractRecord</CODE>
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Merge the current record with the one presented.
     *
     * @param abstractRecord    The record with which to alter.
     */
    public void alter(AbstractRecord abstractRecord)
    {
    }

    /**
     * Merge the current record with the one presented.
     *
     * @param abstractRecord    The record with which to merge.
     */
    public void merge(AbstractRecord abstractRecord)
    {
    }

    /**
     * Indicates if the the record presented should be added to the intentions list.
     *
     * @param abstractRecord    The record in question.
     *
     * @return  True if the record should be added, otherwise.
     */
    public boolean shouldAdd(AbstractRecord abstractRecord)
    {
        return false;
    }

    /**
     * Indicates if we should alter the current record with the one presented.
     *
     * @param abstractRecord    The record in question.
     *
     * @return                  True if the record should be altered by this record, otherwise false.
     */
    public boolean shouldAlter(AbstractRecord abstractRecord)
    {
        return false;
    }

    /**
     * Indicates if we should merge the current record with the one presented.
     *
     * @param abstractRecord    The record to try to merge.
     * @return                  True if the record should be merged with this record, false otherwise.
     */
    public boolean shouldMerge(AbstractRecord abstractRecord)
    {
        return false;
    }

    /**
     * Should we replace the record presented with the current record? Before a record is added to the intentions list
     * it queries each current record to check if it should replace it.
     *
     * @param abstractRecord    The record to try to replace.
     * @return                  True, if the record should be replaced, false otherwise.
     */
    public boolean shouldReplace(AbstractRecord abstractRecord)
    {
        return false;
    }

    /**
     * This can be used during merge to assign a different value to the object. This is only required for Records
     * aware of each other.
     *
     * @param object    The value to merge into this object.
     */
    public void setValue(Object object)
    {
    }
}
