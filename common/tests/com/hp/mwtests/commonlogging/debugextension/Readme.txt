JBoss, Home of Professional Open Source
Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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

(C) 2005-2006,
@author JBoss Inc.
1. Description
   The DebugExt class allows to test how finer debugging re provided for the Common Logging 
   Framework and how finer debuggin value are provided or assigned to determine if the logging is 
   enabled or not.
   
 
2. How to test
   
   prompt> java [option] DebugExt

	   where [option] is optional and concerns any property defined in the CommonLogging.properties, 
	   and passed with the java flag -D. For instance:
	   -DLogFac=<log_name>	    : where <log_name> is CSFLOG or LOG4J (default is CFSLOG)
	   -Doutput=<output_name>   : where <output_name> is console or file (default is console)
	   ...
	   
	   See the CommonLogging.properties for more details and additional properties
