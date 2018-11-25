import cv2
import numpy


def take_webcam_photo(imageName):
    cam = cv2.VideoCapture(0)
    return_value, image = cam.read()
    cv2.imwrite(imageName, image)
    del cam


def get_faces(image_path):
    # Get user supplied values
    casc_path = "haarcascade_frontalface_default.xml"

    # Create the haar cascade
    face_cascade = cv2.CascadeClassifier(casc_path)

    # Read the image
    image = cv2.imread(image_path)
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Detect faces in the image
    faces = face_cascade.detectMultiScale(
        gray,
        scaleFactor=1.2,
        minNeighbors=5,
        minSize=(30, 30),
        flags=cv2.CASCADE_SCALE_IMAGE
    )

    print("Found {0} faces!".format(len(faces)))

    # Draw a rectangle around the faces
    # for (x, y, w, h) in faces:
      #  cv2.rectangle(image, (x, y), (x+w, y+h), (0, 255, 0), 2)

    # cv2.imshow("Faces found", image)
    # cv2.waitKey(0)

    return faces
