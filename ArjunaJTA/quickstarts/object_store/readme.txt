JBoss, Home of Professional Open Source
Copyright 2011, Red Hat Middleware LLC, and individual contributors
as indicated by the @author tags.
See the copyright.txt in the distribution for a
full listing of individual contributors.
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
 
(C) 2011
@author JBoss Inc.

OVERVIEW
--------
A transaction manager must store enough information such that it can guarantee recovery from failures.
This is achieved by persisting information in an Object Store. Various implementation are provided
to cater for various application requirements.

1. FileStoreExample shows how to change the store type to a file base store but in directory different from the default;
2. HornetqStoreExample shows how to use the Hornetq journal for transction logging;
3. VolatileStoreExample shows how to use an unsafe (because it does not persist logs in the event of
   failures and therefore does not support recovery) in-memory log store implementation.

USAGE
-----
./run.[sh|bat]
or to run individual tests using the maven java exec plugin:
	mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.VolatileStoreExample
	mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.HornetqStoreExample
	mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.FileStoreExample

EXPECTED OUTPUT
---------------

When running examples one at a time look for the output
[INFO] BUILD SUCCESS
If you use the run script then you the line "[INFO] BUILD SUCCESS" should appear once for each example.

WHAT JUST HAPPENED?
-------------------
Each example either changes the object store directory or object store type (or both) and then runs a
transaction. Each example performs a relevant test to verify that the object store type or directory,
as appropriate, was used.
