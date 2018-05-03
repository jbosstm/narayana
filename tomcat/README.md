# Tomcat Narayana testing

The objective is to run basic integration with multiple versions of Tomcat across several databases.
The test suite offers four modes of operation:

## 1. H2, embedded database

This is the default. If there are no parameters specified, the test suite runs with embedded H2 database and its driver fetched from Maven central.

## 2. PostgreSQL server in CI

If selected, the test suite uses a resident PostgreSQL server present in the Narayana upstream CI environment. The server is always running, always ready and the test suite does not have any control over it. If you have such server installed, simply overwrite ```pgsql``` prefixed attributes in the pom.xml file, e.g. ```-Dpgsql.servername=pg1.example.com```.

## 3. DBAllocator

When specified, the test suite could take advantage of DBAllocator service and allocate an arbitrary database system.
It was tested to work well with Postgres family of database systems. The DBAllocator service is not public and thus it is not used in the upstream testing.

## 4. Container - Postgres

The fourth **and the most versatile mode** is to control a Docker daemon and to pull database images and to start 
containers. The test suite expects a Docker daemon listening to its REST commands and nothing else. 
No special or pre-fetched images or local dependencies are expected. Postgres family of databases is currently implemented.

# Examples

All undermentioned examples expect a Tomcat installation pointed to by ```CATALINA_HOME``` env var. Certain users (mandatory) and logging (convenient) settings are also listed:

```bash
wget http://www-us.apache.org/dist/tomcat/tomcat-9/v9.0.7/bin/apache-tomcat-9.0.7.zip
unzip apache-tomcat-9.0.7.zip
export CATALINA_HOME=`pwd`/apache-tomcat-9.0.7/

cat <<EOT >> ${CATALINA_HOME}/conf/logging.properties
org.apache.tomcat.tomcat-jdbc.level = ALL
org.h2.level = ALL
org.postgresql.level = ALL
javax.sql.level = ALL
org.apache.tomcat.tomcat-dbcp.level = ALL
com.arjuna.level = ALL
EOT

sed -i 's/<\/tomcat-users>/<user username="arquillian" password="arquillian" roles="manager-script"\/>\n<\/tomcat-users>/' ${CATALINA_HOME}/conf/tomcat-users.xml
```

That is all. Nothing else is to be done for the Tomcat installation. One does not start Tomcat, the TS does it automatically.

```bash
pushd narayana/tomcat
```

## 1. H2, embedded database
Run the TS:
```bash
mvn integration-test -Parq-tomcat -Dtomcat.user=arquillian -Dtomcat.pass=arquillian
```
See logs, including Tomcat ones:
```
vim tomcat-jta/target/failsafe-reports/org.jboss.narayana.tomcat.jta.integration.BaseITCase-output.txt
```
It is noteworthy that one can see the database trace log too, e.g.
```bash
...
406 /**/Statement stat2 = conn0.createStatement();
407 2018-02-13 16:55:05 jdbc[3]:
408 /**/stat2.execute("PREPARE COMMIT XID_131077_00000000000000000000ffff7f000001000097c35a830a590000000c0000000000000000_00000000000000000000ffff7f000001000097c35a830a590000000831");
409 2018-02-13 16:55:05 jdbc[3]:
410 /*SQL */PREPARE COMMIT XID_131077_00000000000000000000ffff7f000001000097c35a830a590000000c0000000000000000_00000000000000000000ffff7f000001000097c35a830a590000000831;
411 2018-02-13 16:55:05 jdbc[3]:
...
```

The overall runtime with the simple base integration 

## 2. DBAllocator

DBAllocator is a reservation tool paired with a driver repository, one simply adjusts these parameters:

```bash
mvn integration-test -Parq-tomcat -Dtomcat.user=arquillian -Dtomcat.pass=arquillian \
    -Ddballocator.host.port=http://your.db.allocator.instance.example.com:8080 \
    -Ddballocator.driver.url=http://path.to.your.drivers.stash.example.com/postgresql94/jdbc4/postgresql-42.1.1.jar \
    -Dtest.db.type=dballocator
```

