#! /bin/bash

if [ ! -f passwd ];
then
   echo "Password hasn't been set";
else
    mosquitto -v -c doom-mosquitto.conf
fi
