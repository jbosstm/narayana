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
  echo 1>&2 "$0: not enough arguments: CURRENT_VERSION_IN_WFLY CURRENT NEXT <WFLYISSUE>(versions should end in .Final or similar)"
  grep 5.2.10.Final pom.xml
  exit 2
elif [ $# -gt 4 ]; then
  echo 1>&2 "$0: too many arguments: CURRENT_VERSION_IN_WFLY CURRENT NEXT (versions should end in .Final or similar)"
  grep 5.2.10.Final pom.xml
  exit 2
else
  WFLYISSUE=$4
fi

set -e
export CURRENT_VERSION_IN_WFLY=$1
export CURRENT=$2
export NEXT=$3
if [ -z "$WFLYISSUE" ]
then
  JENKINS_JOBS=narayana,narayana-catelyn,narayana-codeCoverage,narayana-documentation,narayana-hqstore,narayana-ibm-jdk,narayana-jdbcobjectstore,narayana-quickstarts,narayana-quickstarts-catelyn ./scripts/release/pre_release.py
  ./scripts/release/update_jira.py -k JBTM -t 5.next -n $CURRENT
  read -p "Enter WFLY issue: " WFLYISSUE
  if [ ! -d "jboss-as" ]
  then
    (git clone git@github.com:jbosstm/jboss-as.git -o jbosstm; cd jboss-as; git remote add upstream-wildfly git@github.com:wildfly/wildfly.git)
  fi
  (cd jboss-as; git fetch upstream-wildfly; git checkout -b ${WFLYISSUE}; git reset --hard upstream-wildfly/master)
  cd jboss-as
  if [[ $(uname) == CYGWIN* ]]
  then
    sed -i "s/narayana>$CURRENT_VERSION_IN_WFLY/narayana>$CURRENT/g" pom.xml
  else
    sed -i "" "s/narayana>$CURRENT_VERSION_IN_WFLY/narayana>$CURRENT/g" pom.xml
  fi
  git add pom.xml
  git commit -m "${WFLYISSUE} Upgrade Narayana to $CURRENT"
  git push --set-upstream jbosstm ${WFLYISSUE}
  git fetch jbosstm
  git checkout 5_BRANCH
  git reset --hard jbosstm/5_BRANCH
  if [[ $(uname) == CYGWIN* ]]
  then
    sed -i "s/narayana>$CURRENT/narayana>$NEXT/g" pom.xml
  else
    sed -i $SED_EXTRA_ARG "s/narayana>$CURRENT/narayana>$NEXT/g" pom.xml
  fi
  git add pom.xml
  git commit -m "Update to latest version of Narayana"
  git push
  cd ..
fi
set +e
git tag | grep $CURRENT
if [[ $? != 0 ]]
then
  set -e
  (cd ./scripts/ ; ./pre-release.sh $CURRENT $NEXT)
else
  set -e
fi
if [[ $(uname) == CYGWIN* ]]
then
  git fetch upstream --tags; git checkout $CURRENT; MAVEN_OPTS="-XX:MaxPermSize=512m" ant -f build-release-pkgs.xml -Dmvn.executable="tools/maven/bin/mvn.cmd" -Dawestruct.executable="awestruct.bat" all
else
  git fetch upstream --tags; git checkout $CURRENT; MAVEN_OPTS="-XX:MaxPermSize=512m" ant -f build-release-pkgs.xml -Dmvn.executable="tools/maven/bin/mvn" -Dawestruct.executable="awestruct" all
fi
echo "build and retrieve the centos54x64 blacktie binary on centos54x64 machine"
ssh narayanaci1.eng.hst.ams2.redhat.com -x "export JAVA_HOME=/usr/local/jdk1.8.0/ ; mkdir tmp ; cd tmp ; rm -rf narayana ; git clone https://github.com/jbosstm/narayana.git ; cd narayana ; git fetch origin --tags ; git checkout $CURRENT ; ./build.sh -f blacktie/wildfly-blacktie/pom.xml clean install -DskipTests ; ./build.sh -f blacktie/pom.xml clean install -DskipTests"
scp narayanaci1.eng.hst.ams2.redhat.com:tmp/narayana/blacktie/blacktie/target/blacktie-${CURRENT}-centos54x64-bin.tar.gz ~/tmp/narayana/$CURRENT/
scp ~/tmp/narayana/$CURRENT/blacktie-${CURRENT}-centos54x64-bin.tar.gz jbosstm@filemgmt.jboss.org:/downloads_htdocs/jbosstm/${CURRENT}/binary/
echo "You need to execute the following commands on a Windows box"
echo "cd %USERPROFILE%\tmp & del \S narayana & git clone https://github.com/jbosstm/narayana & cd narayana & git fetch origin --tags"
echo "set CURRENT="
echo 'set NOPAUSE=true & git checkout %CURRENT% & call "C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\vcvarsall.bat" & build.bat -f blacktie\wildfly-blacktie\pom.xml clean install -DskipTests & build.bat -f blacktie\pom.xml clean install -DskipTests & cd blacktie\blacktie\target & rsync -P --protocol=28 --chmod=ugo=rwX blacktie-%CURRENT%-vc9x32-bin.zip jbosstm@filemgmt.jboss.org:/downloads_htdocs/jbosstm/%CURRENT%/binary/blacktie-%CURRENT%-vc9x32-bin.zip'