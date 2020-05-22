#!/bin/bash

SERVER=$1
USER=$2
PASSWORD=$3

if [ "x$SERVER" = "x" ] || [ "x$USER" = "x" ] || [ "x$PASSWORD" = "x" ]; then
	echo "usage: $0 <server:port> <user> <password>"
	exit
fi
bin/kafka-configs.sh --zookeeper $SERVER --alter --add-config "SCRAM-SHA-512=[password=$PASSWORD]" --entity-type users --entity-name $USER
