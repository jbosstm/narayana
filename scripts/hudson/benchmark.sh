
# -i (1 iteration), -wi (10 warm ups), -r (300 seconds at each iteration)
# use java -jar <maven module>/target/benchmarks.jar -h for options
#[ -z "${JMHARGS}" ] && JMHARGS="-i 1 -wi 10 -f 1 -r 300"
[ -z "${JMHARGS}" ] && JMHARGS="-i 1 -wi 1 -f 1 -r 1"

[ -z "${WORKSPACE}" ] && WORKSPACE=`pwd`
MAVEN_HOME=$WORKSPACE/tools/maven
PATH=$MAVEN_HOME/bin:$PATH
ofile=$WORKSPACE/benchmark-output.txt

# run a set of benchmarks and copy the generated jmh csv files to $WORKSPACE
function run_bm {
  suffix=".\*"
  f=${2%$suffix}
  CSV_DIR="$f/target/jmh"
  [ -d $CSV_DIR ] || mkdir -p $CSV_DIR
  CSVF="$CSV_DIR/$f-$3.csv"

  java -jar $1/target/benchmarks.jar "$2" $JMHARGS -rf csv -rff $CSVF

  cp $CSVF $WORKSPACE # the jmh plugin looks for csv files in $WORKSPACE
}

# build the product in order to calculate a baseline for the benchmark
function build_narayana_master {
  cd $WORKSPACE
  [[ -d tmp ]] || mkdir tmp
  cd tmp

  rm -rf narayana
  git clone https://github.com/jbosstm/narayana.git
  cd narayana
  ./build.sh clean install -DskipTests
}

# build the benchmarks
function build_benchmark {
  cd $WORKSPACE
  [[ -d tmp ]] || mkdir tmp
  cd tmp
  rm -rf performance
  git clone https://github.com/jbosstm/performance
  cd performance/narayana

  mvn package -DskipTests # build the benchmarks
}

BMDIR=$WORKSPACE/tmp/performance/narayana
BM1="com.hp.mwtests.ts.arjuna.performance.Performance1.*"
BM2="com.hp.mwtests.ts.arjuna.atomicaction.CheckedActionTest.*"
BM3="com.arjuna.ats.jta.xa.performance.JTAStoreTests.*"
BM4="io.narayana.perf.product.ProductComparison.*"

function run_benchmarks {
  cd $WORKSPACE/tmp/performance/narayana
  run_bm $BMDIR/ArjunaCore/arjuna "$BM1" $1
  run_bm $BMDIR/ArjunaCore/arjuna "$BM2" $1
  run_bm $BMDIR/ArjunaJTA/jta "$BM3" $1
}

function regression_check {
  cd $WORKSPACE/tmp/performance/narayana
  ls $WORKSPACE/*.csv > /dev/null 2>&1
  res=$?

  if [ $res = 0 ]; then
    # "$@" should only contain jvm args (such as -D -X etc)
    java "$@" -cp tools/target/classes io.narayana.perf.jmh.Benchmark $WORKSPACE/*.csv >> $ofile
    res=$?
  else
    echo "no benchmarks to compare"
  fi

  return $res
}

function generate_csv_files {
  build_benchmark # build the benchmarks

  run_benchmarks pr # run the benchmarks against the local maven repo (should be the PR)
  build_narayana_master # build narayana master
  run_benchmarks master # run the benchmarks against this build of master
}

echo "JMH benchmark run (with args $JMHARGS)\n" > $ofile
generate_csv_files
regression_check "$@"
rv=$?
cd $WORKSPACE

exit $rv
