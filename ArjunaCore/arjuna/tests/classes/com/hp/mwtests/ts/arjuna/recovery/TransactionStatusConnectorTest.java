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
 * $Id: TransactionStatusConnectorTest.java 2342 2006-03-30 13:06:17Z  $
 */

import java.io.* ;
import java.net.* ;
import java.util.* ;

import com.arjuna.ats.arjuna.common.Uid ;
import com.arjuna.ats.arjuna.coordinator.ActionStatus ;
import com.arjuna.ats.arjuna.recovery.Service ;
import com.arjuna.ats.internal.arjuna.recovery.Listener ;
import com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector ;
import com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem ;
import com.arjuna.ats.arjuna.utils.Utility ;

class TransactionStatusConnectorTestService implements Service
{
   public void doWork( InputStream is, OutputStream os )
      throws IOException
   {
      BufferedReader in  = new BufferedReader( new InputStreamReader(is) ) ;
      PrintWriter    out = new PrintWriter( new OutputStreamWriter(os) ) ;

      try
      {
         out.println ( Utility.intToHexString( Utility.getpid() )) ;
         out.flush() ;

         String rmStatus = in.readLine() ;
         if ( rmStatus.equals( "OK" ) )
         {         
            for ( int i = 0 ; i < 4 ; i++ )
            {
               _rx_tran_type = in.readLine() ;
               _rx_uid_str   = in.readLine() ;

               out.println( _test_status ) ;
               out.flush() ;
            }
         }
      }
      catch ( SocketException ex )
      {
         ; // Socket closed
      }
      catch ( Exception ex )
      {
         System.err.println( "TestService failed " + ex ) ;
      }
   }
   
   public String _rx_tran_type ;
   public String _rx_uid_str ;
   public String _test_status ;
   public boolean _stop_test = false ;
}

public class TransactionStatusConnectorTest
{
   public static void main( String[] args )
   {
      if ( test_setup() )
      {
         try
         {
            test1() ;
            
            System.out.println( _unit_test + "tests passed: " + _tests_passed +
                                "  tests failed: " + _tests_failed ) ;
         }
         catch ( Exception ex )
         {
            System.err.println( _unit_test + "FATAL EXCEPTION: " +
                                           "tests passed: " + _tests_passed +
                                           "  tests failed: " + _tests_failed ) ;
         }
      }
   }

   /**
    * Pre-test setup.
    */
   private static boolean test_setup()
   {
      boolean setupOk = false ;
      
      try
      {
         _tests_passed = 0 ;
         _tests_failed = 0 ;

         if ( TransactionStatusManagerItem.createAndSave( _test_port ) )
         {
            _pidUid = Utility.getProcessUid() ;
            String strUid = _pidUid.toString() ;
            StringTokenizer st = new StringTokenizer( strUid, ":" ) ;
            st.nextToken() ;
            _pidStr = st.nextToken() ;

            _test_service_socket = new ServerSocket( _test_port ) ;
            _test_service = new TransactionStatusConnectorTestService() ;

            _test_status_1 = ActionStatus.COMMITTED ;
            _test_status_2 = ActionStatus.PREPARED ;
            _test_status_3 = ActionStatus.ABORTED ;
            _test_status_4 = ActionStatus.INVALID ;

            _test_uid_1 = new Uid() ;
            _test_uid_2 = new Uid() ;
            _test_uid_3 = new Uid() ;
            _test_uid_4 = new Uid() ;
            
            _test_tran_type_1 = "_test_tran_type_1" ;
            _test_tran_type_2 = "_test_tran_type_2" ;
            _test_tran_type_3 = "_test_tran_type_3" ;
            _test_tran_type_4 = "_test_tran_type_4" ;

            _listener = new Listener( _test_service_socket, _test_service ) ;
            _listener.start() ;

            setupOk = true ;
         }
      }
      catch ( Exception ex )
      {
         System.err.println( "test_setup: Failed " + ex ) ;
      }
      
      return setupOk ;
   }
   
