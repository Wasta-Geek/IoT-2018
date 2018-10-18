//
// Created by wasta-geek on 18/10/18.
//

#include <iostream>

#include "../includes/FaceDetector.hpp"

FaceDetector::FaceDetector()
{
}

FaceDetector::~FaceDetector()
{
}

bool FaceDetector::init()
{
    this->frontalFaceCascade = new cv::CascadeClassifier();

    if (!this->frontalFaceCascade->load("./data_opencv/haarcascades/haarcascade_frontalface_alt2.xml"))
    {
        std::cout << "Haarcascade frontal face xml file not found" << std::endl;
        return -1;
    }
    return 0;
}

void FaceDetector::detectFace(cv::Mat &frame, std::vector<cv::Rect> &faces)
{
    cv::Mat frame_gray;
    cvtColor( frame, frame_gray, cv::COLOR_BGR2GRAY );
    equalizeHist( frame_gray, frame_gray );
    //-- Detect faces
    this->frontalFaceCascade->detectMultiScale( frame_gray, faces);
}

void FaceDetector::showBoundingBox(cv::Mat &frame, std::vector<cv::Rect> &faces)
{
    for ( size_t i = 0; i < faces.size(); i++ )
    {
        cv::Point center( faces[i].x + faces[i].width/2, faces[i].y + faces[i].height/2 );
        ellipse( frame, center, cv::Size( faces[i].width/2, faces[i].height/2 ), 0, 0, 360, cv::Scalar( 100, 155, 20 ), 4 );
    }

    //-- Show what you got
    imshow( "Capture - Face detection", frame );
}