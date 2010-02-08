/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */

package com.arjuna.ats.internal.arjuna.tools.log;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.RecordListIterator;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;

public class EditableAtomicAction extends AtomicAction implements EditableTransaction  // going to have to be one per action type because of state differences
{
    public EditableAtomicAction (final Uid u)
    {
        super(u);
   
        _activated = activate();
        
        if (!_activated)
            System.err.println("Transaction "+u+" and "+type()+" not activated.");
    }
    
    /**
     * Move a previous heuristic participant back to the prepared list so that recovery
     * can try again. If it fails again then it may end up back on the heuristic list.
     */
    
    public void moveHeuristicToPrepared (int index) throws IndexOutOfBoundsException
    {
        if ((index < 0) || (super.heuristicList.size() < index))
            throw new IndexOutOfBoundsException();
        else
        {
            if (super.heuristicList.size() == 0)
                throw new IndexOutOfBoundsException();
            
            RecordListIterator iter = new RecordListIterator(super.heuristicList);
            AbstractRecord rec = iter.iterate();
            
            for (int i = 0; i < index; i++)
                rec = iter.iterate();

            /*
             * Before we move we need to tell the resource that is can forget about the heuristic.
             * This assumes that a) the user knows what they are doing, and b) the resource can then
             * be driven through commit once more. If either of those assumptions is incorrect then
             * we could be in a world of pain!
             */
            
            if (rec.forgetHeuristic())
            {
                /*
                 * Move from heuristic list to prepared list.
                 */
                
                super.heuristicList.remove(rec);           
                super.preparedList.insert(rec);
                
                /*
                 * If the list is zero then the heuristic has gone away!
                 * 
                 * We don't maintain a per-resource heuristic so we cannot change
                 * the heuristic each time we remove a resource, i.e., we have to
                 * assume that the original heuristic applies to all of the remaining
                 * participants.
                 */
                
                if (super.heuristicList.size() == 0)
                    super.setHeuristicDecision(TwoPhaseOutcome.FINISH_OK);
                
                super.updateState();
            }
            else
                System.err.println("Error - could not get resource to forget heuristic. Left on Heuristic List.");
        }
    }
    
    /**
     * Delete a heuristic participant from the list.
     */
    
    public void deleteHeuristicParticipant (int index) throws IndexOutOfBoundsException
    {
        if ((index < 0) || (super.heuristicList.size() < index))
            throw new IndexOutOfBoundsException();
        else
        {
            if (super.heuristicList.size() == 0)
                throw new IndexOutOfBoundsException();
            
            RecordListIterator iter = new RecordListIterator(super.heuristicList);
            AbstractRecord rec = iter.iterate();
            
            for (int i = 0; i < index; i++)
                rec = iter.iterate();

            super.heuristicList.remove(rec);
            
            /*
             * If the list is zero then the heuristic has gone away!
             */
            
            if (super.heuristicList.size() == 0)
                super.setHeuristicDecision(TwoPhaseOutcome.FINISH_OK);
            
            // if the log is not entry this call will delete the log automatically.
            
            super.updateState();
        }
    }
    
    public String toString ()
    {
        if (!_activated)
            return "RecoveryAction not activated.";
        else
        {
            String printableForm = "ActionStatus: "+ActionStatus.stringForm(super.status());
            
            printableForm += "\nHeuristic Decision: "+TwoPhaseOutcome.stringForm(super.getHeuristicDecision());
            
            if (super.preparedList.size() == 0)
                printableForm += "\nNo prepared entries.";
            else
            {
                printableForm += "\nPrepared entries:";
                
                RecordListIterator iter = new RecordListIterator(super.preparedList);
                AbstractRecord rec = iter.iterate();
                int i = 0;
                
                while (rec != null)
                {
                    printableForm += "\n["+i+"] "+rec;
                    
                    rec = iter.iterate();
                    i++;
                }
            }
            
            if (super.heuristicList.size() == 0)
                printableForm += "\nNo heuristic entries.";
            else
            {
                printableForm += "\nHeuristic entries:";
                
                RecordListIterator iter = new RecordListIterator(super.heuristicList);
                AbstractRecord rec = iter.iterate();
                int i = 0;
                
                while (rec != null)
                {
                    printableForm += "\n["+i+"] "+rec;
                    
                    rec = iter.iterate();
                    i++;
                }
            }
            
            return printableForm;
        }
    }
    
    private boolean _activated;
}