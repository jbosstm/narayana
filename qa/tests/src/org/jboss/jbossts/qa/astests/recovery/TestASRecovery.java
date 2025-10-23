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
package org.jboss.jbossts.qa.astests.recovery;

import javax.transaction.xa.XAException;
import javax.transaction.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for manually starting a transaction and enlisting various resources
 * and synchronizations.
 */
public class TestASRecovery
{
    List<ASTestResource> resources = new ArrayList<ASTestResource> ();
    private boolean expectException;

    public void addResource(ASFailureSpec spec)
    {
        resources.add(new ASTestResource(spec));
    }

    /**
     * See if there are any faults that should be injected before starting the
     * commit protocol
     */
    private void preCommit()
    {
        for (ASTestResource spec : resources)
        {
            if (spec.isPreCommit())
                try
                {
                    spec.applySpec("Pre commit", true);
                }
                catch (XAException ignore)
                {
                }
        }
    }

    public boolean startTest(Transaction tx)
    {
        try
        {
            for (ASTestResource res : resources)
            {
                System.out.println("Enlisting " + res);

                if (res.isXAResource())
                    tx.enlistResource(res);
                else if (res.isSynchronization())
                    tx.registerSynchronization(res);

                if (res.expectException())
                    expectException = true;
            }

            preCommit();

            return true;
        }
        catch (RollbackException e)
        {
            e.printStackTrace();
        }
        catch (SystemException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public boolean startTest()
    {
        UserTransaction ut = com.arjuna.ats.jta.UserTransaction.userTransaction();

        try
        {
            ut.begin();

            if (!startTest(com.arjuna.ats.jta.TransactionManager.transactionManager().getTransaction()))
                ut.rollback();
            else
            {                
                ut.commit();
                
                return !expectException;
            }
        }
        catch (Exception e)
        {
            if (expectException)
                return true; // TODO should check each specific exception type
            
            e.printStackTrace();
        }

        return false;
    }
}
