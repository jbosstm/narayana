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
1. Description
   The logi18n class allows to test the way the Common Logging Framework is able to manage national messages.
   In the example simple messages can be displayed either in French or in English. For this aim a programmer
   uses keys to specify the message needed to be displayed while properties file contain the corresponding
   text appropriate to the French or the English according to the programmer choice.


2. Properties file for i18n
   In this example properties files are prefixed with "logging_msg_", which indicates to the logging framework
   to seek a file beginning with this prefix. To determine the complete name file, the logging framewotk uses
   the properties 'language' and 'country'; In the program these properties are set to "english" and "U"S. 
   Provided file are:
   - logging_msg_fr_FR.properties
   - logging_msg_en_US.properties

   Additional files appropriate to other languages can be defined in a similar way.


3. How to test
   
   prompt> java [option] logi18n     

	   where [option] is optional and concerns any property defined in the CommonLogging.properties, 
	   and passed with the java flag -D. For instance:
	   -DLogFac=<log_name>	    : where <log_name> is CSFLOG or LOG4J (default is CFSLOG)
	   -Doutput=<output_name>   : where <output_name> is console or file (default is console)
	   ...
	   
	   See the CommonLogging.properties for more details and additional properties
