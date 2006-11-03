JBoss, Home of Professional Open Source
Copyright 2006, Red Hat Middleware LLC, and individual contributors
as indicated by the @author tags. 
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
->	Configuration script options

		-d
	Use the default answers for aquestions.  Using this will override
	environment variables (-e option).

		-e
	Use environment variables for answers for all questions.  The
	variable names are documented below.  If the environment variable
	is not set, the default answer will be used.

		-r
	Create a release build.  This replaces constants with expressions
	evaluated at run time and based on the value of $JTSARJUNA_HOME. IF
	this is specified at the top level, all modules for a product will be
	configured. built and installed.

		-u
	Unconfigure the module.  Removes the files created by the configuration
	process.

		<cf file>
	Configuration file to use for answers.  Using a configuration file will
	override default answers and environment variables (-d and -e options).


->	Questions and environment variables

The questions and environment variables used for the orbportability module are :

	Root of directory of		$INSTALLTOPDIR
	orbportability installation

	Root of directory of		$BUILDSYSTEMTOPDIR [1]
	BuildSystem

	Orb number [1]			$ORB_NUMBER [2]

	Support bind [n]		$SUPPORT_BIND [3]

	Specific idl->Java generation	$IDLFLAGS_DEFAULT [4]
	flags [ ]

	Specific interface repository	$IRFLAGS_DEFAULT [4]
	flags [ ]

	Default service resolution	$DEFAULT_BIND_MECHANISM [4]
	mechanism

	Location of properties file	$PROPERTIES

	Versioon of properties file	$ORBPORTABILITY_PROPERTIES_VERSION [5]

	Install test programs [n]	$INSTALL_TESTS

	Build utility programs [y]	$BUILD_UTILITIES

	Install utility programs [y]	$INSTALL_UTILITIES

	What is the source identifier	$SOURCEID
	[unknown]

	What is your name		$BUILDINFO [6]

	Any other information [none]	$NOTES

[1] This is automatically set for release builds
[2] That really is a default of "1" - Orbix 2000
[3] This is only required for Visibroker 4
[4] The default is ORB-specific
[5] The default is derived from the product version number
[6] The default is set from the output of `whoami`

->	Additional information

	The environment variable $ORB_ROOT must be set to the installation
	root directory of the ORB being used.
