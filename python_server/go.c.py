import paho.mqtt.client as mqtt_client

broker = "broker.mqttdashboard.com"
root_topic = "doom_portal/"


def unlock_door(payload):
    print("Trying to unlock door with payload " + str(payload))


def await_pairing(payload):
    print("Trying to pair device with payload " + str(payload))


def confirm_pairing(payload):
    print("Trying to confirm device pairing with payload " + str(payload))


mqtt_protocol = {
    root_topic + "unlock_door": unlock_door,
    root_topic + "await_pairing": await_pairing,
    root_topic + "confirm_pairing": confirm_pairing
}


def callback_msg(client, userdata, message):
    print("Received : " + str(message.payload.decode("utf-8")) + " on topic " + str(message.topic))
    mqtt_protocol[message.topic](message.payload.decode("utf-8"))


def callback_connect(client, userdata, flags, response_code):
    print("Connection result: ", str(response_code))
    for topic in mqtt_protocol:
        client.subscribe(topic)


mqtt_client = mqtt_client.Client("yuruh")
mqtt_client.on_connect = callback_connect
mqtt_client.on_message = callback_msg

print("Connecting to broker ", broker)
mqtt_client.connect(broker)


mqtt_client.loop_forever()
