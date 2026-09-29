#!/bin/bash -e
#
# Copyright The Narayana Authors
# SPDX-License-Identifier: Apache-2.0
#
# Runs the REST-AT (rts/at/tx) unit tests against the embedded servers supported
# by BaseTest. The server is selected through the -Drts.server system property
# that BaseTest reads: "undertow" (default), "netty" or "quarkus".
#
# undertow and netty are distinct HTTP stacks, so the full protocol suite runs
# under each. The quarkus path deploys the same UndertowJaxrsServer as the
# undertow path (it just wraps it in a QuarkusApplication with quarkus-resteasy
# on the classpath), so its pass is only a smoke test that verifies the quarkus
# classpath and the QuarkusApplication wrapper start up and serve the REST-AT
# endpoints correctly.
#
# TLS is a transport concern orthogonal to the transaction protocol, so it is
# only smoke-tested (with -Drts.usessl=true, using a certificate generated at
# runtime by TestSSLContext) once per server to prove each server's HTTPS wiring.
#
# Any extra arguments are forwarded to build.sh (e.g. -fae, -PcodeCoverage). A
# caller-supplied -Dtest overrides the smoke selection for the smoke passes.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

# Representative test class exercised under the smoke passes (override with the
# RTS_SMOKE_TEST env var if needed).
SMOKE_TEST="${RTS_SMOKE_TEST:-${RTS_QUARKUS_SMOKE_TEST:-CoordinatorTest}}"

cd "${ROOT_DIR}"

echo "===== REST-AT tx tests: rts.server=undertow (full suite) ====="
./build.sh -f rts/at/tx/pom.xml -B -Drts.server=undertow "$@" test

echo "===== REST-AT tx tests: rts.server=netty (full suite) ====="
./build.sh -f rts/at/tx/pom.xml -B -Drts.server=netty "$@" test

echo "===== REST-AT tx tests: rts.server=quarkus (smoke: ${SMOKE_TEST}) ====="
./build.sh -f rts/at/tx/pom.xml -B -Drts.server=quarkus -Dtest="${SMOKE_TEST}" "$@" test

echo "===== REST-AT tx tests: rts.server=undertow + TLS (smoke: ${SMOKE_TEST}) ====="
./build.sh -f rts/at/tx/pom.xml -B -Drts.server=undertow -Drts.usessl=true -Dtest="${SMOKE_TEST}" "$@" test

echo "===== REST-AT tx tests: rts.server=netty + TLS (smoke: ${SMOKE_TEST}) ====="
./build.sh -f rts/at/tx/pom.xml -B -Drts.server=netty -Drts.usessl=true -Dtest="${SMOKE_TEST}" "$@" test

echo "===== REST-AT tx tests: rts.server=quarkus + TLS (smoke: ${SMOKE_TEST}) ====="
./build.sh -f rts/at/tx/pom.xml -B -Drts.server=quarkus -Drts.usessl=true -Dtest="${SMOKE_TEST}" "$@" test

echo "===== REST-AT tx tests completed ====="
