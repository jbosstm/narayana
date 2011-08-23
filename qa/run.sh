#!/bin/bash

sed -i TaskImpl.properties -e "s#^COMMAND_LINE_0=.*#COMMAND_LINE_0=java#"
ant -Ddriver.url=file:///home/tom/narayana/dbdrivers get.drivers dist
ant -f run-tests.xml test -Dtest=jdbcresources01_oracle_thin_jndi
