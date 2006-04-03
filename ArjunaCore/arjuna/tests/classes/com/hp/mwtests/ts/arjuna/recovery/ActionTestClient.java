/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
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
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.arjuna.recovery;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ActionTestClient.java 2342 2006-03-30 13:06:17Z  $
 */

import java.io.* ;
import java.net.* ;
import java.util.* ;

import com.arjuna.ats.arjuna.AtomicAction ;
import com.arjuna.ats.arjuna.coordinator.ActionStatus ;
import com.arjuna.ats.arjuna.coordinator.AddOutcome ;
import com.arjuna.ats.arjuna.common.Uid ;
import com.arjuna.ats.arjuna.coordinator.ActionStatus ;
import com.arjuna.ats.arjuna.recovery.Service ;
import com.arjuna.ats.arjuna.utils.Utility ;
import com.arjuna.ats.internal.arjuna.recovery.Listener ;
import com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector ;
import com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem ;
import com.arjuna.mwlabs.testframework.unittest.Test;
import com.hp.mwtests.ts.arjuna.objectstore.ObjectStoreTest;

class ActionTestClientTestService implements Service
{
   private void test1()
   {
      try
      {
         String test_tran_type_1 = _in.readLine() ;
         String test_uid_1_str   = _in.readLine() ;

         Uid test_uid_1 = new Uid( test_uid_1_str ) ;

         int test_status1 = _tsc.getTransactionStatus( test_tran_type_1, test_uid_1 ) ;
         int test_status2 = _tsc.getTransactionStatus( "", test_uid_1 ) ;

         _out.println( "OK" ) ;
         _out.flush() ;

         test_tran_type_1 = _in.readLine() ;
         test_uid_1_str   = _in.readLine() ;
         test_uid_1 = new Uid( test_uid_1_str ) ;

         int test_status3 = _tsc.getTransactionStatus( test_tran_type_1, test_uid_1 ) ;
         int test_status4 = _tsc.getTransactionStatus( "", test_uid_1 ) ;

         _out.println ( "OK" ) ;
         _out.flush() ;

         test_tran_type_1 = _in.readLine() ;
         test_uid_1_str   = _in.readLine() ;
         test_uid_1 = new Uid( test_uid_1_str ) ;

         int test_status5 = _tsc.getTransactionStatus( test_tran_type_1, test_uid_1 ) ;
         int test_status6 = _tsc.getTransactionStatus( "", test_uid_1 ) ;

         _out.println( "OK" ) ;
         _out.flush() ;

         if ( ( test_status1 == ActionStatus.ABORTED )   &&
              ( test_status2 == ActionStatus.ABORTED )   &&
              ( test_status3 == ActionStatus.RUNNING )   &&
              ( test_status4 == ActionStatus.RUNNING )   &&
              ( test_status5 == ActionStatus.COMMITTED ) &&
              ( test_status6 == ActionStatus.COMMITTED ) )
         {
            System.out.println( _unit_test + "test1: passed" ) ;
            _tests_passed++ ;
         }
         else
         {
            System.out.println( _unit_test + "test1: failed" ) ;
            _tests_failed++ ;
         }
      }
      catch ( IOException ex )
      {
         System.err.println( _unit_test + "test1: failed " + ex ) ;
      }
   }

   private void test2()
   {
      try
      {
         String test_tran_type_2 = _in.readLine() ;
         String test_uid_2_str   = _in.readLine() ;
         Uid test_uid_2 = new Uid( test_uid_2_str ) ;

         int test_status1 = _tsc.getTransactionStatus( test_tran_type_2, test_uid_2 ) ;
         int test_status2 = _tsc.getTransactionStatus( "", test_uid_2 ) ;

         _out.println ( "OK" ) ;
         _out.flush() ;

         test_tran_type_2 = _in.readLine() ;
         test_uid_2_str   = _in.readLine() ;
         test_uid_2 = new Uid( test_uid_2_str ) ;

         int test_status3 = _tsc.getTransactionStatus( test_tran_type_2, test_uid_2 ) ;
         int test_status4 = _tsc.getTransactionStatus( "", test_uid_2 ) ;

         _out.println ( "OK" ) ;
         _out.flush() ;

         test_tran_type_2 = _in.readLine() ;
         test_uid_2_str   = _in.readLine() ;
         test_uid_2 = new Uid( test_uid_2_str ) ;

         int test_status5 = _tsc.getTransactionStatus( test_tran_type_2, test_uid_2 ) ;
         int test_status6 = _tsc.getTransactionStatus( "", test_uid_2 ) ;

         _out.println( "OK" ) ;
         _out.flush() ;

         if ( ( test_status1 == ActionStatus.ABORTED ) &&
              ( test_status2 == ActionStatus.ABORTED ) &&
              ( test_status3 == ActionStatus.RUNNING ) &&
              ( test_status4 == ActionStatus.RUNNING ) &&
              ( test_status5 == ActionStatus.ABORTED ) &&
              ( test_status6 == ActionStatus.ABORTED ) )
         {
            System.out.println( _unit_test + "test2: passed" ) ;
            _tests_passed++ ;
         }
         else
         {
            System.out.println( _unit_test + "test2: failed" ) ;
            _tests_failed++ ;
         }
      }
      catch ( IOException ex )
      {
         System.err.println( _unit_test + "test2: failed " + ex ) ;
      }
   }

