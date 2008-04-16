#
# JBoss, Home of Professional Open Source
# Copyright 2007, Red Hat Middleware LLC, and individual contributors
# as indicated by the @author tags.
# See the copyright.txt in the distribution for a
# full listing of individual contributors.
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
#!/bin/bash

(
function waitUntilComplete()
{
  echo "Waiting for tests to finish at `date`"
  sleep 120
  while [ `GET http://dtf/dtf/summary.jsp` -gt 0 ]
  do
    sleep 30
  done
  echo "Tests finished at `date`, restarting all testnodes and pausing"
  GET 'http://dtf/dtf/default.jsp?page=nodemanager&function=restartall' >/dev/null
  sleep 30
  echo "Testnodes started at `date`, restarting coordinator and pausing"
  GET 'http://dtf/dtf/default.jsp?page=nodemanager&function=restartcoordinator' >/dev/null
  sleep 30
  echo "Continuing test run at `date`"
}

export ANT_HOME=/usr/local/ant-1.5.3/
export JAVA_HOME=/opt/j2sdk1.4.2/

export PATH=${JAVA_HOME}/bin:${ANT_HOME}/bin:${PATH}

ant -Dcvs.tag=ATS_4_0_2_RC1 -Dtests.linux=yes -Dtests.sunos=yes -Dtests.win2k=yes -f run-qa.xml -Dtests.basic=yes

waitUntilComplete

ant -Dcvs.tag=ATS_4_0_2_RC1 -Dtests.linux=yes -Dtests.sunos=yes -Dtests.win2k=yes -f run-qa.xml -Dtests.rawresources=yes

waitUntilComplete

ant -Dcvs.tag=ATS_4_0_2_RC1 -Dtests.linux=yes -Dtests.sunos=yes -Dtests.win2k=yes -f run-qa.xml -Dtests.crashrecovery=yes

waitUntilComplete

ant -Dcvs.tag=ATS_4_0_2_RC1 -Dtests.linux=yes -Dtests.sunos=yes -Dtests.win2k=yes -f run-qa.xml -Dtests.jdbc=yes
) > run-tests.out 2>run-tests.err </dev/null &
