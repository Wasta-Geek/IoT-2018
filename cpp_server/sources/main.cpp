#include <iostream>
#include "../includes/FaceDetector.hpp"

int main()
{
    FaceDetector faceDetector;
    if (faceDetector.init() == -1)
        return -1;
    cv::VideoCapture inputVideo(0);
    if (!inputVideo.isOpened())
    {
        std::cout << "Cannot access default camera, please check your settings" << std::endl;
        return -1;
    }
    cv::Mat frame;
    while (inputVideo.read(frame))
    {
        if (frame.empty())
        {
            std::cout << "Frame empty, dafuck ?" << std::endl;
            return -1;
        }
        //-- 3. Apply the classifier to the frame
        std::vector<cv::Rect> faces;
        faceDetector.detectFace(frame, faces);
        faceDetector.showBoundingBox(frame, faces);

        if (cv::waitKey(10) == 27 )
        {
            break; // escape
        }
    }
    inputVideo.release();
    return 0;
}