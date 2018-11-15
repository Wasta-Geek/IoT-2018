import paho.mqtt.client as mqtt_client

broker = "broker.mqttdashboard.com"
topic = "DoomPortal/topic"


def callback_msg(client, userdata, message):
    print("Received : " + str(message.payload) + " on topic " + str(message.topic))


def callback_connect(client, userdata, flags, response_code):
    print("Connection result: ", str(response_code))
    client.subscribe(topic)


mqtt_client = mqtt_client.Client("yuruh")
mqtt_client.on_connect = callback_connect
mqtt_client.on_message = callback_msg

print("Connecting to broker ", broker)
mqtt_client.connect(broker)


mqtt_client.loop_forever()
