#!/bin/sh

# start txm
# bin/standalone.sh
# start participant
# mvn compile exec:java -Dexec.mainClass=quickstart.TransactionAwareResource -Dexec.arg"-a 172.17.130.188:8081"
#./test start
#./test service 0_ffff7f000001_1831c3b0_4e2ed0f3_141
#./test commit 0_ffff7f000001_1831c3b0_4e2ed0f3_141


function fatal {
	echo "syntax: start | abort txid | commit txid | status txid | service txid | fail wid"
	exit 1
}

aws=0

if [ $aws != 0 ]; then
	ep="gondolin.ncl.ac.uk:9188"
	auth="demo4-mm2.dev.rhcloud.com"
	proxies=1

#mvn compile exec:java -Dexec.mainClass=quickstart.TransactionAwareResource -Dexec.args="-a 127.0.0.1:8080" -Dhttp.proxyHost="file.rdu.redhat.com" -Dhttp.proxyPort=3128
else
#cd /home/mmusgrov/source/as/jboss-7.0.0.CR1
#bin/standalone.sh -DRecoveryEnvironmentBean.periodicRecoveryPeriod=10 &
#mvn compile exec:java -Dexec.mainClass=quickstart.TransactionAwareResource -Dexec.args="-a 127.0.0.1:8081"
	ep="172.17.130.188:8081"
	ep="127.0.0.1:8081"
	auth="127.0.0.1:9090"
	proxies=0
fi

service="http://$ep/service"
tm="http://${auth}/rest-tx/tx/transaction-manager"

if [ $# == 0 ]; then
	ruby client.rb -p $proxies -a "${tm}"
	exit 0
fi

case "$1" in
"status")
	[[ $# == 1 ]] && fatal
	ruby client.rb -p $proxies -v Get -a "${tm}/${2}";;
"start")
	ruby client.rb -p $proxies -v Post -a "$tm" -b "timeout=0";;
"commit")
	[[ $# == 1 ]] && fatal
	ruby client.rb -p $proxies -v Put -a "${tm}/${2}/terminate" -b "txStatus=TransactionCommitted";;
"abort")
	[[ $# == 1 ]] && fatal
	ruby client.rb -p $proxies -v Put -a "${tm}/${2}/terminate" -b "txStatus=TransactionRolledBack";;
"service")
	if [ $# == 1 ]; then
		ruby client.rb -p $proxies -v Get -a "$service/query"
	else
		ruby client.rb -p $proxies -v Get -a "$service?counter=0&enlistURL=${tm}/${2}"
	fi;;
"fail")
	[[ $# == 1 ]] && fatal
	ruby client.rb -p $proxies -v Get -a "$service?failWid=$2";;
"demo")
	ruby client.rb -p $proxies -v Post -a "$tm" -b "timeout=0"
	txn=`cat tx`
	echo "service url for counter 0: $service?counter=0&enlistURL=${tm}/${txn}"

	wid1=`ruby client.rb -p $proxies -v Get -a "$service?counter=0&enlistURL=${tm}/${txn}"`
	wid2=`ruby client.rb -p $proxies -v Get -a "$service?counter=1&enlistURL=${tm}/${txn}"`
#echo "WORK ids: $wid1 and $wid2"
	ruby client.rb -p $proxies -v Get -a "$service?failWid=${wid2}"
	ruby client.rb -p $proxies -v Put -a "${tm}/${txn}/terminate" -b "txStatus=TransactionCommitted"
	;;
*)
	fatal;;
esac
