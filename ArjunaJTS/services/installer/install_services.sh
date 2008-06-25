#!/bin/sh
#
# JBoss, Home of Professional Open Source
# Copyright 2006, Red Hat Middleware LLC, and individual contributors
# as indicated by the @author tags. 
# See the copyright.txt in the distribution for a full listing 
# of individual contributors.
# This copyrighted material is made available to anyone wishing to use,
# modify, copy, or redistribute it subject to the terms and conditions
# of the GNU Lesser General Public License, v. 2.1.
# This program is distributed in the hope that it will be useful, but WITHOUT A
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
# You should have received a copy of the GNU Lesser General Public License,
# v.2.1 along with this distribution; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
# MA  02110-1301, USA.

# 
# (C) 2005-2006,
# @author JBoss Inc.
#

# Unix services install/uninstall script
# Copyright (C) 2004, Arjuna Technologies, Limited.
# $Id: install_services.sh 2342 2006-03-30 13:06:17Z  $

IFS=" 
"

# Install (normal) or uninstall (invoked with -u)
if [ "a$1" = a-u ]; then
	INST=Uni
else
	INST=I
fi

# Which OS are we running on?
# Most OS's put uname in /usr/bin.  Linux puts it in /bin
if [ -x /usr/bin/uname ]; then
	OSNAME="`/usr/bin/uname -s`" 2>/dev/null
elif [ -x /bin/uname ]; then
	OSNAME="`/bin/uname -s`" 2>/dev/null
else
	echo Unable to find uname binary to determine OS name. >&2
	exit 1
fi

# Check JAVA_HOME
# (we need to add $JAVA_HOME/bin to startup/shutdown scripts)
if [ $INST = I -a "a$JAVA_HOME" = a ]; then
	echo \$JAVA_HOME must be set before the scripts are installed. >&2
	exit 1
fi

# File names
# (recovery manager needs to start before & stop after transaction manager)
RSCRIPT=recoverymanagerservice.sh
TSCRIPT=transactionserverservice.sh
RSTARTSCRIPT=S98recoverymanagerservice
TSTARTSCRIPT=S99transactionserverservice
RSTOPSCRIPT=K01recoverymanagerservice
TSTOPSCRIPT=K00transactionserverservice

# OS-dependant locations for scripts
case $OSNAME in
	HP-UX)
		STARTDIRS="/sbin/rc3.d"
		KILLDIRS="/sbin/rc2.d"
		SERVICEOS=hpux
		ED=/usr/bin/ed
	;;
	Linux)
		STARTDIRS="/etc/rc2.d /etc/rc3.d /etc/rc4.d /etc/rc5.d"
		KILLDIRS="/etc/rc0.d /etc/rc6.d"
		SERVICEOS=linux
		ED=/bin/ed
	;;
	SunOS)
		STARTDIRS="/etc/rc3.d"
		KILLDIRS="/etc/rcS.d /etc/rc0.d /etc/rc1.d /etc/rc2.d"
		SERVICEOS=solaris
		ED=/usr/bin/ed
	;;
	*)
		echo ${INST}nstallation for $OSNAME is not supported. >&2
		exit 1
	;;
esac

# Modify script function
modify_script () {
	echo Adding \$JAVA_HOME \($JAVA_HOME\) to \$PATH in
	echo $1
	$ED $1 << ENDOFEDIT > /dev/null
1
/^JAVA_HOME=
s:\(JAVA_HOME=\).*:\1$JAVA_HOME:
w
q
ENDOFEDIT
}

# Install function
do_install () {
	echo "  $2"
	(cd "$1" && ln -s "$3" "$2") || \
	    { echo Failed to install $1/$2 >&2; exit 1; }

}

# Uninstall function
do_uninstall () {
	(
		cd "$1" && \
		if [ -h "$2" ]; then
			echo "  $2"
			rm -f "$2"
		else
			echo $2 is not a symbolic link >&2
			false
		fi
	) || echo Failed to remove $1/$2 >&2
}

# Install
if [ $INST = I ]; then
#	Find the scripts.
#	If @HOME_DIRECTORY@ is not set, assume we're running from .
	if [ "a$@HOME_DIRECTORY@" = a ]; then
		@HOME_DIRECTORY@="`(cd ../.. && pwd) 2> /dev/null`"
	fi
	SERVICEDIR="$@HOME_DIRECTORY@/services/bin/$SERVICEOS"
	if [ ! -r "$SERVICEDIR/$RSCRIPT" ] || \
	    [ ! -r "$SERVICEDIR/$TSCRIPT" ]; then
		echo Unable to find scripts in $SERVICEDIR >&2
		echo Set \$@HOME_DIRECTORY@ or run this script from its directory >&2
		exit 1
	fi

#	Modify scripts
	if [ ! -x $ED ]; then
		echo Unable to run ed binary >&2
		exit 1
	fi
	modify_script "$SERVICEDIR/$RSCRIPT"
	modify_script "$SERVICEDIR/$TSCRIPT"

#	Do installations
	for d in $KILLDIRS; do
		echo Installing shutdown scripts into $d:
		do_install $d "$RSTOPSCRIPT" "$SERVICEDIR/$RSCRIPT"
		do_install $d "$TSTOPSCRIPT" "$SERVICEDIR/$TSCRIPT"
	done
	for d in $STARTDIRS; do
		echo Installing startup scripts into $d:
		do_install $d "$RSTARTSCRIPT" "$SERVICEDIR/$RSCRIPT"
		do_install $d "$TSTARTSCRIPT" "$SERVICEDIR/$TSCRIPT"
	done
# Uninstall
else
	for d in $STARTDIRS; do
		echo Removing startup scripts from $d:
		do_uninstall $d "$RSTARTSCRIPT"
		do_uninstall $d "$TSTARTSCRIPT"
	done
	for d in $KILLDIRS; do
		echo Removing shutdown scripts from $d:
		do_uninstall $d "$RSTOPSCRIPT"
		do_uninstall $d "$TSTOPSCRIPT"
	done
fi