   /**
    * Test which checks that a transaction status connector can be
    * created and transaction statuses can be retrieved using a simple
    * test service.
    */
   private static void test1()
   {
      try
      {
         TransactionStatusConnector testTransactionStatusConnector = 
            new TransactionStatusConnector( _pidStr, _pidUid ) ;

         _test_service._test_status = Integer.toString( _test_status_1 ) ;
         int test_status1 = testTransactionStatusConnector.getTransactionStatus
                               ( _test_tran_type_1, _test_uid_1 ) ;
         _rx_tran_type_1 = _test_service._rx_tran_type ;
         _rx_uid_str_1   = _test_service._rx_uid_str ;
                               
         _test_service._test_status = Integer.toString( _test_status_2 ) ;
         int test_status2 = testTransactionStatusConnector.getTransactionStatus
                               ( _test_tran_type_2, _test_uid_2 ) ;
         _rx_tran_type_2 = _test_service._rx_tran_type ;
         _rx_uid_str_2   = _test_service._rx_uid_str ;
                               
         _test_service._test_status = Integer.toString( _test_status_3 ) ;
         int test_status3 = testTransactionStatusConnector.getTransactionStatus
                               ( _test_tran_type_3, _test_uid_3 ) ;
         _rx_tran_type_3 = _test_service._rx_tran_type ;
         _rx_uid_str_3   = _test_service._rx_uid_str ;
         
         _test_service._test_status = Integer.toString( _test_status_4 ) ;
         int test_status4 = testTransactionStatusConnector.getTransactionStatus
                               ( _test_tran_type_4, _test_uid_4 ) ;
         _rx_tran_type_4 = _test_service._rx_tran_type ;
         _rx_uid_str_4   = _test_service._rx_uid_str ;
                             
         if ( ( test_status1 == _test_status_1 ) &&
              ( test_status2 == _test_status_2 ) &&
              ( test_status3 == _test_status_3 ) &&
              ( test_status4 == _test_status_4 ) &&
               ( _rx_tran_type_1.equals( _test_tran_type_1 ) ) &&
               ( _rx_tran_type_2.equals( _test_tran_type_2 ) ) &&
               ( _rx_tran_type_3.equals( _test_tran_type_3 ) ) &&
               ( _rx_tran_type_4.equals( _test_tran_type_4 ) ) &&
               ( _rx_uid_str_1.equals( _test_uid_1.toString() ) ) &&
               ( _rx_uid_str_2.equals( _test_uid_2.toString() ) ) &&
               ( _rx_uid_str_3.equals( _test_uid_3.toString() ) ) &&
               ( _rx_uid_str_4.equals( _test_uid_4.toString() ) ) )
         {
            System.out.println( _unit_test + "test1: passed" ) ;
            _tests_passed++ ;
         }
         else
         {
            System.out.println( _unit_test + "test1: failed" ) ;
            _tests_failed++ ;
         }
         _listener.stopListener() ;
      }
      catch ( Exception ex )
      {
         System.err.println( _unit_test + " test1 " + ex ) ;
         _tests_failed++ ;
      }
   }         
         
   private static final String _unit_test = "com.hp.mwtests.ts.arjuna.recovery.TransactionStatusConnectorTest: " ;
   private static final int _test_port = 4321 ;

   private static TransactionStatusConnectorTestService _test_service ;

   private static Socket       _test_socket ;
   private static String       _test_host ;
   private static ServerSocket _test_service_socket ;
   
   private static Listener _listener ;
   
   private static Uid    _pidUid ;
   private static String _pidStr ;
   
   private static BufferedReader _from_test_service ;
   private static PrintWriter    _to_test_service ;
   
   private static int _test_status ;
   private static int _test_status_1 ;
   private static int _test_status_2 ;
   private static int _test_status_3 ;
   private static int _test_status_4 ;
   
   private static Uid _test_uid_1 ;
   private static Uid _test_uid_2 ;
   private static Uid _test_uid_3 ;
   private static Uid _test_uid_4 ;
   
   private static String _test_tran_type_1 ;
   private static String _test_tran_type_2 ;
   private static String _test_tran_type_3 ;
   private static String _test_tran_type_4 ;
   
   private static String _rx_tran_type_1 ;
   private static String _rx_tran_type_2 ;
   private static String _rx_tran_type_3 ;
   private static String _rx_tran_type_4 ;
   
   private static String _rx_uid_str_1 ;
   private static String _rx_uid_str_2 ;
   private static String _rx_uid_str_3 ;
   private static String _rx_uid_str_4 ;
   
   private static int _tests_passed = 0 ;
   private static int _tests_failed = 0 ;
}
