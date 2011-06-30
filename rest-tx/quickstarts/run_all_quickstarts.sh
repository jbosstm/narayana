# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running all quickstarts"

for i in `find * -name run.sh | sed 's#\(.*\)/.*#\1#' |sort -u`
do
    cd $i
    ./run.sh
    if [ "$?" != "0" ]; then
	    exit -1
    fi
    cd -
done

echo "All quickstarts ran OK"
