package com.hp.mwtests.ts.jta.basic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.RollbackException;
import javax.transaction.xa.XAException;

import org.junit.Test;

import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.hp.mwtests.ts.jta.common.FailureXAResource;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailLocation;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailType;
import com.hp.mwtests.ts.jta.common.TestResource;

/**
 * 
 * @author sebplorenz
 *
 */
public class ExceptionDeferrerTest {

    @Test
    public void testCheckDeferredPrepareRollbackException () throws Exception
    {
        ThreadActionData.purgeActions();

        TransactionImple tx = new TransactionImple(0);


        tx.enlistResource(new FailureXAResource());

        try
        {
            tx.enlistResource(new FailureXAResource(FailLocation.prepare, FailType.rollback));
        }
        catch (final RollbackException ex)
        {
            fail();
        }

        try
        {
            tx.commit();

            fail();
        }
        catch (final RollbackException ex)
        {
            assertEquals(XAException.XAER_INVAL, ((XAException) ex.getSuppressed()[0]).errorCode);
        }
    }

    @Test
    public void testCheckDeferredPrepareInitCause () throws Exception
    {
        ThreadActionData.purgeActions();

        TransactionImple tx = new TransactionImple(0);


        tx.enlistResource(new FailureXAResource());

        try
        {
            tx.enlistResource(new FailureXAResource(FailLocation.prepare, FailType.message));
        }
        catch (final RollbackException ex)
        {
            fail();
        }

        try
        {
            tx.commit();

            fail();
        }
        catch (final RollbackException ex)
        {
            assertEquals(XAException.XA_RBROLLBACK, ((XAException) ex.getSuppressed()[0]).errorCode);
            assertEquals("test message", ((XAException) ex.getSuppressed()[0]).getCause().getMessage());
        }
    }

    @Test
    public void testCheckDeferredPrepareHeuristic() throws Exception {
        ThreadActionData.purgeActions();

        TransactionImple tx = new TransactionImple(0);

        tx.enlistResource(new FailureXAResource());

        try {
            tx.enlistResource(new FailureXAResource(FailLocation.prepare, FailType.XA_HEURHAZ));
        } catch (final RollbackException ex) {
            fail();
        }

        try {
            tx.commit();

            fail();
        } catch (final HeuristicMixedException ex) {
            assertEquals(XAException.XA_HEURHAZ, ((XAException) ex.getSuppressed()[0]).errorCode);
        }
    }

   @Test
   public void testCheckDeferredRollbackException () throws Exception
   {
       ThreadActionData.purgeActions();
       
       TransactionImple tx = new TransactionImple(0);

       try
       {
           tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.rollback));
       }
       catch (final RollbackException ex)
       {
          fail();
       }
       
       try
       {
           tx.commit();
           
           fail(); 
       }
       catch (final RollbackException ex)
       {
          assertEquals(XAException.XA_HEURRB, ((XAException) ex.getSuppressed()[0]).errorCode);
       }
   }
   
   @Test
   public void testCheckDeferredHeuristicException () throws Exception
   {
       ThreadActionData.purgeActions();
       
       TransactionImple tx = new TransactionImple(0);

       try
       {
           tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.normal));
       }
       catch (final RollbackException ex)
       {
          fail();
       }
       
       try
       {
           
           tx.commit();
           
           fail(); 
       }
       catch (final HeuristicMixedException ex)
       {
          assertEquals(XAException.XA_HEURMIX, ((XAException) ex.getSuppressed()[0]).errorCode);
       }
   }
   
   @Test
   public void testCheckDeferredHeuristicRollbackFirstResourceFails() throws Exception
   {
       ThreadActionData.purgeActions();
       TxControl.setXANodeName("test");
       TransactionImple tx = new TransactionImple(500);

       try
       {
          tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.nota));
          tx.enlistResource(new TestResource());
       }
       catch (final RollbackException ex)
       {
          fail();
       }
       
       try
       {
           tx.commit();
           
           fail(); 
       }
       catch (final HeuristicMixedException ex)
       {
          assertEquals(XAException.XAER_NOTA, ((XAException) ex.getSuppressed()[0]).errorCode);
       }
   }
   
   @Test
   public void testCheckDeferredHeuristicRollbackSecondResourceFails() throws Exception
   {
       ThreadActionData.purgeActions();
       TxControl.setXANodeName("test");
       TransactionImple tx = new TransactionImple(500);

       try
       {
          tx.enlistResource(new TestResource());
          tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.nota));
       }
       catch (final RollbackException ex)
       {
          fail();
       }
       
       try
       {
           tx.commit();
           
           fail(); 
       }
       catch (final HeuristicMixedException ex)
       {
          assertEquals(XAException.XAER_NOTA, ((XAException) ex.getSuppressed()[0]).errorCode);
       }
   }
   
   @Test
   public void testCheckDeferredHeuristicRollbackSecondOfThreeFails() throws Exception
   {
      ThreadActionData.purgeActions();
      TxControl.setXANodeName("test");
      TransactionImple tx = new TransactionImple(500);

      try
      {
         tx.enlistResource(new TestResource());
         tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.nota));
         tx.enlistResource(new TestResource());
      }
      catch (final RollbackException ex)
      {
         fail();
      }
      
      try
      {
          tx.commit();
          
          fail(); 
      }
      catch (final HeuristicMixedException ex)
      {
         assertEquals(XAException.XAER_NOTA, ((XAException) ex.getSuppressed()[0]).errorCode);
      }
   }

}
