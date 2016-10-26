#JAVA_HOME=/usr/local/jdk1.8.0_91
#export JAVA_HOME
function fatal {
  echo "$1"
  exit 1
}

JAR_DIR=target/dependency

which vertx &> /dev/null
if [ $? = 1 ]; then
  fatal "Please install vertx or add the vertx bin dir to your path"
fi

if [ ! -d $JAR_DIR ]; then
  echo "Attempting to download jar dependencies for this example via mvn:"
  mvn clean dependency:copy-dependencies
  if [ $? = 1 ]; then
    fatal "mvn clean dependency:copy-dependencies failed"
  fi
fi

if [ $# = 0 ]; then
  fatal "syntax: $0 <java verticle source file>"
fi

export CLASSPATH="$JAR_DIR/*:." 
vertx run $1 -cp "$CP"

