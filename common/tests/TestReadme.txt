JBoss, Home of Professional Open Source
Copyright 2006, JBoss Inc., and others contributors as indicated 
by the @authors tag. All rights reserved. 
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

1- Description:
---------------
The tests directory provide a set of programs allowing to test features of the Common Logging Framework.
Each Subdirectory provides its own Readme file explaining the aim of the test program and how to run it.

Basically each program is execute using the following template.

   prompt> java [-DLogFac=<log_name>] [-Doutput=<output_name>] [-Dlanguage=<language_name>] \
	   [-Dcountry=<country_name>] [-Dlogfile=<file_name>] <java_program>

	   where:
	   ----------------------------------------------------------------------------------
           []  : The property is Optional 

	   <log_name>	  : CSFLOG or LOG4J	-	The default is CFSLOG
	   <output_name>  : console or file	-	The default is console
	   <language_name>: fr, en, ...		-	The default is en
	   <country_name> : EN, FR, ...		-	The default is US

	   <file_name>	  : The name given to the output file
			    The way the name is used in HP CSF logging and LOG4j is different.
			    - Log4j:
			      The Default name given for the output file is "transaction.tlog",
			      which can be changed by the <file_name> if provided.
			    - CSF Log
			      The Default name given for the output file is LOGactive.log. 
			      If provided, <file_name> replaces only the suffix LOG.
			      This default will be corrected in a next version.
			     
           <java_program> : The java class



2- Important Note
-----------------
The Common Logging Framework provides a set of Levels defining the importance of a logging message.
The provided default Levels and their corresponding underlying values are given below:

    CommonLevel	     Log4j	HP log
    -----------	     -----	------
    FATAL	     FATAL	CRITICAL
    ERROR	     ERROR	ERROR
    WARN	     WARN	WARNING
    INFO	     INFO	INFO
    DEBUG	     DEBUG	DEBUG	

When requested to be sent to an output (console, file, ...), a logging message associated with
a CommonLevel is currently displayed or provided to the output with its corresponding underlying
value. That is, when using the common Logging Framework on top of HP log, the developer should
be aware that the CommonLevel FATAL will be displayed as a CRITICAL level. 

A next release of the Common Logging Framework will sent a same value. A new HP LogFormater will
be provided.

For each CommonLevel the corresponding value that will be displayed with the current version are
given below:

    CommonLevel	     Log4j	HP log
    -----------	     -----	------
    FATAL	     FATAL	C
    ERROR	     ERROR	E
    WARN	     WARN	W
    INFO	     INFO	I
    DEBUG	     DEBUG	D	



