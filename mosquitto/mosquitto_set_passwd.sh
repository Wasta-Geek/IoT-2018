#! /bin/bash

touch passwd
mosquitto_passwd -b passwd Iot-of-doom-admin doomdoomdoom
