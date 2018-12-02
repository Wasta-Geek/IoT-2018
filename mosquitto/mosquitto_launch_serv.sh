#! /bin/bash

if [ ! -f passwd ];
then
    touch passwd
    mosquitto_passwd -b passwd Iot-of-doom-admin doomdoomdoom
fi
mosquitto -v -c doom-mosquitto.conf
