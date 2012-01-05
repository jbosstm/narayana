# ALLOW JOBS TO BE BACKGROUNDED
set -m

x=`ruby -v`
if [ $? != 0 ]; then
	echo "Skipping recovery demo because it requires ruby to be installed"
	exit 0
else
	echo "Running recovery demo"
fi

mvn compile exec:java -Dexec.mainClass=quickstart.TransactionAwareResource -Dexec.args="-a 127.0.0.1:8081" &

sleep 5

./test.sh demo

echo "Recovering failed service - this could take up to 2 minutes"
rm -f out.txt
mvn compile exec:java -Dexec.mainClass=quickstart.TransactionAwareResource -Dexec.args="-a 127.0.0.1:8081" > out.txt &
pid=$!

# wait for message indicating that the transaction was recovered (should happen within 2 minutes)
count=0
res=0
while true; do
    grep "txStatus=TransactionCommitted" out.txt 
    if [ $? == 0 ]; then
        echo "SUCCESS: Transaction was recovered"
        res=0
		break
    fi

    sleep 6
    count=`expr $count + 1`

    if [ $count == 20 ]; then
        echo "FAILURE: Transaction was not recovered"
        break
    fi
done

kill $pid
exit $res
