import paho.mqtt.client as mqtt_client
from flask import Flask
from flask_restful import Api, Resource, reqparse
import time
import detectFace

broker = "broker.mqttdashboard.com"
root_topic = "doom_portal/"
lastPairingAttempt = 0
authenticatedDevices = []


def unlock_door(payload):

    detectFace.take_webcam_photo("temp_cam_photo.png")

#    print("Trying to unlock door with payload " + str(payload))
#    faces = detectFace.get_faces("cafaitquoidegagnerleswarmupdays.jpg")
#    if len(faces) == 0:
#        print("No face detected")
#        mqtt_client.publish(root_topic + "unlock_response", "NO FACE")
#        return

#    for face in faces:
#        print("Testing a face")
#    mqtt_client.publish(root_topic + "unlock_response", 1)


# improvement : get time to pair from mqtt call and set callback to publish a response
def await_pairing(payload):
    print("Trying to pair device with payload " + str(payload))
    global lastPairingAttempt
    lastPairingAttempt = time.time()


# HTTP
def manually_unlock_door(payload):
    print("Trying to manually unlock door with payload " + str(payload))


# HTTP
def feed_whitelist_info(payload):
    print("Trying to manually unlock door with payload " + str(payload))


# HTTP
def confirm_pairing(deviceId):
    print("Trying to confirm device pairing")
    if time.time() - lastPairingAttempt > 10:
        print("Did not authorize " + deviceId + " to access API because the last pairing attempt is too old")
        return 403
    if deviceId in authenticatedDevices:
        print("Did not authorize " + deviceId + " to access API because the device ID is already registered")
        return 409
    authenticatedDevices.append(deviceId)
    print("Authorized " + deviceId + " to access API")
    return 201


# ==============MQTT SETUP===============

mqtt_protocol = {
    root_topic + "unlock_door": unlock_door,
    root_topic + "await_pairing": await_pairing,
    # root_topic + "confirm_pairing": confirm_pairing
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


#mqtt_client.loop_start()

# ==============HTTP SETUP===============


class Pairing(Resource):
    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument("deviceId")
        args = parser.parse_args()
        return confirm_pairing(args["deviceId"])


app = Flask(__name__)
api = Api(app)
api.add_resource(Pairing, "/pair")

#app.run("127.0.0.1", 8080, use_reloader=False, debug=True)

mqtt_client.loop_forever()
