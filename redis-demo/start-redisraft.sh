#!/usr/bin/bash

REDIS_RAFT_MODULE=./redisraft.so
#REDIS_RAFT_MODULE=/home/mmusgrov/src/misc/redis/redisraft/redisraft.so

function start_redis {
  echo "STARTING on port 3000$1 with db raft${1}.rdb and file raftlog${1}.db"
  redis-server \
    --port 3000$1 --dbfilename raft${1}.rdb \
    --loadmodule $REDIS_RAFT_MODULE \
    --raft.log-filename raftlog${1}.db \
    --raft.follower-proxy yes \
    --raft.addr localhost:3000${1} &
}

for instance in 1 2 3; do
  start_redis $instance
done

sleep 1

for instance in 1 2 3; do
  case $instance in
  1) echo initialize the cluster
     redis-cli -p 30001 raft.cluster init
     ;;
  2) echo joining 30002 to the cluster
     sleep 1
     redis-cli -p 30002 RAFT.CLUSTER JOIN localhost:30001
     ;;
  3) echo joining 30003 to the cluster
     sleep 1
     redis-cli -p 30003 RAFT.CLUSTER JOIN localhost:30001
     ;;
  esac
done

# for info about the cluster use
# redis-cli -p 30001 INFO raft

#    redis-cli -p 30002 slaveof localhost:30001

#redis-cli -p 30001 INFO raft
