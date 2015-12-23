#!/bin/bash
if [[ $(uname) == CYGWIN* ]]
then
  read -p "ARE YOU RUNNING AN ELEVATED CMD PROMPT mvn.cmd needs this" ELEV
  if [[ $ELEV == n* ]]
  then
    exit
  fi
fi
read -p "You will need: VPN, credentials for jbosstm@filemgmt, jira admin, github permissions on all jbosstm/ repo. Do you have these?" ENVOK
if [[ $ENVOK == n* ]]
then
  exit
fi
if [ $# -lt 3 ]; then
  echo 1>&2 "$0: not enough arguments: PREVIOUS CURRENT NEXT (versions should end in .Final or similar)"
  grep 5.2.10.Final pom.xml
  exit 2
elif [ $# -gt 3 ]; then
  echo 1>&2 "$0: too many arguments: PREVIOUS CURRENT NEXT (versions should end in .Final or similar)"
  grep 5.2.10.Final pom.xml
  exit 2
fi

JENKINS_JOBS=narayana,narayana-catelyn,narayana-codeCoverage,narayana-documentation,narayana-hqstore,narayana-ibm-jdk,narayana-jdbcobjectstore,narayana-quickstarts,narayana-quickstarts-catelyn ./scripts/release/pre_release.py
export PREVIOUS=$1
export CURRENT=$2
export NEXT=$3
./scripts/release/update_jira.py -k JBTM -t 5.next -n $CURRENT
read -p "Enter WFLY issue: " WFLYISSUE
(cd scripts; ./pre-release.sh $CURRENT $NEXT)
(cd jboss-as; git fetch jbosstm; git checkout 5_BRANCH; git reset --hard jbosstm/5_BRANCH; sed -i "s/narayana>$CURRENT/narayana>$NEXT/g" pom.xml; git add pom.xml; git commit --amend -m "Update to latest version of Narayana"; git push -f)
(cd jboss-as; git fetch wildfly; git checkout -b ${WFLYISSUE}; git reset --hard wildfly/master; git branch -u jbosstm/${WFLYISSUE}; sed -i "s/narayana>$PREVIOUS/narayana>$CURRENT/g" pom.xml; git commit -am "${WFLYISSUE} Upgrade Narayana to $CURRENT"; git push jbosstm $WFLYISSUE)
if [[ $(uname) == CYGWIN* ]]
then
  echo "Detected Cygwin build"
  git fetch upstream; git checkout $CURRENT; MAVEN_OPTS="-XX:MaxPermSize=512m" ant -f build-release-pkgs.xml -Dmvn.executable="tools/maven/bin/mvn.cmd" -Dawestruct.executable="awestruct.bat" all
else
  echo "Detected non-Cygwin build"
  git fetch upstream; git checkout $CURRENT; MAVEN_OPTS="-XX:MaxPermSize=512m" ant -f build-release-pkgs.xml -Dmvn.executable="tools/maven/bin/mvn" -Dawestruct.executable="awestruct" all
fi
echo "build and retrieve the centos54x64 blacktie binary on centos54x64 machine"
ssh lancel.eng.hst.ams2.redhat.com -x "cd tmp ; rm -rf narayana ; git clone https://github.com/jbosstm/narayana.git ; cd narayana ; git fetch origin --tags ; git checkout $CURRENT ; git clone -b $WFLYISSUE  https://github.com/jbosstm/jboss-as.git; (cd jboss-as; JAVA_HOME=/usr/local/jdk1.8.0/ ./build.sh install -DskipTests); ./build.sh -f blacktie/wildfly-blacktie/pom.xml clean install -DskipTests ; ./build.sh -f blacktie/pom.xml clean install -DskipTests" ; scp lancel.eng.hst.ams2.redhat.com:tmp/narayana/blacktie/blacktie/target/blacktie-${CURRENT}-centos54x64-bin.tar.gz . ; scp blacktie-${CURRENT}-centos54x64-bin.tar.gz jbosstm@filemgmt.jboss.org:/downloads_htdocs/jbosstm/${CURRENT}/binary/
echo "You need to execute the following commands on a Windows box"
echo "set CURRENT="
echo "set WFLYISSUE="
echo "cd C:\tmp & del \Q \S & git clone https://github.com/jbosstm/narayana & cd narayana & git fetch origin --tags & git checkout %CURRENT% & git clone -b %WFLYISSUE% https://github.com/jbosstm/jboss-as.git"
echo "cd jboss-as & git checkout %WFLYISSUE% & build.bat install -DskipTests & cd .."
echo 'call "C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\vcvarsall.bat" & build.bat -f blacktie\wildfly-blacktie\pom.xml clean install -DskipTests & build.bat -f blacktie\pom.xml clean install -DskipTests'
echo "cd blacktie\blacktie\target & rsync -P --protocol=28 --chmod=ugo=rwX blacktie-%CURRENT%-vc9x32-bin.zip jbosstm@filemgmt.jboss.org:/downloads_htdocs/jbosstm/%CURRENT%/binary/blacktie-%CURRENT%-vc9x32-bin.zip"