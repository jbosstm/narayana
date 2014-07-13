#!/bin/sh

LIB_DIR=../lib
ETC_DIR=../etc
JAVAC=$JAVA_HOME/bin/javac
JAVA=$JAVA_HOME/bin/java

export CLASSPATH=$LIB_DIR/arjuna-5.0.0.CR2-SNAPSHOT.jar:
$LIB_DIR/common-5.0.0.CR2-SNAPSHOT.jar:$LIB_DIR/jboss-logging.jar:
$LIB_DIR/stm-5.0.0.CR2-SNAPSHOT.jar:
$LIB_DIR/txoj-5.0.0.CR2-SNAPSHOT.jar:$ETC_DIR:$CLASSPATH

$JAVAC SharedHammer.java

$JAVA SharedHammer