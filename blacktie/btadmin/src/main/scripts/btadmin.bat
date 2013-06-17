@echo off
java -Dlog4j.configuration=log4j.xml org.jboss.narayana.blacktie.btadmin.BTAdmin %*
