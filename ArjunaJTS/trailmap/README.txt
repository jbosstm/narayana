JBoss, Home of Professional Open Source
Copyright 2006, Red Hat Middleware LLC, and individual contributors
as indicated by the @author tags. 
See the copyright.txt in the distribution for a full listing 
of individual contributors.
This copyrighted material is made available to anyone wishing to use,
modify, copy, or redistribute it subject to the terms and conditions
of the GNU Lesser General Public License, v. 2.1.
This program is distributed in the hope that it will be useful, but WITHOUT A
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License,
v.2.1 along with this distribution; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
MA  02110-1301, USA.


(C) 2005-2006,
@author JBoss Inc.
The trail map is provided with examples (the Banking application) that
allow  a better  understanding   of the  way   to  use  the   JBossTS
Programming interfaces.

To build the sources files you should follow instructions given below:

- Ensure you have the Ant build system installed. Ant is a Java build
tool, similar to make. 
  It is available for free from http://ant.apache.org/ 
  The sample application requires version 1.5.1 or later. 

- The PATH and CLASSPATH environment variables need to be set
appropriately to use JBoss Transaction Service. 

  To make this easier, we provide a shell script setup-env.sh (and for
  Windows a batch file setup-env.bat) which you  can either source, or
  use to input into your own environment. These scripts are located in
  in the directory <jbossts_install_root>/bin/ 
  
  JNDI is recommended way to use XADataSource because it isolates the application from the
  different jdbc implementations. The JNDI implementation, that the jdbcbank sample uses is fscontext.jar,
  which can be download from http://java.sun.com/products/jndi/downloads/index.html 

Important Note: 

  Ensure that, in your CLASSPATH, any JBossTS jar file appears before
  the jacorb (version 2.2.2) jar files

 From a command prompt,  go (or 'cd') to  the directory containing the
 build.xml file (<jbossjts_install_root>/trailmap) and type 'ant'.

 Add   the  generated file  named   jbossts-demo.jar and located under
 <jbossjts_install_root>/trailmap/lib  in    you  CLASSPATH environment
 variable.
 
When running the local JTS transactions part of the trailmap, you will need to start
the recovery manager: java com.arjuna.ats.arjuna.recovery.RecoveryManager -test

You will need a jacorb.properties file to run the distributed JTS tests.

 For each sample, refer to the appropriate trail page.
 
 Database Note:
 
 The out-of-the-box configuration assumes an Oracle database. If you want
 to use MSSQLServer, then you need to do the following:
 
	1. install SQL Server JDBC XA procedures http://edocs.bea.com/wls/docs81/jdbc_drivers/mssqlserver.html#1075232
	2. Start up the Microsoft Distributed Transaction Coordinator (DTC) http://msdn2.microsoft.com/en-US/library/ms378931(SQL.90).aspx 

If to shift from using Oracle as in jdbcbank example to Microsoft SQLServer 2000, the
initialization code for XADataSource should be replaced. The jdbc driver which may be used is available from:
http://www.microsoft.com/downloads/details.aspx?familyid=07287B11-0502-461A-B138-2AA54BFDC03A&displaylang=en

<code>
  SQLServerDataSource ds = new com.microsoft.jdbcx.sqlserver.SQLServerDataSource();
  ds.setDescription("MSSQLServer2k DataSource");
  ds.setServerName(host);
  ds.setPortNumber(1433);
  ds.setDatabaseName(dbName);
  ds.setSelectMethod("cursor"); //It's a Must emphasized in Driver's User Manual
</code>
to replace
<code>
  Class oracleXADataSource = Class.forName("oracle.jdbc.xa.client.OracleXADataSource");
  DataSource ds = (DataSource) oracleXADataSource.newInstance();
  Method setUrlMethod = oracleXADataSource.getMethod("setURL", new Class[]{String.class});
  setUrlMethod.invoke(ds, new Object[]{new String("jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName)});
</code> 