To see logs:
```bash
vim tomcat-jta/target/failsafe-reports/org.jboss.narayana.tomcat.jta.integration.BaseITCase-output.txt
```

With DBAllocated database system, the TS does not retrieve database trace log though.

## 3. Container - Postgres

Verify you have a Docker daemon running. One can either use secure socket and a special user group or a plain REST control. Note that such exposure enables anyone to take control of the host system with a malicious container. The default Test suite setting is insecure and can be used only on trusted hosts withing trusted network with trusted images.
Note the ```-H``` option in your Docker daemon config:
```bash
cat /etc/sysconfig/docker | grep OPTIONS
OPTIONS='--selinux-enabled --log-driver=journald -H tcp://127.0.0.1:2375 -H unix:///var/run/docker.sock'
```
Check the daemon replies:
```bash
docker -H=tcp://127.0.0.1:2375 ps -a;
CONTAINER ID    IMAGE    COMMAND    CREATED    STATUS    PORTS    NAMES
```
Let's run the TS with a database from a container:

```bash
mvn integration-test -Parq-tomcat -Dtomcat.user=arquillian -Dtomcat.pass=arquillian \
    -Dtest.db.type=container \
    -Dcontainer.database.driver.artifact=org.postgresql:postgresql:42.2.1 \
    -Dcontainer.database.image=postgres:10
```

