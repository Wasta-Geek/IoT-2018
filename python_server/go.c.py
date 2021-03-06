import os

os.environ['CLOUDINARY_URL'] = "cloudinary://584812496861958:Y9f-WHpXczrM0C0WjfP7PB6MQEo@dnpgw8jbn"

import paho.mqtt.client as mqtt_client
from flask import Flask
from flask_restful import Api, Resource, reqparse, abort
import time
import detectFace
import werkzeug
import face_recognition
import glob
import base64
from cloudinary.uploader import upload
from cloudinary.utils import cloudinary_url
from cloudinary import CloudinaryImage
from pyfcm import FCMNotification

broker = "broker.mqttdashboard.com"
root_topic = "doom_portal/"
lastPairingAttempt = 0
authenticatedDevices = ["fred"]
photoFilename = "cam_photo.png"
photoAtDoorIdentifier = "intruder"

push_service = FCMNotification(api_key="AAAAY0ath8E:APA91bHYBjrFZvTUSeL5ieVdeRvqMHauBDSCvgeV19jdxznTbZvVTfVKArgc1P8cUeNOskV3cZzQQa2rq0QiN3rh0FQ3D1wWlk8-g2EAgnwyhZlSp0rzCxf7r5cgMc4RJJk_7uptG-te")
registration_id = "dCmfDNpJUiE:APA91bFoE-kT4FmfrHErfyylghM34Wg2vkP74ZlCc8o2uXSLtfEDasJWERfw6ffqP_kDbmIEt_Ir4TbDxzD0KEIYjrO2q1kWK_bjZXgXmiCv_fWruQLD95jeBsaOgd8NZsYkfeNJrpFq"

def upload_existing_whitelist():
    print("Uploading whitelist folder ...")
    for person in os.listdir("./whitelist"):
        upload(os.path.join("whitelist", person, person + ".jpg"), public_id=person)
        # path = os.path.join("whitelist", person, person + ".jpg")


upload_existing_whitelist()


def unlock_door(payload):
    print("Trying to unlock door with payload " + str(payload))

    detectFace.take_webcam_photo(photoFilename)
    upload(photoFilename, public_id=photoAtDoorIdentifier)
    unknown_image = face_recognition.load_image_file(photoFilename)
    unknowns_encoding = face_recognition.face_encodings(unknown_image)

    whitelist = glob.glob("./whitelist/*/*.jpg")

    for people in whitelist:

        for unknown_encoding in unknowns_encoding:

            known_image = face_recognition.load_image_file(people)
            biden_encoding = face_recognition.face_encodings(known_image)[0]

            results = face_recognition.compare_faces([biden_encoding], unknown_encoding)

            if results[0]:
                print(results[0])
                mqtt_client.publish(root_topic + "unlock_response", "AUTHORIZED")
                print("AUTHORIZED")
                return

    message_title = "OH MON DIEU UN INCONNU"
    photo_url, options = cloudinary_url("intruder", format="png", crop="fill")
    data_message = {
        "data": {
            "url": str(photo_url)
        }
    }

    push_service.notify_single_device(registration_id=registration_id, message_title=message_title, data_message=data_message)
    mqtt_client.publish(root_topic + "unlock_response", "UNAUTHORIZED")
    print("UNAUTHORIZED")


# improvement : get time to pair from mqtt call and set callback to publish a response
def await_pairing(payload):
    print("Trying to pair device with payload " + str(payload))
    global lastPairingAttempt
    lastPairingAttempt = time.time()


# HTTP
def manually_unlock_door(authorize, deviceId):
    if deviceId not in authenticatedDevices:
        abort(401)
    if authorize in ['true', '1', 'y', 'True']:
        mqtt_client.publish(root_topic + "unlock_response", "AUTHORIZED")
    else:
        mqtt_client.publish(root_topic + "unlock_response", "UNAUTHORIZED")


def get_whitelist(deviceId):
    if deviceId not in authenticatedDevices:
        abort(401)
    ret = []
    persons = os.listdir("./whitelist")
    for person in persons:
        photo_url, options = cloudinary_url(person, format="jpg", crop="fill")
        json_object = {'name': person, 'photoUrl': photo_url}
        ret.append(json_object)

        #        with open(os.path.join("whitelist", person, person + ".jpg"), "rb") as image_file:
#            byte_content = image_file.read()
#            encoded = base64.b64encode(byte_content)
#           json_object = {'name': person, 'photoUrl': encoded.decode("utf-8")}
#            ret.append(json_object)
    return ret


# HTTP
def store_whitelist(deviceId, name, picture):
    if deviceId not in authenticatedDevices:
        abort(401)
    if picture and name:  # and name not in os.listdir('./whitelist'):
        filename = name + ".jpg"
        if not os.path.exists("whitelist"):
            os.mkdir("whitelist")
        if not os.path.exists(os.path.join("whitelist", name)):
            os.mkdir(os.path.join("whitelist", name))
        path = os.path.join("whitelist", name, filename)
        picture.save(path)
        upload_result = upload(path, public_id=name)
#        thumbnail_url1, options = cloudinary_url(upload_result['public_id'], format="jpg", crop="fill")
 #       print(str(thumbnail_url1))
    else:
        abort(403)


# HTTP
def confirm_pairing(deviceId):
    print("Trying to confirm device pairing")
    if time.time() - lastPairingAttempt > 10:
        print("Did not authorize " + deviceId + " to access API because the last pairing attempt is too old")
        abort(403)
    if deviceId in authenticatedDevices:
        print("Did not authorize " + deviceId + " to access API because the device ID is already registered")
        abort(409)
    authenticatedDevices.append(deviceId)
    print("Authorized " + deviceId + " to access API")
    mqtt_client.publish(root_topic + "result_pairing", "PAIRED")
    return deviceId, 201


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
mqtt_client.username_pw_set("Iot-of-doom-admin", password="doomdoomdoom")
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
        parser.add_argument("Authorization", location='headers')
        args = parser.parse_args()
        return manually_unlock_door(args["authorize"], args["Authorization"])


class Whitelist(Resource):
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument("Authorization", location='headers')
        args = parser.parse_args()
        return get_whitelist(args["Authorization"])

    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument("Authorization", location='headers')
        parser.add_argument('picture', type=werkzeug.datastructures.FileStorage, location='files')
        parser.add_argument("name")
        args = parser.parse_args()
        return store_whitelist(args["Authorization"], args["name"], args["picture"])


app = Flask(__name__)
api = Api(app)
api.add_resource(Pairing, "/pair")
api.add_resource(ManualUnlock, "/unlock")
api.add_resource(Whitelist, "/whitelist")

app.run("127.0.0.1", 5050, use_reloader=False, debug=True)

mqtt_client.loop_stop()