   private void test3()
   {
      try
      {
         String test_tran_type_3 = _in.readLine() ;
         String test_uid_3_str   = _in.readLine() ;
         Uid test_uid_3 = new Uid( test_uid_3_str ) ;

         int test_status1 = _tsc.getTransactionStatus( test_tran_type_3, test_uid_3 ) ;
         int test_status2 = _tsc.getTransactionStatus( "", test_uid_3 ) ;

         _out.println ( "OK" ) ;
         _out.flush() ;

         test_tran_type_3 = _in.readLine() ;
         test_uid_3_str   = _in.readLine() ;
         test_uid_3 = new Uid( test_uid_3_str ) ;

         int test_status3 = _tsc.getTransactionStatus( test_tran_type_3, test_uid_3 ) ;
         int test_status4 = _tsc.getTransactionStatus( "", test_uid_3 ) ;

         _out.println ( "OK" ) ;
         _out.flush() ;

         test_tran_type_3 = _in.readLine() ;
         test_uid_3_str   = _in.readLine() ;
         test_uid_3 = new Uid( test_uid_3_str ) ;

         int test_status5 = _tsc.getTransactionStatus( test_tran_type_3, test_uid_3 ) ;
         int test_status6 = _tsc.getTransactionStatus( "", test_uid_3 ) ;

         _out.println( "OK" ) ;
         _out.flush() ;

         if ( ( test_status1 == ActionStatus.ABORTED ) &&
              ( test_status2 == ActionStatus.ABORTED ) &&
              ( test_status3 == ActionStatus.RUNNING ) &&
              ( test_status4 == ActionStatus.RUNNING ) &&
              ( test_status5 == ActionStatus.ABORTED ) &&
              ( test_status6 == ActionStatus.ABORTED ) )
         {
            System.out.println( _unit_test + "test3: passed" ) ;
            _tests_passed++ ;
         }
         else
         {
            System.out.println( _unit_test + "test3: failed" ) ;
            _tests_failed++ ;
         }
      }
      catch ( IOException ex )
      {
         System.err.println( _unit_test + "test3: failed " + ex ) ;
      }
   }

   public void doWork( InputStream is, OutputStream os )
      throws IOException
   {
      _in  = new BufferedReader( new InputStreamReader(is) ) ;
      _out = new PrintWriter( new OutputStreamWriter(os) ) ;

      try
      {
         String pidUidStr = _in.readLine() ;
         Uid pidUid = new Uid( pidUidStr ) ;
         String pidStr = _in.readLine() ;

         if ( pidUid == Uid.nullUid() )
         {
            System.out.println( _unit_test + "Test Failed" ) ;
         }
         else
         {
            _tsc = new TransactionStatusConnector( pidStr, pidUid ) ;

            test1() ;
            test2() ;
            test3() ;

            System.out.println( _unit_test + "tests passed: " + _tests_passed +
                                "  tests failed: " + _tests_failed ) ;
         }
      }
      catch ( Exception ex )
      {
         System.err.println( _unit_test + " FAILED " + ex ) ;
      }
   }

   private static final String _unit_test = ActionTestClient._unit_test ;

   private static TransactionStatusConnector _tsc ;

   private static BufferedReader _in ;
   private static PrintWriter    _out ;

   private static AtomicAction _transaction_1 ;
   private static AtomicAction _transaction_2 ;
   private static AtomicAction _transaction_3 ;

   private static Uid _test_uid_1 ;
   private static Uid _test_uid_2 ;
   private static Uid _test_uid_3 ;

   private static String _test_tran_type_1 ;
   private static String _test_tran_type_2 ;
   private static String _test_tran_type_3 ;

   public static int _tests_passed = 0 ;
   public static int _tests_failed = 0 ;
}

public class ActionTestClient extends Test
{
   private static boolean test_setup()
   {
      boolean setupOk = false ;

      try
      {
         _test_service = new ActionTestClientTestService() ;
         _listener = new Listener( test_port, _test_service ) ;
         _listener.setDaemon( true ) ;
         _listener.start() ;

         Thread.sleep( 8000 ) ;  // allow time for test to run

         setupOk = true ;
      }
      catch ( Exception ex )
      {
         System.err.println( _unit_test + "test_setup: Failed " + ex ) ;
      }

      return setupOk ;
   }

    public void run( String[] args )
    {
        if ( !test_setup() )
        {
            logInformation("Test setup failed");
            assertFailure();
        }

        logInformation("Tests Passed: "+ActionTestClientTestService._tests_passed);
        logInformation("Tests Failed: "+ActionTestClientTestService._tests_failed);

        if ( ActionTestClientTestService._tests_failed == 0 )
        {
            assertSuccess();
        }
        else
        {
            assertFailure();
        }
    }

public static void main(String[] args)
    {
        ActionTestClient test = new ActionTestClient();
        test.initialise(null, null, args, new com.arjuna.mwlabs.testframework.unittest.LocalHarness());
        test.run(args);
    }


   public static final String _unit_test = "com.hp.mwtests.ts.arjuna.recovery.ActionTestClient: " ;

   private static ActionTestClientTestService _test_service ;

   private static int test_port  = 4321 ;

   private static Listener _listener ;
}
