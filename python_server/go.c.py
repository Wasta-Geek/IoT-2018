import paho.mqtt.client as mqtt_client
from flask import Flask
from flask_restful import Api, Resource, reqparse
import time
import detectFace
import werkzeug
import face_recognition
import os


broker = "broker.mqttdashboard.com"
root_topic = "doom_portal/"
lastPairingAttempt = 0
authenticatedDevices = ["fred"]
photoFilename = "cam_photo.png"


def unlock_door(payload):
    print("Trying to unlock door with payload " + str(payload))

    detectFace.take_webcam_photo(photoFilename)

    known_image = face_recognition.load_image_file("jeteconnais/Chelmi.jpg")
    unknown_image = face_recognition.load_image_file(photoFilename)

    biden_encoding = face_recognition.face_encodings(known_image)[0]
    unknown_encoding = face_recognition.face_encodings(unknown_image)

    for unknown_face in unknown_encoding:
        results = face_recognition.compare_faces([biden_encoding], unknown_face)
        print(results)

    mqtt_client.publish(root_topic + "unlock_response", 1)


# improvement : get time to pair from mqtt call and set callback to publish a response
def await_pairing(payload):
    print("Trying to pair device with payload " + str(payload))
    global lastPairingAttempt
    lastPairingAttempt = time.time()


# HTTP
def manually_unlock_door(authorize, deviceId):
    if deviceId not in authenticatedDevices:
        return 403
    if authorize in ['true', '1', 'y', 'True']:
        mqtt_client.publish(root_topic + "unlock_response", "AUTHORIZE")
    else:
        mqtt_client.publish(root_topic + "unlock_response", "UNAUTHORIZE")
    return 200


# HTTP
def store_whitelist(deviceId, name, picture):
    if deviceId not in authenticatedDevices:
        return 403
    if picture and name:
        filename = name + ".jpg"
        if not os.path.exists("whitelist"):
            os.mkdir("whitelist")
        if not os.path.exists(os.path.join("whitelist", name)):
            os.mkdir(os.path.join("whitelist", name))
        picture.save(os.path.join("whitelist", name, filename))
        return 200
    else:
        return 400


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
}


def callback_msg(client, userdata, message):
    print("Received : " + str(message.payload.decode("utf-8")) + " on topic " + str(message.topic))
    mqtt_protocol[message.topic](message.payload.decode("utf-8"))


def callback_connect(client, userdata, flags, response_code):
    print("Connection result: ", str(response_code))
    for topic in mqtt_protocol:
        client.subscribe(topic)


def callback_disconnect(client, userdata, rc):
    print("User disconnected with response code : ", str(rc))


# set name if it keeps connecting and disconnecting
mqtt_client = mqtt_client.Client()
mqtt_client.on_connect = callback_connect
mqtt_client.on_disconnect = callback_disconnect
mqtt_client.on_message = callback_msg

print("Connecting to broker ", broker)
mqtt_client.connect(broker)


mqtt_client.loop_start()

# ==============HTTP SETUP===============


class Pairing(Resource):
    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument("deviceId")
        args = parser.parse_args()
        return confirm_pairing(args["deviceId"])


class ManualUnlock(Resource):
    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument("authorize")
        parser.add_argument("deviceId")
        args = parser.parse_args()
        return manually_unlock_door(args["authorize"], args["deviceId"])


class FeedWhitelist(Resource):
    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument("deviceId")
        parser.add_argument('picture', type=werkzeug.datastructures.FileStorage, location='files')
        parser.add_argument("name")
        args = parser.parse_args()
        return store_whitelist(args["deviceId"], args["name"], args["picture"])


app = Flask(__name__)
api = Api(app)
api.add_resource(Pairing, "/pair")
api.add_resource(ManualUnlock, "/unlock")
api.add_resource(FeedWhitelist, "/whitelist")

app.run("127.0.0.1", 5050, use_reloader=False, debug=True)

mqtt_client.loop_stop()
