#!/bin/bash

#  functions to download the WildFly and to setup the JBOSS_HOME directory

function download_wildfly_nightly_build() {
  local urlNightlyBuildZip=${1:-https://ci.wildfly.org/httpAuth/repository/downloadAll/WF_Nightly/.lastSuccessful/artifacts.zip}
  wget --user=guest --password=guest -nv "${urlNightlyBuildZip}"
  unzip -q artifacts.zip
  # the artifacts.zip may be wrapping several zip files: artifacts.zip -> wildfly-latest-SNAPSHOT.zip -> wildfly-###-SNAPSHOT.zip
  wildflyLatestZipWrapper=$(ls wildfly-latest-*.zip | head -n 1)
  if [ -f "${wildflyLatestZipWrapper}" ]; then # wrapper zip exists, let's unzip it to proceed further to distro zip
    unzip -q "${wildflyLatestZipWrapper}"
    [ $? -ne 0 ] && echo "Cannot unzip WildFly nightly build wrapper zip file '${wildflyLatestZipWrapper}'" && return 2
  fi
  wildflyDistZip=$(ls wildfly-*-SNAPSHOT.zip | head -n 1)
  [ "x$wildflyDistZip" = "x" ] && echo "Cannot find any zip file of SNAPSHOT WildFly distribution in the nightly build artifacts" && return 1
  unzip -q "${wildflyDistZip}"
  [ $? -ne 0 ] && echo "Cannot unzip WildFly nightly build distribution zip file '${wildflyDistZip}'" && return 3
  export JBOSS_HOME="${PWD}/${wildflyDistZip%.zip}"
  [ ! -d "${JBOSS_HOME}" ] && echo "After unzipping the file '${wildflyDistZip}' the JBOSS_HOME directory at '${JBOSS_HOME}' does not exist" && return 4
  # zip cleanup
  rm -f artifacts.zip
  rm -f wildfly-*-SNAPSHOT*.zip
}

function download_wildfly_dist() {
  local urlDistZip=${1:-https://download.jboss.org/wildfly/21.0.0.Final/wildfly-21.0.0.Final.zip}
  wget -nv "${urlDistZip}"
  local wildflyDistZip=${urlDistZip##*/}
  unzip -q "${wildflyDistZip}"
  [ $? -ne 0 ] && echo "Cannot unzip WildFly distribution zip file '${wildflyDistZip}'" && return 3
  export JBOSS_HOME="${PWD}/${wildflyDistZip%.zip}"
  # zip cleanup
  rm -f "${wildflyDistZip}"
}