The aforementioned example uses ```postgres:10``` from [DockerHub Postgres](https://hub.docker.com/_/postgres/), i.e. a vanilla image without
any additional layers or modifications. All configuration is done at runtime, see class ```PostgresContainerAllocator``` 
around ```final CreateContainerResponse narayanaDB ...``` assignment. To implement e.g. a MariaDB allocator one would simply refactor ```PostgresContainerAllocator``` class into a hierarchy.

To see the results:
```bash
vim tomcat-jta/target/failsafe-reports/org.jboss.narayana.tomcat.jta.integration.BaseITCase-output.txt
```
It is noteworthy that the test suite retrieves trace log from the database container, see:
```bash
2018-02-13 16:12:57.982 UTC transaction_id:0 LOG:  execute <unnamed>: BEGIN
2018-02-13 16:12:57.983 UTC transaction_id:0 LOG:  execute <unnamed>: INSERT INTO test VALUES ('test-entry-16:12:57.966')
2018-02-13 16:12:57.985 UTC transaction_id:558 LOG:  execute <unnamed>: PREPARE TRANSACTION '131077_AAAAAAAAAAAAAP//fwAAAQAArnVagw6JAAAACDE=_AAAAAAAAAAAAAP//fwAAAQAArnVagw6JAAAADAAAAAAAAAAA'
2018-02-13 16:13:01.396 UTC transaction_id:0 LOG:  execute <unnamed>: SET extra_float_digits = 3
2018-02-13 16:13:01.396 UTC transaction_id:0 LOG:  execute <unnamed>: SET application_name = 'PostgreSQL JDBC Driver'
2018-02-13 16:13:01.398 UTC transaction_id:0 LOG:  execute <unnamed>: COMMIT PREPARED '131077_AAAAAAAAAAAAAP//fwAAAQAArnVagw6JAAAACDE=_AAAAAAAAAAAAAP//fwAAAQAArnVagw6JAAAADAAAAAAAAAAA'
2018-02-13 16:13:01.417 UTC transaction_id:0 LOG:  execute <unnamed>: SELECT COUNT(*) FROM test WHERE value='test-entry-16:12:57.966'
```

The container based testing is used upstream.

# Jenkins job

See Jenkins job: [https://ci.modcluster.io/job/narayana-tomcat/](https://ci.modcluster.io/job/narayana-tomcat/). There are three axis:
 * Tomcat version, 9.0.7
 * Database, h2, postgres:9.4 and postgres:10
 * JDK, openjdk-8 and oraclejdk-9
 
Narayana neither builds nor runs tests with JDK 9 at the moment, see: 
 * https://issues.jboss.org/browse/JBTM-2986
 * https://issues.jboss.org/browse/JBTM-2992
  
# Debugging
The best thing about using database in the [Container](#4-container---postgres-1) is you can easily start it manually, take the war file and investigate.

The test application the test suite generated is stored for you in ```narayana/tomcat/tomcat-deployment/_DEFAULT__Basic-app_test.war```, so one can simply setup a debug environment and use Byteman and Debugger ```-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1661 -Xnoagent``` to step the Tomcat run.

## Download and configure Tomcat
```bash
wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.7/bin/apache-tomcat-9.0.7.zip
unzip apache-tomcat-9.0.7.zip
export CATALINA_HOME=`pwd`/apache-tomcat-9.0.7/

cat <<EOT >> ${CATALINA_HOME}/conf/logging.properties
org.apache.tomcat.tomcat-jdbc.level = ALL
org.h2.level = ALL
org.postgresql.level = ALL
javax.sql.level = ALL
org.apache.tomcat.tomcat-dbcp.level = ALL
com.arjuna.level = ALL
EOT

sed -i 's/<\/tomcat-users>/<user username="arquillian" password="arquillian" roles="manager-script"\/>\n<\/tomcat-users>/' ${CATALINA_HOME}/conf/tomcat-users.xml
```

## Transactions API Jar and [JWS-976](https://issues.jboss.org/browse/JWS-976)
The current implementation expects [jboss-transaction-api_1.2_spec-1.0.0.Final.jar](http://central.maven.org/maven2/org/jboss/spec/javax/transaction/jboss-transaction-api_1.2_spec/1.1.0.Final/jboss-transaction-api_1.2_spec-1.1.0.Final.jar) to be present in your ```${CATALINA_HOME}/lib``` directory. The test suite adds the file on its own and it removes it after the test. So for this session, one might need to:
```bash
wget http://central.maven.org/maven2/org/jboss/spec/javax/transaction/jboss-transaction-api_1.2_spec/1.1.0.Final/jboss-transaction-api_1.2_spec-1.1.0.Final.jar -O ${CATALINA_HOME}/lib/jboss-transaction-api_1.2_spec-1.1.0.Final.jar
```
or
```bash
cp ~/.m2/repository/org/jboss/spec/javax/transaction/jboss-transaction-api_1.2_spec/1.0.0.Final/jboss-transaction-api_1.2_spec-1.0.0.Final.jar ${CATALINA_HOME}/lib/jboss-transaction-api_1.2_spec-1.1.0.Final.jar
```
## Byteman
To inject all kinds of failures and to examine and to modify the Java bytecode at runtime, we use Byteman as our weapon of choice.
```bash
wget http://downloads.jboss.org/byteman/3.0.11/byteman-download-3.0.11-bin.zip
unzip byteman-download-3.0.11-bin.zip
export BYTEMAN_HOME=`pwd`/byteman-download-3.0.11/
export BTM_SCRIPT=narayana/tomcat/tomcat-jta/src/test/resources/scripts.btm
```
## PostgreSQL container
This setup mimics what the test suite does for you automatically:
```bash
docker -H=tcp://127.0.0.1:2375 run \
    -e POSTGRES_PASSWORD=narayana_pass \
    -e POSTGRES_USER=narayana_user \
    -e POSTGRES_DB=narayana_db \
    -p 127.0.0.1:5432:5432/tcp \
    -d \
    -i \
    --name narayana_db \
    postgres:10 \
    postgres \
    -c deadlock_timeout=1s \
    -c default_transaction_deferrable=off \
    -c default_transaction_isolation="read committed" \
    -c default_transaction_read_only=off \
    -c log_directory=/tmp \
    -c log_filename=db.log \
    -c log_line_prefix="%m transaction_id: %x " \
    -c log_statement=all \
    -c logging_collector=on \
    -c max_connections=20 \
    -c max_locks_per_transaction=64 \
    -c max_pred_locks_per_transaction=64 \
    -c max_prepared_transactions=50
```
## Test application
Deploy the war file...
```bash
cp narayana/tomcat/tomcat-deployment/_DEFAULT__Basic-app_test.war ${CATALINA_HOME}/webapps/
```

## Running Tomcat
```bash
java \ 
-javaagent:$BYTEMAN_HOME/lib/byteman.jar=script:$BTM_SCRIPT \ 
-Djava.security.egd=file:/dev/./urandom \ 
-Djava.util.logging.config.file=$CATALINA_HOME/conf/logging.properties -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djdk.tls.ephemeralDHKeySize=2048 -Djava.protocol.handler.pkgs=org.apache.catalina.webresources \ 
-Dignore.endorsed.dirs= \ 
-classpath $CATALINA_HOME/bin/bootstrap.jar:$CATALINA_HOME/bin/tomcat-juli.jar \ 
-Dcatalina.base=$CATALINA_HOME \ 
-Dcatalina.home=$CATALINA_HOME \ 
-Djava.io.tmpdir=$CATALINA_HOME/temp \ 
org.apache.catalina.startup.Bootstrap start
```

## Accessing the app
```bash
curl localhost:8080/test/executor/recovery -i
```

## Monitoring database trace log
The test suite does this for you automatically and it also stores the trace log for posterity. If you need to debug manually as we describe in this example, you have to watch the log yourself. Note the container does not log to stdout but to a file within its ephemeral filesystem:
```bash
docker -H=tcp://127.0.0.1:2375 exec -it narayana_db bash
root@b8d46342ac0c:/# tail -f /tmp/db.log 
2018-04-17 15:18:52.585 UTC transaction_id: 0 LOG:  database system was shut down at 2018-04-17 15:18:52 UTC
2018-04-17 15:18:52.605 UTC transaction_id: 0 LOG:  database system is ready to accept connections
```

## Example context.xml file
This is how one's context.xml could look like to establish a pooled XA transactional datasource to a PostgreSQL database. Please note the connection pool values **are only testing ones**, they do not represent the numbers your web application could get the best performance with.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<Context>
    <!-- Narayana resources -->
    <Transaction
        factory="org.jboss.narayana.tomcat.jta.UserTransactionFactory"
    />
    <Resource
        factory="org.jboss.narayana.tomcat.jta.TransactionManagerFactory"
        name="TransactionManager"
        type="javax.transaction.TransactionManager"
    />
    <Resource
        factory="org.jboss.narayana.tomcat.jta.TransactionSynchronizationRegistryFactory"
        name="TransactionSynchronizationRegistry"
        type="javax.transaction.TransactionSynchronizationRegistry"
    />

    <!-- Database resources -->
    <Resource
        auth="Container"
        databaseName="narayana_db"
        description="Data Source"
        factory="org.postgresql.xa.PGXADataSourceFactory"
        loginTimeout="0"
        name="myDataSource"
        password="narayana_pass"
        portNumber="5432"
        serverName="127.0.0.1"
        type="org.postgresql.xa.PGXADataSource"
        uniqueName="myDataSource"
        user="narayana_user"
        username="narayana_user"
    />

    <Resource
        auth="Container"
        description="Transactional Driver Data Source"
        factory="org.jboss.narayana.tomcat.jta.TransactionalDataSourceFactory"
        initialSize="10"
        jmxEnabled="true"
        logAbandoned="true"
        maxAge="30000"
        maxIdle="15"
        maxTotal="20"
        maxWaitMillis="10000"
        minIdle="10"
        name="transactionalDataSource"
        password="narayana_pass"
        removeAbandoned="true"
        removeAbandonedTimeout="60"
        testOnBorrow="true"
        transactionManager="TransactionManager"
        type="javax.sql.XADataSource"
        uniqueName="transactionalDataSource"
        username="narayana_user"
        validationQuery="select 1"
        xaDataSource="myDataSource"
    />
</Context>
```
