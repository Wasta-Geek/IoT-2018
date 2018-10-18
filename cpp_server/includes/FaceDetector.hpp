//
// Created by wasta-geek on 18/10/18.
//

#ifndef IOT_2018_FACEDETECTOR_HPP
#define IOT_2018_FACEDETECTOR_HPP


#include <opencv/cv.hpp>

class FaceDetector
{
public:
    FaceDetector();
    ~FaceDetector();
    bool init();
    void detectFace(cv::Mat&, std::vector<cv::Rect>&);
    void showBoundingBox(cv::Mat&, std::vector<cv::Rect>&);
private:
    cv::Ptr<cv::CascadeClassifier> frontalFaceCascade;
};

#endif //IOT_2018_FACEDETECTOR_HPP
