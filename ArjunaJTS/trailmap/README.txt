JBoss, Home of Professional Open Source
Copyright 2006, JBoss Inc., and individual contributors as indicated
by the @authors tag.  All rights reserved. 
See the copyright.txt in the distribution for a full listing 
of individual contributors.
This copyrighted material is made available to anyone wishing to use,
modify, copy, or redistribute it subject to the terms and conditions
of the GNU General Public License, v. 2.0.
This program is distributed in the hope that it will be useful, but WITHOUT A 
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
PARTICULAR PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License,
v. 2.0 along with this distribution; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
MA  02110-1301, USA.

(C) 2005-2006,
@author JBoss Inc.
The trail map is provided with examples (the Banking application) that
allow  a better  understanding   of the  way   to  use  the   JBossTS
Programming interfaces.

To build the sources files you should follow instructins given below:


- Ensure you have the Ant build system installed. Ant is a Java build
tool, similar to make. 
  It is available for free from http://ant.apache.org/ 
  The sample application requires version 1.5.1 or later. 

- The PATH and CLASSPATH environment variables need to be set
appropriately to use Arjuna Transaction Service. 

  To make this easier, we provide a shell script setup-env.sh (and for
  Windows a batch file setup-env.bat) which you  can either source, or
  use to input into your own environment. These scripts are located in
  in the directory <arjunats_install_root>/bin/ 

Important Note: 

  Ensure that, in your CLASSPATH, any JBossTS jar file appears before
  the jacorb (version 2.2.2) jar files

 From a command prompt,  go (or 'cd') to  the directory containing the
 build.xml file (<arjunats_install_root>/trailmap) and type 'ant'.

 Add   the  generated file  named   arjunats-demo.jar and located under
 <arjunats_install_root>/trailmap/lib  in    you  CLASSPATH environment
 variable.

 For each sample, refer to the appropriate trail page.


