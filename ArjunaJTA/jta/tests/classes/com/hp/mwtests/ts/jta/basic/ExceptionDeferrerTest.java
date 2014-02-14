package com.hp.mwtests.ts.jta.basic;
import static org.junit.Assert.*;

import javax.transaction.HeuristicMixedException;
import javax.transaction.RollbackException;
import javax.transaction.xa.XAException;

import org.junit.Test;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.hp.mwtests.ts.jta.common.DummyXA;
import com.hp.mwtests.ts.jta.common.FailureXAResource;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailLocation;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailType;

/**
 * 
 * @author sebplorenz
 *
 */
public class ExceptionDeferrerTest {

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
   public void testCheckDeferredHeuristicRollbackSecondResourceFails() throws Exception
   {
       ThreadActionData.purgeActions();
       
       TransactionImple tx = new TransactionImple(0);

       try
       {
          tx.enlistResource(new DummyXA(true));
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
       catch (final HeuristicMixedException ex)
       {
          assertEquals(XAException.XA_HEURRB, ((XAException) ex.getSuppressed()[0]).errorCode);
       }
   }
   
   @Test
   public void testCheckDeferredHeuristicRollbackFirstResourceFails() throws Exception
   {
       ThreadActionData.purgeActions();
       
       TransactionImple tx = new TransactionImple(0);

       try
       {
          tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.rollback));
          tx.enlistResource(new DummyXA(true));
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

}
