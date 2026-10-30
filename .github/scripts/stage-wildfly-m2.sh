#!/bin/bash
#
# Stage the org.wildfly SNAPSHOT artifacts that the jboss-as xts and rts test modules need
# (xts, rts, testsuite/integration/xts and testsuite/integration/rts) so they can be shipped
# to the RTS/XTS jobs without copying the whole ~/.m2/repository/org/wildfly tree.
#
# Usage: stage-wildfly-m2.sh <destination-dir> <wildfly-version> <narayana-version>
#
# Must be run from the Narayana workspace root, after jboss-as has been built with "install".
# The destination uses the same layout as ~/.m2/repository/org/wildfly (<artifactId>/<version>/...)
# so it can be restored by downloading it straight into that directory.
#
# Honours MAVEN_ARGS (e.g. -Dmaven.repo.local=...) and M2_REPO (default: ~/.m2/repository).

set -euo pipefail

DEST=${1:?destination dir}
WILDFLY_VERSION=${2:?wildfly version}
NARAYANA_VERSION=${3:?narayana version}
M2_WILDFLY=${M2_REPO:-$HOME/.m2/repository}/org/wildfly

# BOMs and test-suite poms that are imported/depended on by the modules above but are not part
# of their reactor closure. They are tiny (pom only). wildfly-build and wildfly-dist are
# deliberately absent: the test-suite is run with -Dtestsuite.only so they are not demanded.
POM_ONLY_ARTIFACTS=(
  wildfly-common-ee-dependency-management
  wildfly-common-expansion-dependency-management
  wildfly-standard-ee-bom
  wildfly-standard-preview-ee-bom
  wildfly-standard-legacy-ee-bom
  wildfly-standard-expansion-bom
  wildfly-standard-test-bom
  wildfly-standard-test-expansion-bom
  wildfly-ts-build-demander-base
  wildfly-ts-build-demander-expansion
)

# Let Maven compute the reactor closure instead of maintaining the list by hand: it is every
# module xts, rts and testsuite/shared (test dependency of the integration modules) depend on.
CLOSURE=()
while IFS= read -r artifact; do CLOSURE+=("${artifact}"); done < <(
  mvn -B -ntp -q -f jboss-as/pom.xml \
    -pl xts,rts,testsuite/shared -am \
    -Dversion.org.jboss.narayana="${NARAYANA_VERSION}" \
    org.codehaus.mojo:exec-maven-plugin:3.6.4:exec \
    -Dexec.executable=echo '-Dexec.args=${project.groupId}:${project.artifactId}' \
  | grep '^org\.wildfly:' | cut -d: -f2 | sort -u
)

[ "${#CLOSURE[@]}" -gt 0 ] || { echo "ERROR: empty reactor closure for xts/rts" >&2; exit 1; }

rm -rf "${DEST}"
mkdir -p "${DEST}"

for artifact in "${CLOSURE[@]}" "${POM_ONLY_ARTIFACTS[@]}"; do
  src="${M2_WILDFLY}/${artifact}/${WILDFLY_VERSION}"
  if [ ! -d "${src}" ]; then
    echo "ERROR: ${src} not found: was jboss-as built with 'install'?" >&2
    exit 1
  fi
  mkdir -p "${DEST}/${artifact}"
  cp -R "${src}" "${DEST}/${artifact}/"
done

echo "Staged $(( ${#CLOSURE[@]} + ${#POM_ONLY_ARTIFACTS[@]} )) org.wildfly artifacts ($(du -sh "${DEST}" | cut -f1)) in ${DEST}"
